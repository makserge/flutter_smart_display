package com.smsoft.smartdisplay.service.radio

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.smsoft.smartdisplay.utils.mpd.MPDHelper
import com.smsoft.smartdisplay.utils.mpd.MPDStatusMonitor
import com.smsoft.smartdisplay.utils.mpd.MpdAckException
import com.smsoft.smartdisplay.utils.mpd.MpdConnectionException
import com.smsoft.smartdisplay.utils.mpd.MpdSetupException
import com.smsoft.smartdisplay.utils.mpd.MpdSnapshot
import com.smsoft.smartdisplay.utils.mpd.MpdSong
import com.smsoft.smartdisplay.utils.mpd.MpdTimeoutException
import com.smsoft.smartdisplay.utils.mpd.data.MPDCredentials
import com.smsoft.smartdisplay.utils.mpd.data.MPDState
import com.smsoft.smartdisplay.utils.mpd.data.MPDStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

private const val TAG = "MPDPlayer"
private const val NO_GENERATION = -1

/**
 * The radio player for RadioType.MPD: a Media3 Player whose output is a Music Player Daemon.
 *
 * The playlist is MPD's queue (playlistid), the current item MPD's current song, playWhenReady
 * MPD's play state. Commands go to MPD over one connection on one MPD thread, in call order;
 * an [MPDStatusMonitor] reports changes made by MPD itself or by other clients while prepared.
 *
 * Threading: like every Player, it is used on the main (application) thread only. The model
 * below is main-thread confined; MPD answers are applied to it on the main thread before the
 * operation's future completes, so getState() always sees them.
 */
