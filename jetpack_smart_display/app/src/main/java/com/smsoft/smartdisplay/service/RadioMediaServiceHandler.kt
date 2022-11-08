package com.smsoft.smartdisplay.service

import android.util.Log
import androidx.annotation.Nullable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Player.MediaItemTransitionReason
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class RadioMediaServiceHandler @Inject constructor(
    private val player: ExoPlayer
) : Player.Listener {
    private val FADE_IN_DELAY = 200L //2s fade in

    private val mediaStateInt = MutableStateFlow<MediaState>(MediaState.Initial)
    val mediaState = mediaStateInt.asStateFlow()

    private val mediaMetadataInt = MutableStateFlow(MediaMetadata.Builder().build())
    val mediaMetadata = mediaMetadataInt.asStateFlow()

    private var job: Job? = null

    init {
        player.volume = 0F
        player.addListener(this)
        job = Job()
    }

    fun addMediaItemList(mediaItemList: List<MediaItem>) {
        player.apply {
            setMediaItems(mediaItemList)
            prepare()
        }
    }

    suspend fun onPlayerEvent(playerEvent: PlayerEvent) {
        when (playerEvent) {
            PlayerEvent.Previous -> player.seekToPreviousMediaItem()
            PlayerEvent.Next -> player. seekToNextMediaItem()
            PlayerEvent.PlayPause -> {
                if (player.isPlaying) {
                    player.pause()
                    stopProgressUpdate()
                } else {
                    player.play()
                    mediaStateInt.value = MediaState.Playing(isPlaying = true)
                    startProgressUpdate()
                }
            }
            PlayerEvent.Stop -> stopProgressUpdate()
            is PlayerEvent.UpdateProgress -> player.seekTo((player.duration * playerEvent.newProgress).toLong())
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> mediaStateInt.value = MediaState.Buffering(player.currentPosition)
            ExoPlayer.STATE_READY -> mediaStateInt.value = MediaState.Ready(player.duration, player.currentMediaItem, player.currentMediaItemIndex)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        mediaStateInt.value = MediaState.Playing(isPlaying = isPlaying)
        if (isPlaying) {
            GlobalScope.launch(Dispatchers.Main) {
                startProgressUpdate()
            }
        } else {
            stopProgressUpdate()
        }
    }

    override fun onMediaItemTransition(@Nullable mediaItem: MediaItem?, reason: @MediaItemTransitionReason Int) {
        Log.d("", player.currentMediaItem!!.mediaId)
        mediaMetadataInt.value = MediaMetadata.EMPTY
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        mediaMetadataInt.value = mediaMetadata
    }

    private suspend fun startProgressUpdate() = job.run {
        while (true) {
            delay(500)
            mediaStateInt.value = MediaState.Progress(player.currentPosition)
        }
    }

    private fun stopProgressUpdate() {
        job?.cancel()
        mediaStateInt.value = MediaState.Playing(isPlaying = false)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun fadeInVolume() {
        player.volume = 0F
        GlobalScope.launch(Dispatchers.Main) {
            repeat(10) {
                delay(FADE_IN_DELAY)
                player.volume += 0.1F
            }
        }
    }

    fun playPlaylistItem(position: Int) {
        player.apply {
            seekTo(position, 0)
            playWhenReady = true
        }
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
    data class Ready(val duration: Long, val currentMediaItem: MediaItem?, val currentMediaItemIndex: Int) : MediaState()
    data class Progress(val progress: Long) : MediaState()
    data class Buffering(val progress: Long) : MediaState()
    data class Playing(val isPlaying: Boolean) : MediaState()
}