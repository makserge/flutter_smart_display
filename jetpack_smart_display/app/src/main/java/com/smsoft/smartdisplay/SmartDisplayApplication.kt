package com.smsoft.smartdisplay

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.media3.common.util.UnstableApi
import androidx.work.Configuration
import com.smsoft.smartdisplay.service.alarm.AlarmHandler
import com.smsoft.smartdisplay.service.clock.DailyClockChanger
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import javax.inject.Inject

@UnstableApi
@HiltAndroidApp
class SmartDisplayApplication : Application(), Configuration.Provider {

    // Still created at startup, so a ring does not wait for it, but on a background thread:
    // loading libVLC's native libraries (libvlc.so is 40 to 54 MB) ran on the main thread here. A
    // doorbell that plays earlier waits for it (Dagger creates the singleton once, under a lock).
    // The app-wide MediaPlayer that was injected here is gone (see DoorbellStreamPlayer).
    @Inject
    lateinit var libVlc: Lazy<LibVLC>

    @Inject
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var alarmHandler: AlarmHandler

    @Inject
    lateinit var dailyClockChanger: DailyClockChanger

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()

    @UnstableApi
    override fun onCreate() {
        super.onCreate()

        alarmHandler.rescheduleAlarms()
        dailyClockChanger.start()
        coroutineScope.launch {
            try {
                libVlc.get()
            } catch (e: IllegalStateException) {
                // Only the doorbell video needs it; the doorbell logs this again.
                Log.e(TAG, "libVLC could not be created", e)
            }
        }
    }

    private companion object {
        const val TAG = "SmartDisplayApplication"
    }
}