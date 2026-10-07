package com.smsoft.smartdisplay.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.service.alarm.AlarmHandler
import com.smsoft.smartdisplay.service.alarm.INTENT_ALARM_DAYS_OF_WEEK
import com.smsoft.smartdisplay.service.alarm.INTENT_ALARM_ID
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var alarmHandler: AlarmHandler

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(INTENT_ALARM_ID, 0L)
        val days = intent.getIntExtra(INTENT_ALARM_DAYS_OF_WEEK, 0)
        if (alarmHandler.isAlarmToday(days)) {
            alarmHandler.fireAlarm(alarmId)
        }
        // Alarms are exact one-shot entries now: schedule tomorrow's occurrence.
        alarmHandler.scheduleNext(alarmId)
    }
}