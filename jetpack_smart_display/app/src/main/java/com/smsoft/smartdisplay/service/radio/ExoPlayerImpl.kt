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

    /**
     * @param handleAudioFocus true for players that own playback (radio, alarm, timer).
     * Players used for short UI cues (wake-word / error chimes, message alerts) must pass
     * false: every ExoPlayer with audio-focus handling requests AUDIOFOCUS_GAIN, which makes
     * the radio player lose focus permanently and pause (it never resumes by itself).
     */
    fun getExoPlayer(
        context: Context,
        audioAttributes: AudioAttributes,
        handleAudioFocus: Boolean = true
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
            .setAudioAttributes(audioAttributes, handleAudioFocus)
            .setHandleAudioBecomingNoisy(handleAudioFocus)
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
