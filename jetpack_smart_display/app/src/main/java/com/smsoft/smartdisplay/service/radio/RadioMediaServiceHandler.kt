package com.smsoft.smartdisplay.service.radio

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.common.Player.MediaItemTransitionReason
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_IDLE
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class RadioMediaServiceHandler @Inject constructor(
    private val player: ExoPlayer
) : Listener {
    private val playerStateInt = MutableStateFlow(PlayerState(volume = player.volume))
    val playerState = playerStateInt.asStateFlow()

    private val mediaStateInt = MutableStateFlow<MediaState>(MediaState.Initial)
    val mediaState = mediaStateInt.asStateFlow()

    private val mediaMetadataInt = MutableStateFlow(MediaMetadata.Builder().build())
    val mediaMetadata = mediaMetadataInt.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    init {
        player.addListener(this)
    }

    fun addMediaItemList(mediaItemList: List<MediaItem>) {
        player.setMediaItems(mediaItemList)
    }

    fun mediaItemCount() : Int {
        return player.mediaItemCount
    }

    fun playItem(preset: Int) {
        val newPreset = if (preset > player.mediaItemCount) 1 else preset
        player.seekTo(newPreset, 0)
    }
    @UnstableApi
    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        player.apply {
            prepare()
            playWhenReady = true
        }
    }
    override fun onPlayerError(
        error: PlaybackException
    ) {
        Log.e("Radio", error.message!!)

        if ((error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED)
            || (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS)) {
            mediaStateInt.value = MediaState.Error
        }
    }
    fun onPlayerEvent(
        playerEvent: PlayerEvent
    ) {
        when (playerEvent) {
            PlayerEvent.Previous -> {
                player.seekToPreviousMediaItem()
            }
            PlayerEvent.Next -> {
                player.seekToNextMediaItem()
            }
            PlayerEvent.PlayPause -> {
                mediaStateInt.value = MediaState.Playing(isPlaying = player.isPlaying)
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }
            PlayerEvent.Play -> {
                if (!player.isPlaying) {
                    mediaStateInt.value = MediaState.Playing(isPlaying = player.isPlaying)
                    player.play()
                }
            }
            PlayerEvent.Pause -> {
                if (player.isPlaying) {
                    mediaStateInt.value = MediaState.Playing(isPlaying = player.isPlaying)
                    player.pause()
                } else {
                    mediaStateInt.value = MediaState.Ready(
                        player.duration,
                        player.currentMediaItem,
                        player.currentMediaItemIndex
                    )
                    player.stop()
                }
            }
            PlayerEvent.Stop -> {
                stopProgressUpdate()
                player.stop()
            }
            is PlayerEvent.UpdateProgress -> player.seekTo((player.duration * playerEvent.newProgress).toLong())
        }
    }

    override fun onPlaybackStateChanged(
        playbackState: Int
    ) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> mediaStateInt.value =
                MediaState.Buffering(player.currentPosition)
            ExoPlayer.STATE_READY -> mediaStateInt.value = MediaState.Ready(
                player.duration,
                player.currentMediaItem,
                player.currentMediaItemIndex
            )
            STATE_ENDED -> {}
            STATE_IDLE -> {}
        }
    }

    override fun onIsPlayingChanged(
        isPlaying: Boolean
    ) {
        mediaStateInt.value = MediaState.Playing(isPlaying = isPlaying)

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
    }

    override fun onMediaMetadataChanged(
        mediaMetadata: MediaMetadata
    ) {
        mediaMetadataInt.value = mediaMetadata
    }

    override fun onVolumeChanged(
        volume: Float
    ) {
        playerStateInt.value = PlayerState(
            volume = volume
        )
    }

    fun setVolume(
        value: Float
    ) {
        player.volume = value
    }
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

class PlayerState(val volume: Float)