package com.smsoft.smartdisplay

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.MediaPlayer
import javax.inject.Inject

@HiltAndroidApp
class SmartDisplayApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var libVlc: LibVLC

    @Inject
    lateinit var mediaPlayer: MediaPlayer

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()

}