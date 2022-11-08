package com.smsoft.smartdisplay.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import com.smsoft.smartdisplay.service.RadioMediaServiceHandler
import com.smsoft.smartdisplay.service.notification.RadioMediaNotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RadioMediaModule {
    private val BUFFER_SIZE = 2L //in minutes
    private val BUFFER_FOR_PLAYBACK_MS = 1000

    @Provides
    @Singleton
    fun provideAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

    @Provides
    @Singleton
    fun providePlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ): ExoPlayer {
        val minBufferMs = TimeUnit.SECONDS.toMillis(100).toInt()
        val maxBufferMs = TimeUnit.SECONDS.toMillis(1000).toInt()
        val bufferForPlaybackMs = BUFFER_FOR_PLAYBACK_MS
        val bufferAfterResumeMs = DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                minBufferMs,
                maxBufferMs,
                bufferForPlaybackMs,
                bufferAfterResumeMs
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

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context,
        player: ExoPlayer
    ): RadioMediaNotificationManager = RadioMediaNotificationManager(
            context = context,
            player = player
        )

    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context,
        player: ExoPlayer
    ): MediaSession = MediaSession.Builder(context, player).build()

    @Provides
    @Singleton
    fun provideServiceHandler(
        player: ExoPlayer
    ): RadioMediaServiceHandler = RadioMediaServiceHandler(
            player = player
        )
}