package com.smsoft.smartdisplay.ui.screen.doorbell

import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.annotation.MainThread
import dagger.Lazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plays the doorbell camera for the doorbell page of the dashboard and for the doorbell screen
 * that a ring opens. While the screen fades in, the page is still on screen, so both can be shown
 * at once. The last screen that starts a visit owns the video, and only the owner may stop it.
 * Before, the second attachViews() threw IllegalStateException and crashed the app (a ring while
 * the dashboard showed its doorbell page), and the page's stop would have ended the new screen's
 * stream. Main thread only; libVLC delivers the player events on the main thread as well.
 *
 * Every connection attempt gets its own MediaPlayer. The app used to keep one player for its whole
 * life, and libVLC 3 handles one player stopped and started many times badly (VLC for Android
 * creates a new player for every playback). stop() and release() wait until libVLC's input thread
 * has ended. That can take seconds when the camera does not answer, and an RTSP stream that hangs
 * while it opens can keep stop() waiting for good (see [CONNECT_TIMEOUT_MS]). So an old player is
 * muted and detached at once, then stopped and released in the background, each player on its own
 * thread. Before, stop() ran on the main thread and an unreachable camera froze the UI.
 * attachViews() and detachViews() stay on the main thread.
 *
 * While its visit is current, a failed attempt is replaced by a new player on the same video
 * layout: after an error, the end of the stream, a stop that nobody asked for, no Playing within
 * [CONNECT_TIMEOUT_MS] or no progress for [STALL_TIMEOUT_MS]. The new player starts after the
 * backoff ([reconnectDelayMs]), and once the old one is released if that one has played (see
 * Visit.reconnect). Before, a camera reboot left a frozen picture until the page was left, and the
 * dashboard page has no back timer.
 */
