package com.smsoft.smartdisplay.service.timer

import android.os.CountDownTimer
import androidx.compose.runtime.mutableStateMapOf
import com.smsoft.smartdisplay.data.TimerState
import com.smsoft.smartdisplay.data.database.entity.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class TimerHandler @Inject constructor(
    private val coroutineScope: CoroutineScope
) {
    private val timerStateMap = mutableMapOf<Long, TimerState>()
    private val timerStateInt = MutableStateFlow(timerStateMap)
    val timerState = timerStateInt.asStateFlow()
    private val timerEndStateInt = MutableSharedFlow<Long>()
    val timerEndState = timerEndStateInt.asSharedFlow()
    val timerTickMap = mutableStateMapOf<Long, Long>()

    private val countDownTimers = mutableMapOf<Long, CountDownTimer>()

    fun startTimer(duration: Long, timer: Timer) {
        val durationMs = duration * 1000L

        val countDownTimer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(tick: Long) {
                timerTickMap[timer.id] =  tick / 1000
            }

            override fun onFinish() {
                makeNewState(TimerState.Finished(timer))
                coroutineScope.launch {
                    timerEndStateInt.emit(System.currentTimeMillis())
                }
            }
        }.start()
        countDownTimers[timer.id] = countDownTimer
        makeNewState(TimerState.Running(timer.duration, timer))
    }

    fun pauseTimer(state: TimerState.Running) {
        countDownTimers[state.timer.id]?.cancel()
        makeNewState(TimerState.Paused(timerTickMap[state.timer.id]!!, state.timer))
    }

    fun resetTimer(timer: Timer) {
        countDownTimers[timer.id]?.cancel()
        makeNewState(TimerState.Idle(timer))
    }

    fun deleteTimer(timer: Timer) {
        countDownTimers[timer.id]?.cancel()
    }

    private fun makeNewState(state: TimerState) {
        val newState = timerStateInt.value.toMutableMap()
        newState[state.timer.id] = state
        timerStateInt.value = newState
    }
}