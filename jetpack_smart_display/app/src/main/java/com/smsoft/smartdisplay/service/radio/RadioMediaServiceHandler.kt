package com.smsoft.smartdisplay.service.radio

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.common.Player.MediaItemTransitionReason
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_IDLE
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.data.RadioType
import com.smsoft.smartdisplay.utils.getRadioPreset
import com.smsoft.smartdisplay.utils.observe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

private const val TAG = "Radio"

/**
 * Single owner of radio playback. Everything that switches the radio on or off (the radio page,
 * voice commands, the media session) goes through the explicit operations below, so the
 * "radio is on" state stays consistent for both the local ExoPlayer and the MPD player.
 * It also owns the player and replaces it when the radio type or the MPD server changes in
 * Settings (see [switchPlayer]). All methods must be called on the main thread.
 */
@UnstableApi
class RadioMediaServiceHandler @Inject constructor(
    private val playerFactory: RadioPlayerFactory,
    private val dataStore: DataStore<Preferences>,
    activeState: RadioActiveState,
    // App-wide: follows the radio settings and runs the progress updates.
    private val coroutineScope: CoroutineScope
) : Listener {
    /**
     * The radio settings [player] was built from. Read blocking, as the Player binding did: the
     * handler is created on the first visit to the radio page, not at app start.
     */
    private var config = readRadioConfig(runBlocking { dataStore.data.first() })

    /** The radio player. Replaced by [switchPlayer], never reconfigured in place. */
    private var player: Player = playerFactory.create(config)

    private val currentPlayerInt = MutableStateFlow(player)
    /** The current radio player, for the media session; it changes with the radio settings. */
    val currentPlayer: StateFlow<Player> = currentPlayerInt.asStateFlow()

    /** False while the player is the MPD client; follows the radio type setting. */
    val isInternalPlayer: Boolean
        get() = player !is MPDPlayer

    /** The type of [player]: its station list, and so its saved station (getRadioPreset). */
    val radioType: RadioType
        get() = config.type

    private val playerStateInt = MutableStateFlow(PlayerState(volume = currentVolume()))
    val playerState = playerStateInt.asStateFlow()

    private val mediaStateInt = MutableStateFlow<MediaState>(MediaState.Initial)
    val mediaState = mediaStateInt.asStateFlow()

    private val mediaMetadataInt = MutableStateFlow(MediaMetadata.Builder().build())
    val mediaMetadata = mediaMetadataInt.asStateFlow()

    private val isActiveInt = activeState.state
    /** True while the radio is switched on: playing, buffering or starting a station. */
    val isActive = activeState.isActive

    private var job: Job? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    /** True after [stop]: late errors of the abandoned connection are ignored. */
    private var isStopped = true

    /**
     * Direction of a next/previous request made before the MPD queue is known (first use after
     * app start or after the radio settings changed): MPD is started and the step is done once
     * its queue arrives.
     */
    private var pendingStep = 0

    init {
        player.addListener(this)
        // One collector on the main thread, so a switch never overlaps another switch, a voice
        // command, a page event or a media-session command.
        dataStore.observe(coroutineScope + Dispatchers.Main.immediate, ::readRadioConfig) { newConfig ->
            // The first value is the one the player was built from.
            if (newConfig != config) {
                try {
                    switchPlayer(newConfig)
                } catch (e: Exception) {
                    // The old player is still in place, and a later change can still succeed.
                    Log.e(TAG, "Radio player not replaced", e)
                }
            }
        }
    }

    /**
     * Replaces the player after a change of the radio type or the MPD server. The old player is
     * stopped (an MPD server too) and released; the radio carries on with the new player if it
     * was on. A change made in Settings finds the radio off, because the radio page is not shown
     * then, so nothing connects while the user is still editing the server.
     */
    private fun switchPlayer(newConfig: RadioConfig) {
        // First, so that a failure changes nothing.
        val newPlayer = playerFactory.create(newConfig)
        val wasActive = isActiveInt.value
        // An MPD player sends its server a stop, which still runs after release().
        stop()
        val oldPlayer = player
        // Late events and errors of the old player are not ours any more.
        oldPlayer.removeListener(this)
        config = newConfig
        player = newPlayer
        newPlayer.addListener(this)
        currentPlayerInt.value = newPlayer
        mediaMetadataInt.value = MediaMetadata.EMPTY
        // UNKNOWN for MPD until it answers.
        updateVolume(currentVolume())
        // One main-loop turn later: the media session must not drive a released player, and
        // RadioMediaService has moved it to the new one (or released it, for MPD) by then: its
        // collector was resumed through Dispatchers.Main, i.e. queued before this message.
        mainHandler.post {
            oldPlayer.release()
        }
        Log.i(TAG, "Radio player replaced: ${newConfig.type}, radio was on: $wasActive")
        if (wasActive) {
            // The radio only plays while its page is shown: carry on, as on entering the page,
            // with the station saved for the new type's list.
            playItem(getRadioPreset(dataStore, newConfig.type))
        }
    }

    fun mediaItemCount() : Int {
        return player.mediaItemCount
    }

    /**
     * Starts [preset] from the live edge. Out-of-range presets fall back to the first station.
     * Before the MPD queue is known, MPD resumes its own current song.
     */
    fun playItem(preset: Int) {
        val count = player.mediaItemCount
        markActive()
        if (count > 0) {
            player.seekTo(if (preset in 0 until count) preset else 0, C.TIME_UNSET)
        }
        player.prepare()
        player.playWhenReady = true
    }

    /** Switches the radio on with [preset] unless it is already on. A playing stream is not restarted. */
    fun ensurePlaying(preset: Int) {
        when {
            !isActiveInt.value -> playItem(preset)
            player.playbackState.isIdleOrEnded() -> resume()
            else -> {}
        }
    }

    /** Resumes after a pause. A stopped or failed player is prepared again first. */
    fun resume() {
        if (player.playbackState.isIdleOrEnded()) {
            player.seekToDefaultPosition()
            player.prepare()
        }
        player.play()
        markActive()
    }

    fun pause() {
        isActiveInt.value = false
        player.pause()
    }

    /** Switches the radio off and drops the stream connection. Safe to call when it is already off. */
    fun stop() {
        isActiveInt.value = false
        isStopped = true
        pendingStep = 0
        stopProgressUpdate()
        player.stop()
        mediaStateInt.value = MediaState.Playing(isPlaying = false)
    }

    /** Next station, wrapping around. [fallbackPreset] is the base station when the radio is off. */
    fun next(fallbackPreset: Int) = step(+1, fallbackPreset)

    /** Previous station, wrapping around. [fallbackPreset] is the base station when the radio is off. */
    fun previous(fallbackPreset: Int) = step(-1, fallbackPreset)

    private fun step(direction: Int, fallbackPreset: Int) {
        val count = player.mediaItemCount
        if (count == 0) {
            if (!isInternalPlayer) {
                // MPD before its queue is known: start it, step once the queue arrives. A queue
                // already known to be empty (ENDED) has nothing to step to, and the step must not
                // fire later, unasked, when another MPD client fills it.
                if (player.playbackState != STATE_ENDED) {
                    pendingStep = direction
                }
                markActive()
                player.prepare()
                player.playWhenReady = true
            }
            return
        }
        val base = if (isActiveInt.value) player.currentMediaItemIndex else fallbackPreset
        playItem((base + direction).mod(count))
    }

    fun onPlayerEvent(
        playerEvent: PlayerEvent
    ) {
        when (playerEvent) {
            PlayerEvent.Previous -> previous(player.currentMediaItemIndex)
            PlayerEvent.Next -> next(player.currentMediaItemIndex)
            PlayerEvent.PlayPause -> if (player.isPlaying) pause() else resume()
            PlayerEvent.Play -> resume()
            PlayerEvent.Pause -> pause()
            PlayerEvent.Stop -> stop()
            is PlayerEvent.UpdateProgress -> player.seekTo((player.duration * playerEvent.newProgress).toLong())
        }
    }

    private fun markActive() {
        isActiveInt.value = true
        isStopped = false
    }

    override fun onPlayerError(
        error: PlaybackException
    ) {
        Log.e(TAG, "Playback error: ${error.errorCodeName}", error)
        isActiveInt.value = false
        // A next/previous waiting for the MPD queue must not fire later, unasked.
        pendingStep = 0
        if (isStopped) {
            // Late error from a connection that was already given up (radio switched off).
            return
        }

        if (isInternalPlayer) {
            // A dead or unreachable station (e.g. HTTP 404) just stops the radio; it used to open
            // Settings, which cannot fix a stream URL. The user can pick another station.
            mediaStateInt.value = MediaState.Playing(isPlaying = false)
            return
        }
        // MPD: the server is unreachable or misconfigured, so the radio page opens Settings.
        if ((error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED)
            || (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS)) {
            mediaStateInt.value = MediaState.Error
        }
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        // Covers pauses that do not come from this class: permanent audio-focus loss (an alarm),
        // headphones unplugged, or Pause pressed in the media notification.
        if (!playWhenReady) {
            isActiveInt.value = false
        } else if (!player.playbackState.isIdleOrEnded()) {
            isActiveInt.value = true
        }
    }

    override fun onPlaybackStateChanged(
        playbackState: Int
    ) {
        when (playbackState) {
            Player.STATE_BUFFERING -> mediaStateInt.value =
                MediaState.Buffering(player.currentPosition)
            Player.STATE_READY -> emitReady()
            // A stop or error that bypassed stop() (e.g. a media key) also switches the radio off.
            STATE_ENDED,
            STATE_IDLE -> {
                isActiveInt.value = false
                pendingStep = 0
            }
        }
    }

    private fun emitReady() {
        mediaStateInt.value = MediaState.Ready(
            player.duration,
            player.currentMediaItem,
            player.currentMediaItemIndex
        )
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        if ((pendingStep != 0) && !timeline.isEmpty) {
            val direction = pendingStep
            pendingStep = 0
            playItem((player.currentMediaItemIndex + direction).mod(timeline.windowCount))
        }
    }

    override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
        // MPD without a mixer has no volume; it may get one later.
        updateVolume(currentVolume())
    }

    override fun onIsPlayingChanged(
        isPlaying: Boolean
    ) {
        mediaStateInt.value = MediaState.Playing(isPlaying = isPlaying)
        if (isPlaying) {
            isActiveInt.value = true
        }

        stopProgressUpdate()
        if (isPlaying) {
            job = coroutineScope.launch(Dispatchers.Main) {
                var progress = player.currentPosition
                while (true) {
                    mediaStateInt.value = MediaState.Progress(progress)
                    progress += 500
                    delay(500)
                }
            }
        }
    }

    private fun stopProgressUpdate() {
        job?.cancel()
    }

    override fun onMediaItemTransition(
        mediaItem: MediaItem?,
        reason: @MediaItemTransitionReason Int
    ) {
        mediaMetadataInt.value = MediaMetadata.EMPTY
        if (player.playbackState == Player.STATE_READY) {
            // Station changed by another MPD client: no BUFFERING/READY round trip follows.
            emitReady()
        }
    }

    override fun onMediaMetadataChanged(
        mediaMetadata: MediaMetadata
    ) {
        mediaMetadataInt.value = mediaMetadata
    }

    override fun onVolumeChanged(
        volume: Float
    ) {
        updateVolume(currentVolume())
    }

    /** The volume of [player] as the radio page shows it; see [volumePosition]. */
    private fun currentVolume(): Float = volumePosition(player)

    /**
     * The volume of [radioPlayer] as a slider position 0..1 that is even in loudness
     * ([RadioVolume]), or [RadioVolume.UNKNOWN] while it has no volume (MPD without a mixer, or
     * before MPD's first answer): the radio page then waits for one.
     */
    fun volumePosition(radioPlayer: Player): Float =
        if (radioPlayer.isCommandAvailable(Player.COMMAND_GET_VOLUME)) {
            RadioVolume.toPosition(radioPlayer.volume, isInternal = radioPlayer !is MPDPlayer)
        } else {
            RadioVolume.UNKNOWN
        }

    private fun updateVolume(volume: Float) {
        if (volume != playerStateInt.value.volume) {
            playerStateInt.value = PlayerState(volume = volume)
        }
    }

    /**
     * Sets the volume to the slider [position] 0..1 ([RadioVolume]): through the loudness curve
     * for the internal player, as MPD's percent for MPD. Player.volume stays the raw value.
     */
    fun setVolume(
        position: Float
    ) {
        player.volume = RadioVolume.toPlayerVolume(position, isInternalPlayer)
    }

    /** The UI has shown the error (it opened Settings); do not show it again on the next visit. */
    fun clearError() {
        if (mediaStateInt.value == MediaState.Error) {
            mediaStateInt.value = MediaState.Playing(isPlaying = false)
        }
    }

    private fun Int.isIdleOrEnded() = (this == STATE_IDLE) || (this == STATE_ENDED)
}

sealed class PlayerEvent {
    data object Play : PlayerEvent()
    data object Pause : PlayerEvent()
    data object PlayPause : PlayerEvent()
    data object Previous : PlayerEvent()
    data object Next : PlayerEvent()
    data object Stop : PlayerEvent()
    data class UpdateProgress(val newProgress: Float) : PlayerEvent()
}

sealed class MediaState {
    data object Initial : MediaState()
    data object Error: MediaState()
    data class Ready(val duration: Long, val currentMediaItem: MediaItem?, val currentMediaItemIndex: Int) : MediaState()
    data class Progress(val progress: Long) : MediaState()
    data class Buffering(val progress: Long) : MediaState()
    data class Playing(val isPlaying: Boolean) : MediaState()
}

/** [volume]: the slider position 0..1 ([RadioVolume]), [RadioVolume.UNKNOWN] without a volume. */
class PlayerState(val volume: Float)
