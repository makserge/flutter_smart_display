package com.smsoft.smartdisplay.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.media3.common.AudioAttributes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.smsoft.smartdisplay.data.RadioType
import com.smsoft.smartdisplay.service.notification.RadioMediaNotificationManager
import com.smsoft.smartdisplay.service.radio.ExoPlayerImpl
import com.smsoft.smartdisplay.service.radio.MPDPlayer
import com.smsoft.smartdisplay.service.radio.RadioMediaServiceHandler
import com.smsoft.smartdisplay.utils.getRadioType
import com.smsoft.smartdisplay.utils.mpd.MPDHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RadioMediaModule {

    @Provides
    @Singleton
    @UnstableApi
    fun provideAudioAttributes(): AudioAttributes = ExoPlayerImpl.getAudioAttributes()

    @Provides
    @Singleton
    fun provideMPDHelper(): MPDHelper = MPDHelper()

    @Provides
    @Singleton
    @UnstableApi
    fun providePlayer(
        @ApplicationContext context: Context,
        dataStore: DataStore<Preferences>,
        audioAttributes: AudioAttributes,
        mpdHelper: MPDHelper
    ): ExoPlayer {

        return when (getRadioType(dataStore)) {
            RadioType.INTERNAL -> ExoPlayerImpl.getExoPlayer(
                context = context,
                audioAttributes = audioAttributes
            )
            else -> MPDPlayer(
                dataStore = dataStore,
                helper = mpdHelper
            )
        }
    }

    @Provides
    @Singleton
    @UnstableApi
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