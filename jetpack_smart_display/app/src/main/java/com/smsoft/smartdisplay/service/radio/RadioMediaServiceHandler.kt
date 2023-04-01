package com.smsoft.smartdisplay.service.radio

import android.util.Log
import androidx.media3.common.*
import androidx.media3.common.Player.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun addMediaItemList(mediaItemList: List<MediaItem>, preset: Int) {
        player.apply {
            setMediaItems(mediaItemList)
            seekTo(preset, 0)
        }
    }
    @Deprecated("Deprecated in Java")
    @UnstableApi
    override fun onSeekProcessed() {
        player.apply {
            prepare()
            playWhenReady = true
        }
    }
    override fun onPlayerError(error: PlaybackException) {
        Log.e("Radio", error.message!!)

        if (error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED) {
            mediaStateInt.value = MediaState.Error
        }
    }
    fun onPlayerEvent(playerEvent: PlayerEvent) {
        when (playerEvent) {
            PlayerEvent.Previous -> player.seekToPreviousMediaItem()
            PlayerEvent.Next -> player.seekToNextMediaItem()
            PlayerEvent.PlayPause -> {
                mediaStateInt.value = MediaState.Playing(isPlaying = player.isPlaying)
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }
            PlayerEvent.Stop -> {
                stopProgressUpdate()
                player.stop()
            }
            is PlayerEvent.UpdateProgress -> player.seekTo((player.duration * playerEvent.newProgress).toLong())
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
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

    override fun onIsPlayingChanged(isPlaying: Boolean) {
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

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: @MediaItemTransitionReason Int) {
        mediaMetadataInt.value = MediaMetadata.EMPTY
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        mediaMetadataInt.value = mediaMetadata
    }

    override fun onVolumeChanged(volume: Float) {
        playerStateInt.value = PlayerState(
            volume = volume
        )
    }

    fun setVolume(value: Float) {
        player.volume = value
    }
}

sealed class PlayerEvent {
    object PlayPause : PlayerEvent()
    object Previous : PlayerEvent()
    object Next : PlayerEvent()
    object Stop : PlayerEvent()
    data class UpdateProgress(val newProgress: Float) : PlayerEvent()
}

sealed class MediaState {
    object Initial : MediaState()
    object Error: MediaState()
    data class Ready(val duration: Long, val currentMediaItem: MediaItem?, val currentMediaItemIndex: Int) : MediaState()
    data class Progress(val progress: Long) : MediaState()
    data class Buffering(val progress: Long) : MediaState()
    data class Playing(val isPlaying: Boolean) : MediaState()
}

class PlayerState(val volume: Float)