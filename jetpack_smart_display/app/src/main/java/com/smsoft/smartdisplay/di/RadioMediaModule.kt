package com.smsoft.smartdisplay.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.media3.common.AudioAttributes
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.service.radio.ExoPlayerImpl
import com.smsoft.smartdisplay.service.radio.RadioActiveState
import com.smsoft.smartdisplay.service.radio.RadioMediaServiceHandler
import com.smsoft.smartdisplay.service.radio.RadioPlayerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RadioMediaModule {

    @Provides
    @Singleton
    @UnstableApi
    fun provideAudioAttributes(): AudioAttributes = ExoPlayerImpl.getAudioAttributes()

    /**
     * There is no Player binding on purpose: a change of the radio type or the MPD server in
     * Settings replaces the radio player, and an injected Player would keep the old one. The
     * handler owns the player; the media session follows RadioMediaServiceHandler.currentPlayer.
     * The MediaSession is not provided here either: RadioMediaService owns it, because a released
     * session cannot be reused by the next service instance.
     */
    @Provides
    @Singleton
    @UnstableApi
    fun provideServiceHandler(
        playerFactory: RadioPlayerFactory,
        dataStore: DataStore<Preferences>,
        activeState: RadioActiveState,
        // The app-wide scope from AlarmModule.
        coroutineScope: CoroutineScope
    ): RadioMediaServiceHandler = RadioMediaServiceHandler(
            playerFactory = playerFactory,
            dataStore = dataStore,
            activeState = activeState,
            coroutineScope = coroutineScope
        )
}
