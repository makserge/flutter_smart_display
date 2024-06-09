package com.smsoft.smartdisplay

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.media3.common.util.UnstableApi
import androidx.work.Configuration
import com.smsoft.smartdisplay.service.alarm.AlarmHandler
import dagger.hilt.android.HiltAndroidApp
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.MediaPlayer
import javax.inject.Inject

@UnstableApi
@HiltAndroidApp
class SmartDisplayApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var libVlc: LibVLC

    @Inject
    lateinit var mediaPlayer: MediaPlayer

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var alarmHandler: AlarmHandler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()

    @UnstableApi
    override fun onCreate() {
        super.onCreate()

        alarmHandler.rescheduleAlarms()
    }
}