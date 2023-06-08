package com.smsoft.smartdisplay.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.MediaPlayer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DoorbellModule {

    @Provides
    @Singleton
    fun provideVlC(
        @ApplicationContext context: Context
    ): LibVLC = LibVLC(context, ArrayList<String>().apply {
        add("--rtsp-tcp")
        add("--verbose=-1")
    })

    @Provides
    @Singleton
    fun provideVlCPlayer(
        libVlc: LibVLC
    ) = MediaPlayer(libVlc)
}