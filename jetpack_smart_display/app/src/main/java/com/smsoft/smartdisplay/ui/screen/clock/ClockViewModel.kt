package com.smsoft.smartdisplay.ui.screen.clock

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@HiltViewModel
class ClockViewModel @Inject constructor() : ViewModel() {
    private val TIMER_INTERVAL = 100L //100ms

    private val uiStatePrivate = MutableStateFlow(getTime())

    private var isTimerStarted = true
    val uiState = uiStatePrivate.asStateFlow()

    fun onStart() {
        fixedRateTimer(
            name= "default",
            daemon = false,
            initialDelay = 0L,
            period = TIMER_INTERVAL) {

            val cal = Calendar.getInstance()
            uiStatePrivate.update {
                it.copy(
                    year = cal.get(Calendar.YEAR),
                    month = cal.get(Calendar.MONTH),
                    day = cal.get(Calendar.DATE),
                    dayOfWeek = cal.get(Calendar.DAY_OF_WEEK),
                    hour = cal.get(Calendar.HOUR_OF_DAY),
                    minute = cal.get(Calendar.MINUTE),
                    second = cal.get(Calendar.SECOND),
                    millisecond = cal.get(Calendar.MILLISECOND)
                )
            }

            if (!isTimerStarted) {
                cancel()
            }
        }
    }

    fun onStop() {
        isTimerStarted = false
    }

    private fun getTime(): ClockUiState {
        val cal = Calendar.getInstance()
        return ClockUiState(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH),
            day = cal.get(Calendar.DATE),
            dayOfWeek = cal.get(Calendar.DAY_OF_WEEK),
            hour = cal.get(Calendar.HOUR_OF_DAY),
            minute = cal.get(Calendar.MINUTE),
            second = cal.get(Calendar.SECOND),
            millisecond = cal.get(Calendar.MILLISECOND)
        )
    }
}

data class ClockUiState(
    val year: Int,
    val month: Int,
    val day: Int,
    val dayOfWeek: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val millisecond: Int
)