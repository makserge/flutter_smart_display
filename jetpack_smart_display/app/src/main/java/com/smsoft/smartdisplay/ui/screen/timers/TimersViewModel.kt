package com.smsoft.smartdisplay.ui.screen.timers

import android.content.Context
import android.os.CountDownTimer
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.data.AlarmSoundToneType
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.TimerDurationType.Companion.getByCommand
import com.smsoft.smartdisplay.data.TimerState
import com.smsoft.smartdisplay.data.VoiceCommandType
import com.smsoft.smartdisplay.data.database.entity.Timer
import com.smsoft.smartdisplay.data.database.repository.TimerRepository
import com.smsoft.smartdisplay.service.radio.ExoPlayerImpl
import com.smsoft.smartdisplay.service.timer.TimerHandler
import com.smsoft.smartdisplay.ui.composable.settings.TIMER_ASR_ENABLED_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.TIMER_SOUND_VOLUME_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.TIMER_TIMEOUT_DEFAULT
import com.smsoft.smartdisplay.utils.playAlarmSound
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class TimersViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val dataStore: DataStore<Preferences>,
    private val timerRepository: TimerRepository,
    private val timerHandler: TimerHandler
) : ViewModel() {

    private var isTimerAsrEnabled = TIMER_ASR_ENABLED_DEFAULT
    private var timerSoundVolume = TIMER_SOUND_VOLUME_DEFAULT
    private var timerOffTimeout = TIMER_TIMEOUT_DEFAULT

    var player = ExoPlayerImpl.getExoPlayer(
        context = context,
        audioAttributes = ExoPlayerImpl.getAudioAttributes()
    )

    val getAll = timerRepository.getAll

    private val timerStateInt = MutableStateFlow(mapOf<Long, TimerState>())
    val timerState = timerStateInt.asStateFlow()

    val timerTickMap = timerHandler.timerTickMap

    private val timerFinishedStateInt = MutableStateFlow<TimerState>(TimerState.Idle())
    val timerFinishedState = timerFinishedStateInt.asStateFlow()

    private var timerOffTimer: CountDownTimer? = null

    init {
        viewModelScope.launch {
            val data = dataStore.data.first()
            data[booleanPreferencesKey(PreferenceKey.TIMER_ASR_ENABLED.key)]?.let {
                isTimerAsrEnabled = it
            }
            data[floatPreferencesKey(PreferenceKey.TIMER_TIMEOUT.key)]?.let {
                timerOffTimeout = it
            }
            data[floatPreferencesKey(PreferenceKey.TIMER_SOUND_VOLUME.key)]?.let {
                timerSoundVolume = it
            }
        }
        viewModelScope.launch {
            timerHandler.timerState.collect { timer ->
                timerStateInt.value = timer
                timer.forEach { item -> run {
                    if (item.value is TimerState.Finished) {
                        resetItemState(item.value.timer)
                        timerFinishedStateInt.value = item.value
                        playTimerSound(item.value.timer)
                        restartTimerOffTimer {
                            cancelTimerAlert(item.value.timer)
                        }
                    }
                }}
            }
        }
    }

    fun updateItem(item: Timer) {
        viewModelScope.launch(Dispatchers.IO) {
            if (item.id > 0) {
                timerHandler.resetTimer(item)
                timerRepository.update(item)

            } else {
                timerRepository.insert(item)
            }
        }
    }

    fun deleteItem(item: Timer) = viewModelScope.launch(Dispatchers.IO) {
        timerHandler.deleteTimer(item)
        timerRepository.delete(item)
    }

    fun resetItemState(item: Timer) {
        timerHandler.resetTimer(item)
    }

    fun changeItemState(state: TimerState) {
        when (state) {
            is TimerState.Idle -> {
                timerHandler.startTimer(state.timer.duration, state.timer)
            }
            is TimerState.Running -> {
                timerHandler.pauseTimer(state)
            }
            is TimerState.Paused -> {
                timerHandler.startTimer(state.tick, state.timer)
            }
            is TimerState.Finished -> {}
        }
    }

    private fun playTimerSound(timer: Timer) {
        playAlarmSound(
            player = player,
            soundToneType = AlarmSoundToneType.getById(timer.soundTone),
            soundVolume = timerSoundVolume,
            isFadeIn = false,
            isRepeat = true
        )
    }

    fun cancelTimerAlert(timer: Timer) {
        player.stop()
        timerOffTimer?.cancel()

        timerFinishedStateInt.value = TimerState.Idle(timer)
    }

    private fun restartTimerOffTimer(
        onEnd: () -> Unit
    ) {
        if (timerOffTimer != null) {
            timerOffTimer!!.cancel()
            timerOffTimer = null
        }
        timerOffTimer = object : CountDownTimer((timerOffTimeout * 60000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                onEnd()
            }
        }
        timerOffTimer!!.start()
    }

    suspend fun processVoiceCommand(command: VoiceCommandType) {
        val type = getByCommand(command) ?: return
        val duration = type.id.toLong()
        val timers = getAll.first().filter {
            it.duration == duration
        }
        val timer = if (timers.isNotEmpty()) {
            timers[0]
        } else {
            val timer = Timer(
                id = 0,
                title = context.getString(type.titleId),
                duration = duration,
                soundTone = AlarmSoundToneType.CYAN_ALARM.id
            )
            val itemId = timerRepository.insert(timer)
            timer.copy(id = itemId)
        }
        resetItemState(timer)
        changeItemState(TimerState.Idle(timer))
    }
}

const val DEFAULT_TIMER_TITLE = "Timer"