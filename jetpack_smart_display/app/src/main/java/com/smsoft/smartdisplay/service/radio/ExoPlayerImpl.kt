package com.smsoft.smartdisplay.service.radio

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import java.util.concurrent.TimeUnit

@UnstableApi
object ExoPlayerImpl {
    private const val BUFFER_SIZE = 2L //in minutes
    private const val BUFFER_FOR_PLAYBACK_MS = 1000

    fun getExoPlayer(
        context: Context,
        audioAttributes: AudioAttributes
    ) : ExoPlayer {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                TimeUnit.SECONDS.toMillis(100).toInt(),
                TimeUnit.SECONDS.toMillis(1000).toInt(),
                BUFFER_FOR_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
            )
            .setBackBuffer(
                TimeUnit.MINUTES.toMillis(BUFFER_SIZE).toInt(),
                true
            )
            .build()
        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setTrackSelector(DefaultTrackSelector(context))
            .setLoadControl(loadControl)
            .build()
    }

    fun getAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    }
}