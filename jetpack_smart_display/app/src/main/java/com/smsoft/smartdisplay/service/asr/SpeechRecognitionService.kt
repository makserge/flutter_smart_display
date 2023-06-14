package com.smsoft.smartdisplay.service.asr

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.utils.getForegroundNotification

@UnstableApi
class SpeechRecognitionService : Service() {
    private val STICKY_NOTIFICATION_ID = 77

    override fun onCreate() {
        super.onCreate()

        startForeground()
        initVosk()
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    private fun startForeground() {
        val notification = getForegroundNotification(
            context = this
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                STICKY_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(
                STICKY_NOTIFICATION_ID,
                notification
            )
        }
    }

    private fun initVosk() {
        //TODO("Not yet implemented")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}