@Singleton
class DoorbellStreamPlayer @Inject constructor(
    // Lazy: SmartDisplayApplication creates it on a background thread at startup; an attempt
    // that comes earlier waits for it.
    private val libVlc: Lazy<LibVLC>
) {
    private var current: Visit? = null

    // stop() and release() of the old players, off the main thread and each on its own thread: a
    // stop that never returns (see CONNECT_TIMEOUT_MS) then holds up only its own player, not the
    // stops of later visits, whose players would go on streaming. A view of Dispatchers.IO: its
    // threads do not count against the 64 that the rest of the app shares, so hung stops cannot
    // use those up. MAX_RELEASE_THREADS is far more than the stops that overlap otherwise; a stop
    // that finds them all taken waits, with its player muted and detached already.
    private val releaseScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(MAX_RELEASE_THREADS))

    /** One player and its media, from play() until the attempt is closed. */
    private class Attempt(val player: MediaPlayer, val media: Media, val startedMs: Long) {
        var playingSinceMs: Long? = null
        val time = ProgressSignal()
        val pictures = ProgressSignal()
        var watchdog: Job? = null
    }

    inner class Visit internal constructor(private val layout: VLCVideoLayout) {
        // The watchdog and the reconnects. Cancelled when the visit ends or is taken over, so no
        // player is started after that.
        private val scope = MainScope()
        private var url: String? = null
        private var attempt: Attempt? = null
        private var failuresInARow = 0

        // An attempt of this visit got to Playing: the camera works, so it is retried quickly
        // (see reconnectDelayMs).
        private var hasPlayed = false

        // The release of an old player that had not played and that the next attempt did not
        // wait for (see reconnect()).
        private var unwaitedRelease: Job? = null

        val isCurrent: Boolean
            get() = current === this

        /** Starts the stream; once per visit. */
        fun play(url: String) {
            if (!isCurrent || this.url != null) {
                return
            }
            // Neither of these can ever play; started, they would fail and reconnect forever.
            if (url.isBlank()) {
                Log.i(TAG, "No doorbell stream URL in Settings")
                return
            }
            if (isRtspsUrl(url)) {
                // No URL in the log: it can hold the camera password.
                Log.w(TAG, "rtsps:// is not supported by libVLC 3 (no RTSP over TLS), use rtsp://")
                return
            }
            this.url = url
            startAttempt()
        }

        /** Stops the stream if this visit still owns the video; a newer visit is left alone. */
        fun end() {
            if (!isCurrent) {
                return
            }
            current = null
            close()
        }

        internal fun close() {
            scope.cancel()
            attempt?.let { closeAttempt(it) }
            attempt = null
        }

        private fun startAttempt() {
            val url = url ?: return
            if (!isCurrent || attempt != null) {
                return
            }
            val vlc = try {
                libVlc.get()
            } catch (e: IllegalStateException) {
                // libvlc_new() failed ("can't create LibVLC instance"); a retry would fail alike.
                Log.e(TAG, "libVLC could not be created", e)
                return
            }
            val media = Media(vlc, Uri.parse(url)).apply {
                // Low latency for a live camera. All of these exist in VLC 3.0; ":low-delay" is a
                // VLC 4 option that 3.0 does not know, so it was removed.
                addOption(":file-caching=0")
                addOption(":network-caching=500")
                addOption(":live-caching=0")
                addOption(":clock-jitter=0")
                addOption(":clock-synchro=0")
                addOption(":drop-late-frames")
                addOption(":skip-frames")
                // After the caching options: libVLC 3 adds :file-caching=1500 and
                // :network-caching=1500 here for each of them that is not set yet.
                setHWDecoderEnabled(true, false)
            }
            val player = MediaPlayer(vlc)
            // libVLC 3 fits the picture in Java (VideoHelper). By default it takes the orientation
            // from the configuration and swaps the box when the view's shape does not match it. A
            // square screen (the NSPanel Pro's 480x480) counts as portrait, so the doorbell page,
            // wider than high above the pager's dots, got a picture about a tenth smaller. Taken
            // from the view's bounds, the box is never swapped.
            player.setUseOrientationFromBounds(true)
            val newAttempt = Attempt(player, media, SystemClock.elapsedRealtime())
            attempt = newAttempt
            player.attachViews(layout, null, false, false)
            player.setEventListener { event -> onEvent(newAttempt, event) }
            player.media = media
            player.play()
            newAttempt.watchdog = scope.launch {
                while (true) {
                    delay(WATCHDOG_INTERVAL_MS)
                    check(newAttempt)
                }
            }
        }

        private fun onEvent(eventAttempt: Attempt, event: MediaPlayer.Event) {
            // Events of a closed attempt can still be queued; its player is being released.
            if (eventAttempt !== attempt) {
                return
            }
            when (event.type) {
                // Several times a second, not logged.
                MediaPlayer.Event.TimeChanged ->
                    eventAttempt.time.update(event.timeChanged, SystemClock.elapsedRealtime())
                MediaPlayer.Event.PositionChanged, MediaPlayer.Event.Buffering -> Unit
                else -> {
                    Log.d(TAG, "VLC state:" + event.type)
                    when (event.type) {
                        MediaPlayer.Event.Playing -> if (eventAttempt.playingSinceMs == null) {
                            eventAttempt.playingSinceMs = SystemClock.elapsedRealtime()
                            hasPlayed = true
                        }
                        MediaPlayer.Event.EncounteredError -> reconnect("error")
                        MediaPlayer.Event.EndReached -> reconnect("end of stream")
                        // Never this class's own stop: it removes the listener before stopping.
                        MediaPlayer.Event.Stopped -> reconnect("stopped")
                    }
                }
            }
        }

        private fun check(checkedAttempt: Attempt) {
            if (checkedAttempt !== attempt) {
                return
            }
            val now = SystemClock.elapsedRealtime()
            checkedAttempt.media.stats?.let {
                checkedAttempt.pictures.update(it.displayedPictures.toLong(), now)
            }
            val health = streamHealth(
                nowMs = now,
                startedMs = checkedAttempt.startedMs,
                playingSinceMs = checkedAttempt.playingSinceMs,
                lastTimeChangeMs = checkedAttempt.time.lastChangeMs,
                lastPictureChangeMs = checkedAttempt.pictures.lastChangeMs
            )
            when (health) {
                StreamHealth.OK -> if (isStablePlayback(now, checkedAttempt.playingSinceMs)) {
                    failuresInARow = 0
                }
                StreamHealth.CONNECT_TIMEOUT ->
                    reconnect("not playing after $CONNECT_TIMEOUT_MS ms")
                StreamHealth.STALLED -> reconnect("no progress for $STALL_TIMEOUT_MS ms")
            }
        }

        private fun reconnect(reason: String) {
            val failed = attempt ?: return
            attempt = null
            val released = closeAttempt(failed)
            if (startsSlowReconnects(failuresInARow, hasPlayed)) {
                // No URL in the log: it can hold the camera password.
                Log.w(
                    TAG,
                    "The doorbell stream failed ${failuresInARow + 1} times in a row without" +
                        " playing; check the stream URL and the camera password. Trying less often"
                )
            }
            val delayMs = reconnectDelayMs(failuresInARow, hasPlayed)
            failuresInARow++
            Log.w(TAG, "Doorbell stream lost ($reason), reconnecting in $delayMs ms")
            // The new player renders to the same surface, so an old player that has played (it
            // has a decoder and a video output) has to be gone first. One that never played is
            // still opening the stream and has neither, and its stop may wait for the camera for
            // good (see CONNECT_TIMEOUT_MS); waiting for it could end the reconnects of the visit.
            // Such a player is not waited for, but only one per visit at a time: a camera that
            // hangs on every connection holds at most two players of a visit instead of one more
            // after every reconnect.
            val waitForRelease = failed.playingSinceMs != null || unwaitedRelease?.isActive == true
            if (!waitForRelease) {
                unwaitedRelease = released
            }
            val failedMs = SystemClock.elapsedRealtime()
            scope.launch {
                if (waitForRelease) {
                    released.join()
                }
                delay(delayMs - (SystemClock.elapsedRealtime() - failedMs))
                startAttempt()
            }
        }
    }

    @MainThread
    fun startVisit(layout: VLCVideoLayout): Visit {
        // Takes over from an older screen that is still on screen: its player is muted, detached
        // and released, and it no longer reconnects.
        current?.close()
        return Visit(layout).also { current = it }
    }

    /**
     * Mutes and detaches the player of [closed] and stops and releases it in the background.
     * Returns the job of that release.
     */
    private fun closeAttempt(closed: Attempt): Job {
        closed.watchdog?.cancel()
        // Also drops the events of this player that are still queued for the main thread.
        closed.player.setEventListener(null)
        // The stop runs in the background and can take a while to end the stream; until then the
        // player plays what it still receives. detachViews() only switches its video track off,
        // so the camera's sound would go on: it is muted here, before the stop even starts.
        closed.player.setVolume(0)
        closed.player.detachViews()
        return releaseScope.launch {
            // The line below is written only once stop() has returned, so a stop that hangs
            // would never show in the log.
            val stillStopping = launch(Dispatchers.Default) {
                delay(STILL_STOPPING_LOG_MS)
                Log.w(
                    TAG,
                    "Stopping a doorbell stream has not finished after $STILL_STOPPING_LOG_MS ms;" +
                        " libVLC is probably waiting for the camera to answer"
                )
            }
            val startMs = SystemClock.elapsedRealtime()
            try {
                closed.player.stop()
            } finally {
                closed.player.release()
                closed.media.release()
                stillStopping.cancel()
            }
            val durationMs = SystemClock.elapsedRealtime() - startMs
            if (durationMs >= SLOW_RELEASE_MS) {
                Log.w(TAG, "Stopping a doorbell stream took $durationMs ms")
            }
        }
    }

    private companion object {
        // Same tag as before, so existing log filters keep working.
        const val TAG = "DoorbellScreen"

        // A stop and release this slow is logged; it can hold back the next attempt of the visit
        // (see Visit.reconnect()).
        const val SLOW_RELEASE_MS = 1_000L

        // A stop still running after this long is logged while it runs (see closeAttempt()).
        const val STILL_STOPPING_LOG_MS = 10_000L

        // Threads for stop() and release() (see releaseScope). A stop that does not hang is
        // usually over within a second, so only hung stops can take many of them.
        const val MAX_RELEASE_THREADS = 16
    }
}
