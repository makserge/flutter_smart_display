package com.smsoft.smartdisplay.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.service.alarm.AlarmHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var alarmHandler: AlarmHandler

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> alarmHandler.rescheduleAlarms()
        }
    }
}