@UnstableApi
class MPDPlayer(
    private val credentials: MPDCredentials,
    private val session: MPDHelper = MPDHelper(credentials)
) : SimpleBasePlayer(Looper.getMainLooper()) {

    private val mainHandler = Handler(Looper.getMainLooper())

    // One MPD operation at a time, in call order: a stop() issued when the radio page is left
    // finishes before the prepare() of the next visit. Every step has a timeout (Connection), so
    // a hung server delays the queue by seconds, not forever.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    // ---- Model: main thread only. ----
    private var isPrepared = false
    /** MPD answered since prepare() and the monitor has not lost it since. */
    private var isConnected = false
    /** The queue was fetched at least once; an empty queue then means "nothing to play". */
    private var hasQueue = false
    private var queue: List<MpdSong> = emptyList()
    private var items: List<MediaItemData> = emptyList()
    private var status: MPDStatus? = null
    private var currentSong: MpdSong? = null
    private var playWhenReady = false
    private var playWhenReadyReason = Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST
    /** A station picked while MPD is not playing; MPD gets it with the next play. */
    private var pendingIndex = C.INDEX_UNSET
    private var error: PlaybackException? = null
    private var monitor: MPDStatusMonitor? = null
    private var isReleasedModel = false

    /** Incremented by prepare, stop and errors: answers to older requests are dropped. */
    // Bumped by prepare, stop and errors (not by release); operations from an older generation
    // are skipped. Changed from the main and the MPD thread.
    private val generation = AtomicInteger(0)

    /** Monitor refresh requests not yet run: [REFRESH_STATUS] or [REFRESH_QUEUE] bits. */
    private val refreshRequest = AtomicInteger(0)

    override fun getState(): State {
        val currentIndex = when {
            items.isEmpty() -> C.INDEX_UNSET
            pendingIndex != C.INDEX_UNSET -> pendingIndex.coerceIn(0, items.lastIndex)
            else -> (status?.songPos ?: 0).coerceIn(0, items.lastIndex)
        }
        val playbackState = when {
            !isPrepared || (error != null) -> Player.STATE_IDLE
            items.isEmpty() -> if (hasQueue) Player.STATE_ENDED else Player.STATE_IDLE
            !isConnected -> Player.STATE_BUFFERING
            else -> Player.STATE_READY
        }
        val volume = status?.volume?.takeIf { it >= 0 }?.coerceAtMost(100)
        return State.Builder()
            .setAvailableCommands(commands(hasMixer = volume != null))
            .setPlaylist(items)
            .setCurrentMediaItemIndex(currentIndex)
            .setPlayWhenReady(playWhenReady, playWhenReadyReason)
            .setPlaybackState(playbackState)
            .setPlayerError(if (playbackState == Player.STATE_IDLE) error else null)
            .setVolume((volume ?: 100) / 100F)
            .setContentPositionMs(status?.elapsedMs ?: 0L)
            .build()
    }

    /** prepare() with a still unknown queue would show STATE_ENDED until MPD answered. */
    override fun getPlaceholderState(suggestedPlaceholderState: State): State =
        if ((suggestedPlaceholderState.playbackState == Player.STATE_ENDED) && !hasQueue) {
            suggestedPlaceholderState.buildUpon().setPlaybackState(Player.STATE_IDLE).build()
        } else {
            suggestedPlaceholderState
        }

    override fun handlePrepare(): ListenableFuture<*> {
        if (isPrepared && (error == null)) {
            return Futures.immediateVoidFuture()
        }
        isPrepared = true
        isConnected = false
        error = null
        val gen = generation.incrementAndGet()
        startMonitor(gen)
        val start = playWhenReady
        val songId = if (start) takePendingSongId() else null
        return runOnMpd(gen) { mpd ->
            if (start) {
                if (songId != null) mpd.playId(songId) else mpd.play()
            }
            mpd.fetch(includeQueue = true)
        }
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<*> {
        this.playWhenReady = playWhenReady
        playWhenReadyReason = Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST
        if (!isPrepared) {
            // Remembered; prepare() starts MPD.
            return Futures.immediateVoidFuture()
        }
        val songId = if (playWhenReady) takePendingSongId() else null
        return runOnMpd(generation.get()) { mpd ->
            when {
                !playWhenReady -> mpd.pause()
                songId != null -> mpd.playId(songId)
                else -> mpd.play()
            }
            mpd.fetch(includeQueue = false)
        }
    }

    override fun handleSeek(mediaItemIndex: Int, positionMs: Long, seekCommand: Int): ListenableFuture<*> {
        // Unknown queue (before the first answer): MPD keeps its own current song.
        val song = queue.getOrNull(mediaItemIndex) ?: return Futures.immediateVoidFuture()
        if (!isPrepared || !playWhenReady) {
            // "playid" would start playback: keep the choice for the next play.
            pendingIndex = mediaItemIndex
            return Futures.immediateVoidFuture()
        }
        pendingIndex = C.INDEX_UNSET
        return runOnMpd(generation.get()) { mpd ->
            mpd.playId(song.id)
            mpd.fetch(includeQueue = false)
        }
    }

    override fun handleStop(): ListenableFuture<*> {
        // A session that ended in an error may have left MPD playing (fail() clears isPrepared),
        // so it is stopped as well; otherwise the panel could not switch MPD off any more.
        val wasPrepared = isPrepared || (error != null)
        isPrepared = false
        isConnected = false
        error = null
        val gen = generation.incrementAndGet()
        stopMonitor()
        // Best effort and silent: an unreachable server must not raise a player error while the
        // radio page is being left.
        return runOnMpd(gen, reportErrors = false) { mpd ->
            try {
                if (wasPrepared) {
                    mpd.stop()
                }
            } finally {
                mpd.disconnect()
            }
            null
        }
    }

    override fun handleSetVolume(volume: Float, volumeOperationType: Int): ListenableFuture<*> {
        val value = (volume * 100).roundToInt()
        // Volume also works while the radio is off (the voice command does that); a failure is
        // only an error while the radio plays.
        return runOnMpd(generation.get(), reportErrors = isPrepared) { mpd ->
            mpd.setVolume(value)
            mpd.fetch(includeQueue = false)
        }
    }

    override fun handleRelease(): ListenableFuture<*> {
        isReleasedModel = true
        stopMonitor()
        // No generation bump: operations already queued still run, in particular the stop() that
        // comes before every release (RadioMediaServiceHandler.switchPlayer), so a player replaced
        // while MPD plays does not leave that server playing. Their answers are dropped
        // (isReleasedModel); then the connection is closed and the MPD thread ends.
        scope.launch {
            session.disconnect()
        }.invokeOnCompletion {
            scope.cancel()
        }
        return Futures.immediateVoidFuture()
    }

    /**
     * Runs [operation] on the MPD thread. Its answer, or its error, is applied to the model on
     * the main thread, unless a later prepare/stop made it obsolete; then the future completes
     * and SimpleBasePlayer reads the new state.
     */
    private fun runOnMpd(
        gen: Int,
        reportErrors: Boolean = true,
        operation: (MPDHelper) -> MpdSnapshot?
    ): ListenableFuture<*> {
        val future = SettableFuture.create<Unit>()
        scope.launch {
            var snapshot: MpdSnapshot? = null
            var failure: Exception? = null
            // Made obsolete while it waited (stop, error): do not send it, MPD would act on it.
            if (gen == generation.get()) {
                try {
                    snapshot = operation(session)
                } catch (e: Exception) {
                    failure = e
                }
            }
            // A failed command ends the session at once: commands queued behind it are skipped
            // instead of each waiting for its own timeout.
            val failedGen = if ((failure != null) && reportErrors &&
                generation.compareAndSet(gen, gen + 1)) gen + 1 else NO_GENERATION
            mainHandler.post {
                if (!isReleasedModel) {
                    when {
                        failedGen == generation.get() -> fail(failure!!)
                        gen != generation.get() -> Unit
                        failure == null -> snapshot?.let { apply(it) }
                        else -> Log.w(TAG, "MPD request failed: ${failure.message}")
                    }
                }
                future.set(Unit)
            }
        }
        return future
    }

    private fun apply(snapshot: MpdSnapshot) {
        status = snapshot.status
        currentSong = snapshot.currentSong
        snapshot.queue?.let {
            // SimpleBasePlayer rejects duplicate uids (IllegalArgumentException in getState()).
            queue = it.distinctBy { song -> song.id }
            hasQueue = true
        }
        if (pendingIndex >= queue.size) {
            pendingIndex = C.INDEX_UNSET
        }
        if (isPrepared) {
            isConnected = true
            // MPD is the truth: a pause, stop or play by MPD itself or by another client.
            val isPlaying = snapshot.status.state == MPDState.PLAYING
            if (isPlaying != playWhenReady) {
                playWhenReady = isPlaying
                playWhenReadyReason = Player.PLAY_WHEN_READY_CHANGE_REASON_REMOTE
            }
        }
        items = queue.map { song ->
            val playing = currentSong?.takeIf { it.id == song.id }
            MediaItemData.Builder(song.uid)
                .setMediaItem(song.toMediaItem())
                .setMediaMetadata(playing?.toPlayingMetadata())
                .setIsDynamic(true)
                .setIsSeekable(false)
                .build()
        }
        Log.i(TAG, "MPD state=${snapshot.status.state} song=${snapshot.status.songPos} " +
                "volume=${snapshot.status.volume} queue=${queue.size} prepared=$isPrepared")
    }

    private fun fail(e: Exception) {
        Log.w(TAG, "MPD request failed", e)
        isPrepared = false
        isConnected = false
        error = toPlaybackException(e)
        generation.incrementAndGet()
        stopMonitor()
        scope.launch {
            session.disconnect()
        }
    }

    /**
     * CONNECTION_FAILED makes the radio page open Settings (see RadioMediaServiceHandler); it is
     * used for what Settings can fix: wrong host, port or password, server not reachable.
     */
    private fun toPlaybackException(e: Exception): PlaybackException = when (e) {
        is MpdTimeoutException -> PlaybackException(
            "MPD did not answer", e, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT
        )
        is MpdConnectionException, is MpdSetupException -> PlaybackException(
            "MPD connection failed", e, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
        )
        is MpdAckException -> PlaybackException(
            "MPD refused a command", e, PlaybackException.ERROR_CODE_REMOTE_ERROR
        )
        else -> PlaybackException("MPD command failed", e, PlaybackException.ERROR_CODE_REMOTE_ERROR)
    }

    private fun takePendingSongId(): Int? {
        val songId = queue.getOrNull(pendingIndex)?.id
        pendingIndex = C.INDEX_UNSET
        return songId
    }

    private fun startMonitor(gen: Int) {
        stopMonitor()
        monitor = MPDStatusMonitor(credentials, object : MPDStatusMonitor.Listener {
            override fun onConnectionChanged(isConnected: Boolean) {
                if (isConnected) {
                    requestRefresh(gen, includeQueue = true)
                } else {
                    mainHandler.post {
                        if (!isReleasedModel && (gen == generation.get()) && isPrepared) {
                            this@MPDPlayer.isConnected = false
                            invalidateState()
                        }
                    }
                }
            }

            override fun onChanged(subsystems: Set<String>) {
                requestRefresh(gen, includeQueue = "playlist" in subsystems)
            }
        }).also {
            it.start()
        }
    }

    private fun stopMonitor() {
        monitor?.stop()
        monitor = null
    }

    /** Monitor thread. Requests that arrive while one is queued are merged into it. */
    private fun requestRefresh(gen: Int, includeQueue: Boolean) {
        val flags = if (includeQueue) REFRESH_QUEUE else REFRESH_STATUS
        if (refreshRequest.getAndUpdate { it or flags } != 0) {
            return
        }
        scope.launch {
            val requested = refreshRequest.getAndSet(0)
            if (gen != generation.get()) {
                return@launch
            }
            val snapshot = try {
                session.fetch(includeQueue = (requested and REFRESH_QUEUE) == REFRESH_QUEUE)
            } catch (e: Exception) {
                // The monitor notices a server that is gone; a refresh error alone is no reason
                // to stop the radio.
                Log.w(TAG, "MPD refresh failed: ${e.message}")
                null
            }
            mainHandler.post {
                if ((snapshot != null) && !isReleasedModel && (gen == generation.get()) && isPrepared) {
                    apply(snapshot)
                    invalidateState()
                }
            }
        }
    }

    private fun commands(hasMixer: Boolean): Player.Commands = Player.Commands.Builder()
        .addAll(
            Player.COMMAND_PLAY_PAUSE,
            Player.COMMAND_PREPARE,
            Player.COMMAND_STOP,
            Player.COMMAND_RELEASE,
            Player.COMMAND_SEEK_TO_DEFAULT_POSITION,
            Player.COMMAND_SEEK_TO_MEDIA_ITEM,
            Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
            Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
            Player.COMMAND_SEEK_TO_NEXT,
            Player.COMMAND_SEEK_TO_PREVIOUS,
            Player.COMMAND_GET_CURRENT_MEDIA_ITEM,
            Player.COMMAND_GET_TIMELINE,
            Player.COMMAND_GET_METADATA
        )
        // Without a mixer MPD reports no volume; the radio page then hides the volume value.
        .addIf(Player.COMMAND_GET_VOLUME, hasMixer)
        .addIf(Player.COMMAND_SET_VOLUME, hasMixer)
        .build()
}

private const val REFRESH_STATUS = 1
private const val REFRESH_QUEUE = 3
