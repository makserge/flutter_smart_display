package com.smsoft.smartdisplay.service.alarm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.data.database.entity.Alarm
import com.smsoft.smartdisplay.data.database.repository.AlarmRepository
import com.smsoft.smartdisplay.receiver.AlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.pow


@UnstableApi
class AlarmHandler @Inject constructor(
    private val app: Application,
    private val alarmManager: AlarmManager,
    private val alarmRepository: AlarmRepository,
    private val coroutineScope: CoroutineScope
) {
    private val alarmStateInt = MutableSharedFlow<Alarm>()
    val alarmState = alarmStateInt.asSharedFlow()
    private val alarmFireStateInt = MutableSharedFlow<Long>()
    val alarmFireState = alarmFireStateInt.asSharedFlow()
    var lightSensorState = MutableStateFlow(0)

    fun createAlarm(alarm: Alarm) {
        val hours = alarm.time / 60
        val minutes = alarm.time - (hours * 60)
        var date = Calendar.getInstance(Locale.GERMANY).apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        date = checkDate(date)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            date.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            getPendingIntent(alarm)
        )
    }

    fun deleteAlarm(alarm: Alarm) {
        alarmManager.cancel(getPendingIntent(alarm))
    }

    private fun getPendingIntent(alarm: Alarm): PendingIntent {
        val intent = Intent(app, AlarmReceiver::class.java).apply {
            this.action = alarm.id.toString()
            putExtra(INTENT_ALARM_ID, alarm.id)
            putExtra(INTENT_ALARM_DAYS_OF_WEEK, alarm.days)
        }
        return PendingIntent.getBroadcast(app, 0, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun checkDate(date: Calendar): Calendar {
        val currentDate = Calendar.getInstance()
        if (checkIfSameDay(date, currentDate)) {
            val duration = abs(currentDate.timeInMillis - date.timeInMillis)
            val days = TimeUnit.MILLISECONDS.toDays(duration).toInt()
            date.add(Calendar.DATE, days + 1)
        }
        return date
    }

    private fun checkIfSameDay(date: Calendar, currentDate: Calendar): Boolean {
        if (date.before(currentDate)) {
            return true
        }
        return date.get(Calendar.DATE) == currentDate.get(Calendar.DATE) &&
               date.get(Calendar.HOUR_OF_DAY) == currentDate.get(Calendar.HOUR_OF_DAY) &&
               date.get(Calendar.MINUTE) == currentDate.get(Calendar.MINUTE)
    }

    fun isAlarmToday(days: Int): Boolean {
        val currentWeekDay = LocalDate.now().dayOfWeek.value
        for (index in 1..7 step 1) {
            val pow = 2.0.pow(index.toDouble()).toInt()
            if ((days > 0) && (days and pow != 0) && (index == currentWeekDay)) {
                return true
            }
        }
        return false
    }

    fun rescheduleAlarms() {
        coroutineScope.launch {
            alarmRepository.getAll.first()
                .filter { it.isEnabled }
                .forEach { createAlarm(it) }
        }
    }

    fun fireAlarm(alarmId: Long) {
        coroutineScope.launch {
            alarmStateInt.emit(alarmRepository.get(alarmId))
            alarmFireStateInt.emit(System.currentTimeMillis())
        }
    }
}

const val INTENT_ALARM_ID = "alarm_id"
const val INTENT_ALARM_DAYS_OF_WEEK = "days_of_week"