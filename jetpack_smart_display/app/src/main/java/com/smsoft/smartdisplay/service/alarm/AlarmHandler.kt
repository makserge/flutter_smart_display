package com.smsoft.smartdisplay.service.alarm

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.data.AlarmSoundType
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.database.entity.Alarm
import com.smsoft.smartdisplay.data.database.repository.AlarmRepository
import com.smsoft.smartdisplay.receiver.AlarmReceiver
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_TIMEOUT_DEFAULT
import com.smsoft.smartdisplay.utils.getShowAppIntent
import dagger.Lazy
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.pow

/** One ring of [alarm]. A new instance per ring, so an alarm that goes off again always emits. */
class RingingAlarm(val alarm: Alarm, val startedAt: Long, val timeoutMs: Long) {
    fun elapsedMs(now: Long = SystemClock.elapsedRealtime()) = (now - startedAt).coerceAtLeast(0L)

    fun remainingMs(now: Long = SystemClock.elapsedRealtime()) = timeoutMs - elapsedMs(now)

    override fun toString() = "RingingAlarm(alarm=${alarm.id}, startedAt=$startedAt, timeoutMs=$timeoutMs)"
}

@UnstableApi
class AlarmHandler @Inject constructor(
    private val app: Application,
    private val alarmManager: AlarmManager,
    private val alarmRepository: AlarmRepository,
    private val dataStore: DataStore<Preferences>,
    // Lazy: creating the client reads the broker settings blocking, and this handler is created
    // when the app starts.
    mqttClient: Lazy<MqttAndroidClient>,
    private val coroutineScope: CoroutineScope
) {
    // Logged: an exception escaping a job of this app-wide scope would crash the app.
    private val ringScope = coroutineScope + CoroutineExceptionHandler { _, e ->
        Log.e(TAG, "Alarm job failed", e)
    }

    // The ringing alarm is state, not a one-off event: the old replay-less SharedFlow dropped the
    // alarm when no Alarms page existed yet (the first alarm after a cold start), so it never rang.
    // Its deadline and wake-up light live here too: a dashboard that replaces another one rings it
    // on for the time left, and a ring that ended while no dashboard existed is not played later.
    // Changed on the main thread only.
    private val ringingAlarmInt = MutableStateFlow<RingingAlarm?>(null)
    /** The ring of an alarm that went off and has neither been dismissed nor timed out, or null. */
    val ringingAlarm: StateFlow<RingingAlarm?> = ringingAlarmInt.asStateFlow()
    private var ringTimeout: Job? = null
    /** Room brightness as the dashboard reports it; 0 means no reading yet. */
    val lightSensorState = MutableStateFlow(0)
    private val wakeLight = AlarmWakeLight(dataStore, mqttClient, lightSensorState, ringScope)
    private val mainHandler = Handler(Looper.getMainLooper())

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

        scheduleExact(date.timeInMillis, getPendingIntent(alarm))
    }

    /**
     * setRepeating() is inexact: Android may deliver a daily repeating alarm up to 18 hours late.
     * An alarm clock entry fires on time, so it is scheduled one day at a time and
     * [scheduleNext] books the following day when it goes off.
     */
    private fun scheduleExact(triggerAtMillis: Long, operation: PendingIntent) {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) && !alarmManager.canScheduleExactAlarms()) {
            // Android 12: exact alarms not granted yet. Best effort within 10 minutes.
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, ALARM_WINDOW_MS, operation)
            return
        }
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAtMillis, getShowIntent()),
            operation
        )
    }

    /** Opens the running dashboard; an explicit activity intent could start a second one. */
    private fun getShowIntent(): PendingIntent {
        return PendingIntent.getActivity(
            app,
            0,
            getShowAppIntent(app),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /** Called by AlarmReceiver: an alarm clock entry fires once, so book the next day. */
    fun scheduleNext(alarmId: Long) {
        coroutineScope.launch {
            val alarm = try {
                alarmRepository.get(alarmId)
            } catch (e: Exception) {
                null
            } ?: return@launch
            if (alarm.isEnabled) {
                createAlarm(alarm)
            }
        }
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

    /** Stops a ringing alarm that plays a radio station; ignored when no such alarm rings. */
    fun requestStopRadioAlarm() = onMain {
        val ring = ringingAlarmInt.value ?: return@onMain
        if (ring.alarm.soundType == AlarmSoundType.RADIO.id) {
            endRing(ring, "stopped by voice")
        }
    }

    fun fireAlarm(alarmId: Long) {
        // The ring counts from now, not from the end of the reads below.
        val startedAt = SystemClock.elapsedRealtime()
        ringScope.launch {
            val alarm = try {
                alarmRepository.get(alarmId)
            } catch (e: Exception) {
                // The alarm was deleted after it had been scheduled.
                Log.w(TAG, "Alarm $alarmId not found", e)
                null
            } ?: return@launch
            val timeoutMs = readAlarmTimeoutMs()
            withContext(Dispatchers.Main) {
                startRing(RingingAlarm(alarm, startedAt, timeoutMs))
            }
        }
    }

    /** Ends [ring] unless it has already ended or a newer ring has replaced it. */
    fun onAlarmDismissed(ring: RingingAlarm) = onMain { endRing(ring, "dismissed") }

    private fun startRing(ring: RingingAlarm) {
        Log.i(TAG, "Alarm ${ring.alarm.id} rings for ${ring.remainingMs()} ms")
        ringingAlarmInt.value = ring
        wakeLight.start()
        ringTimeout?.cancel()
        // The ring ends here, also when no dashboard shows it.
        ringTimeout = ringScope.launch(Dispatchers.Main) {
            delay(ring.remainingMs())
            endRing(ring, "timed out")
        }
    }

    private fun endRing(ring: RingingAlarm, why: String) {
        if (ringingAlarmInt.value !== ring) {
            return
        }
        Log.i(TAG, "Alarm ${ring.alarm.id} $why after ${ring.elapsedMs()} ms")
        ringingAlarmInt.value = null
        ringTimeout?.cancel()
        ringTimeout = null
        wakeLight.stop()
    }

    /** Read when the alarm goes off: a change in Settings applies from the next ring. */
    private suspend fun readAlarmTimeoutMs(): Long {
        val minutes = try {
            dataStore.data.first()[floatPreferencesKey(PreferenceKey.ALARM_TIMEOUT.key)]
        } catch (e: IOException) {
            Log.w(TAG, "Alarm timeout not readable", e)
            null
        } ?: ALARM_TIMEOUT_DEFAULT
        // Minutes come from a continuous slider: 2.7 minutes must not become 2.
        return (minutes * 60_000).toLong()
    }

    private fun onMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }
}

private const val ALARM_WINDOW_MS = 10L * 60 * 1000

private const val TAG = "AlarmHandler"

const val INTENT_ALARM_ID = "alarm_id"
const val INTENT_ALARM_DAYS_OF_WEEK = "days_of_week"