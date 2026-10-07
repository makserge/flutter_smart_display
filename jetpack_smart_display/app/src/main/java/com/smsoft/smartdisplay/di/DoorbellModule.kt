package com.smsoft.smartdisplay.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.videolan.libvlc.LibVLC
import javax.inject.Singleton

// Only LibVLC is app-wide: creating it loads the native libraries. The app-wide MediaPlayer that
// was provided here is gone; DoorbellStreamPlayer creates one for every connection attempt.
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
        // 32-bit colour for pictures that MediaCodec does not render straight to the surface
        // (software decoding, e.g. MJPEG, or H.265 without a hardware decoder). Without it
        // libVLC 3 asks for 16-bit RV16, which shows banding. Option and value are two
        // arguments, the way libVLC adds its RV16 default.
        add("--android-display-chroma")
        add("RV32")
    })
}