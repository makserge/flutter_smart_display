package com.smsoft.smartdisplay.ui.screen.timers

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.data.AlarmSoundToneType
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.TimerDurationType
import com.smsoft.smartdisplay.data.TimerState
import com.smsoft.smartdisplay.data.database.entity.Timer
import com.smsoft.smartdisplay.data.database.repository.TimerRepository
import com.smsoft.smartdisplay.service.radio.ExoPlayerImpl
import com.smsoft.smartdisplay.service.timer.RingingTimer
import com.smsoft.smartdisplay.service.timer.TimerHandler
import com.smsoft.smartdisplay.ui.composable.settings.TIMER_ASR_ENABLED_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.TIMER_SOUND_VOLUME_DEFAULT
import com.smsoft.smartdisplay.utils.playAlarmSound
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.smsoft.smartdisplay.utils.observe
import kotlinx.coroutines.CompletableDeferred
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

    // Released in onCleared. Private: the timer editor plays its preview through previewTone().
    private val player = ExoPlayerImpl.getExoPlayer(
        context = context,
        audioAttributes = ExoPlayerImpl.getAudioAttributes()
    )
    private var isCleared = false

    val getAll = timerRepository.getAll

    val timerState = timerHandler.timerState

    val timerTickMap = timerHandler.timerTickMap

    private val timerFinishedStateInt = MutableStateFlow<TimerState>(TimerState.Idle())
    val timerFinishedState = timerFinishedStateInt.asStateFlow()

    private val scrollToItemIdInt = MutableStateFlow<Long?>(null)
    val scrollToItemId = scrollToItemIdInt.asStateFlow()

    // The alert this ViewModel sounds, else null. The alert itself, with its deadline, lives in
    // TimerHandler; this ViewModel only plays it and shows the dialog.
    private var soundingRing: RingingTimer? = null

    init {
        // Follows Settings: the timer settings used to be read once, so a change only applied
        // after a restart.
        val settingsLoaded = CompletableDeferred<Unit>()
        dataStore.observe(viewModelScope, {
            Pair(
                it[booleanPreferencesKey(PreferenceKey.TIMER_ASR_ENABLED.key)] ?: TIMER_ASR_ENABLED_DEFAULT,
                it[floatPreferencesKey(PreferenceKey.TIMER_SOUND_VOLUME.key)] ?: TIMER_SOUND_VOLUME_DEFAULT
            )
        }) { (isAsrEnabled, volume) ->
            isTimerAsrEnabled = isAsrEnabled
            timerSoundVolume = volume
            settingsLoaded.complete(Unit)
        }
        viewModelScope.launch {
            // An alert for a timer that finished before this ViewModel existed used to sound with
            // the default volume (the maximum) because the settings were not read yet.
            settingsLoaded.await()
            // A StateFlow: delivers an alert that began before this ViewModel existed (e.g. in the
            // dashboard this one replaced) for the time left, and its end wherever that happens
            // (timeout in TimerHandler, dismissal in another dashboard).
            timerHandler.ringingTimer.collect { ring ->
                when {
                    ring === soundingRing -> {}
                    ring == null -> stopAlert()
                    else -> startAlert(ring)
                }
            }
        }
    }

    fun updateItem(item: Timer) {
        if (item.id > 0) {
            // Here rather than on the IO thread below: TimerHandler belongs to the main thread.
            timerHandler.resetTimer(item)
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (item.id > 0) {
                timerRepository.update(item)
            } else {
                timerRepository.insert(item)
            }
        }
    }

    fun deleteItem(item: Timer) {
        timerHandler.deleteTimer(item)
        viewModelScope.launch(Dispatchers.IO) {
            timerRepository.delete(item)
        }
    }

    fun resetItemState(item: Timer) {
        timerHandler.resetTimer(item)
    }

    fun changeItemState(state: TimerState) {
        val wasRunning = timerHandler.timerState.value[state.timer.id] is TimerState.Running
        timerHandler.toggleTimer(state.timer)
        if (!wasRunning) {
            scrollToItemIdInt.value = state.timer.id
        }
    }

    fun consumeScrollToItem() {
        scrollToItemIdInt.value = null
    }

    private fun playTimerSound(timer: Timer) {
        if (isCleared) {
            return
        }
        playAlarmSound(
            player = player,
            soundToneType = AlarmSoundToneType.getById(timer.soundTone),
            soundVolume = timerSoundVolume,
            isRepeat = true
        )
    }

    /** Sound preview in the timer editor. */
    fun previewTone(soundToneId: String) {
        if (isCleared) {
            return
        }
        playAlarmSound(
            player = player,
            soundToneType = AlarmSoundToneType.getById(soundToneId),
            soundVolume = 1F
        )
    }

    /** Dismisses the alert of [timer] in every dashboard. */
    fun cancelTimerAlert(timer: Timer) {
        val ring = soundingRing?.takeIf { it.timer.id == timer.id } ?: return
        stopAlert()
        timerHandler.onTimerAlertDismissed(ring)
    }

    private fun startAlert(ring: RingingTimer) {
        val elapsedMs = ring.elapsedMs()
        if (elapsedMs >= ring.timeoutMs) {
            // Over already: TimerHandler is about to end it.
            stopAlert()
            return
        }
        Log.i(TAG, "Timer alert ${ring.timer.id}: $elapsedMs ms in, ${ring.timeoutMs - elapsedMs} ms left")
        soundingRing = ring
        timerFinishedStateInt.value = TimerState.Finished(ring.timer)
        playTimerSound(ring.timer)
    }

    private fun stopAlert() {
        val ring = soundingRing ?: return
        soundingRing = null
        timerFinishedStateInt.value = TimerState.Idle(ring.timer)
        if (!isCleared) {
            player.stop()
        }
    }

    /**
     * The player used to live as long as the process: one more ExoPlayer with its playback thread
     * for every dashboard that was replaced. An alert that is sounding now goes on in
     * TimerHandler, so the player is released at once: a dashboard that replaces this one sounds
     * the alert for the time left.
     */
    override fun onCleared() {
        super.onCleared()
        isCleared = true
        soundingRing = null
        player.release()
    }

    suspend fun processVoiceCommand(durationId: String) {
        val type = TimerDurationType.entries.find { it.id == durationId } ?: return
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
        timerHandler.restartTimer(timer)
        scrollToItemIdInt.value = timer.id
    }
}

private const val TAG = "TimersViewModel"

const val DEFAULT_TIMER_TITLE = "Timer"