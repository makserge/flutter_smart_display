package com.smsoft.smartdisplay.data

import androidx.annotation.Keep
import com.smsoft.smartdisplay.data.database.entity.Timer
import com.smsoft.smartdisplay.data.database.entity.emptyTimer

@Keep
sealed class TimerState {
    abstract val timer: Timer

    @Keep
    data class Idle(override val timer: Timer = emptyTimer) : TimerState()

    @Keep
    data class Running(val tick: Long, override val timer: Timer = emptyTimer) : TimerState()

    @Keep
    data class Paused(val tick: Long, override val timer: Timer = emptyTimer) : TimerState()

    @Keep
    data class Finished(override val timer: Timer = emptyTimer) : TimerState()
}