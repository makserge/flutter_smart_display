package com.smsoft.smartdisplay.service.timer

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.TimerState
import com.smsoft.smartdisplay.data.database.entity.Timer
import com.smsoft.smartdisplay.ui.composable.settings.TIMER_TIMEOUT_DEFAULT
import com.smsoft.smartdisplay.utils.observe
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

/** One alert of [timer]. A new instance per alert, so a timer that finishes again always emits. */
class RingingTimer(val timer: Timer, val startedAt: Long, val timeoutMs: Long) {
    fun elapsedMs(now: Long = SystemClock.elapsedRealtime()) = (now - startedAt).coerceAtLeast(0L)

    fun remainingMs(now: Long = SystemClock.elapsedRealtime()) = timeoutMs - elapsedMs(now)

    override fun toString() = "RingingTimer(timer=${timer.id}, startedAt=$startedAt, timeoutMs=$timeoutMs)"
}

/**
 * The single source of truth for the timers and their alert. Everything that changes them runs
 * on the main thread: the countdowns need its Looper, and changes that used to come from IO
 * threads too could overwrite each other. Calls from other threads are posted to it.
 */
class TimerHandler @Inject constructor(
    dataStore: DataStore<Preferences>,
    coroutineScope: CoroutineScope
) {
    // Logged: an exception escaping a job of this app-wide scope would crash the app.
    private val scope = coroutineScope + CoroutineExceptionHandler { _, e ->
        Log.e(TAG, "Timer job failed", e)
    }

    private val timerStateInt = MutableStateFlow<Map<Long, TimerState>>(emptyMap())
    val timerState: StateFlow<Map<Long, TimerState>> = timerStateInt.asStateFlow()
    /** Whole seconds left of the running and paused timers, for the display. */
    val timerTickMap = mutableStateMapOf<Long, Long>()

    // At most one countdown per timer: a second start used to leave the first countdown running,
    // and it finished the timer later although the card showed it paused or reset.
    private val countDownTimers = mutableMapOf<Long, CountDownTimer>()
    // elapsedRealtime() at which each running timer ends, so that a pause keeps the exact time left.
    private val endTimes = mutableMapOf<Long, Long>()
    private val mainHandler = Handler(Looper.getMainLooper())

    // The alert is state, not a one-off event: a dashboard created while it sounds (e.g. one that
    // replaces the dashboard that showed it) shows it for the time left, and an alert that ended
    // while no dashboard existed is not played later.
    private val ringingTimerInt = MutableStateFlow<RingingTimer?>(null)
    /** The alert of a timer that finished and has neither been dismissed nor timed out, or null. */
    val ringingTimer: StateFlow<RingingTimer?> = ringingTimerInt.asStateFlow()
    private var ringTimeout: Job? = null

    // Taken when an alert starts: a change in Settings applies from the next alert.
    @Volatile
    private var alertTimeoutMs = minutesToMs(TIMER_TIMEOUT_DEFAULT)

    init {
        dataStore.observe(scope, {
            it[floatPreferencesKey(PreferenceKey.TIMER_TIMEOUT.key)] ?: TIMER_TIMEOUT_DEFAULT
        }) {
            alertTimeoutMs = minutesToMs(it)
        }
    }

    fun startTimer(duration: Long, timer: Timer) = onMain { start(duration * 1000L, timer) }

    fun pauseTimer(state: TimerState.Running) = onMain { pause(state.timer.id) }

    fun resetTimer(timer: Timer) = onMain { reset(timer) }

    fun deleteTimer(timer: Timer) = onMain { remove(timer.id) }

    /** Starts [timer] from its full length, whatever state it is in. */
    fun restartTimer(timer: Timer) = onMain { start(timer.duration * 1000L, timer) }

    /**
     * Play or pause, decided on this handler's state rather than on the state a click lambda
     * captured: two quick taps used to both see Idle and start the timer twice.
     */
    fun toggleTimer(timer: Timer) = onMain {
        when (val state = timerStateInt.value[timer.id]) {
            is TimerState.Running -> pause(timer.id)
            is TimerState.Paused -> start(state.remainingMs, state.timer)
            else -> start(timer.duration * 1000L, timer)
        }
    }

    /** Ends [ring] unless it has already ended or a newer alert has replaced it. */
    fun onTimerAlertDismissed(ring: RingingTimer) = onMain { endRing(ring, "dismissed") }

    private fun start(durationMs: Long, timer: Timer) {
        stopCountDown(timer.id)
        // CountDownTimer.start() finishes a length <= 0 at once, and the Running state set after it
        // then stayed for good, with no countdown behind it.
        if (durationMs <= 0L) {
            finish(timer)
            return
        }
        endTimes[timer.id] = SystemClock.elapsedRealtime() + durationMs
        // Before Running is published: a pause before the first tick found no tick and crashed.
        timerTickMap[timer.id] = durationMs / 1000
        setState(TimerState.Running(durationMs / 1000, timer))
        val countDownTimer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (countDownTimers[timer.id] === this) {
                    timerTickMap[timer.id] = millisUntilFinished / 1000
                }
            }

            override fun onFinish() {
                if (countDownTimers[timer.id] !== this) {
                    return
                }
                stopCountDown(timer.id)
                finish(timer)
            }
        }
        countDownTimers[timer.id] = countDownTimer
        countDownTimer.start()
    }

    private fun pause(id: Long) {
        // A stale request, e.g. a tap on a timer that has finished meanwhile, is ignored.
        val running = timerStateInt.value[id] as? TimerState.Running ?: return
        val leftMs = ((endTimes[id] ?: 0L) - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        stopCountDown(id)
        if (leftMs == 0L) {
            finish(running.timer)
            return
        }
        timerTickMap[id] = leftMs / 1000
        setState(TimerState.Paused(leftMs / 1000, running.timer, remainingMs = leftMs))
    }

    private fun reset(timer: Timer) {
        stopCountDown(timer.id)
        timerTickMap.remove(timer.id)
        setState(TimerState.Idle(timer))
    }

    private fun remove(id: Long) {
        stopCountDown(id)
        timerTickMap.remove(id)
        timerStateInt.update { it - id }
    }

    /** The timer is Idle again at once, so it can be restarted while its alert sounds. */
    private fun finish(timer: Timer) {
        timerTickMap.remove(timer.id)
        setState(TimerState.Idle(timer))
        startRing(timer)
    }

    private fun stopCountDown(id: Long) {
        countDownTimers.remove(id)?.cancel()
        endTimes.remove(id)
    }

    private fun setState(state: TimerState) {
        timerStateInt.update { it + (state.timer.id to state) }
    }

    private fun startRing(timer: Timer) {
        val ring = RingingTimer(timer, SystemClock.elapsedRealtime(), alertTimeoutMs)
        Log.i(TAG, "Timer ${timer.id} finished, alert for ${ring.timeoutMs} ms")
        ringingTimerInt.value = ring
        ringTimeout?.cancel()
        // The alert ends here, also when no dashboard shows it.
        ringTimeout = scope.launch(Dispatchers.Main) {
            delay(ring.timeoutMs)
            endRing(ring, "timed out")
        }
    }

    private fun endRing(ring: RingingTimer, why: String) {
        if (ringingTimerInt.value !== ring) {
            return
        }
        Log.i(TAG, "Timer ${ring.timer.id} alert $why after ${ring.elapsedMs()} ms")
        ringingTimerInt.value = null
        ringTimeout?.cancel()
        ringTimeout = null
    }

    private fun onMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }
}

// Minutes come from a continuous slider: 2.7 minutes must not become 2.
private fun minutesToMs(minutes: Float) = (minutes * 60_000).toLong()

private const val TAG = "TimerHandler"