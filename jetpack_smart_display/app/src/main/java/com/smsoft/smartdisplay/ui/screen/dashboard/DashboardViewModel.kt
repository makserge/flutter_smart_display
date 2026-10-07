package com.smsoft.smartdisplay.ui.screen.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.data.AsrCommand
import com.smsoft.smartdisplay.data.DashboardItem
import com.smsoft.smartdisplay.data.TimerDurationType
import com.smsoft.smartdisplay.data.VoiceCommand
import com.smsoft.smartdisplay.data.VoiceCommandType
import com.smsoft.smartdisplay.data.database.repository.SensorRepository
import com.smsoft.smartdisplay.service.alarm.AlarmHandler
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionHandler
import com.smsoft.smartdisplay.service.mqtt.MqttCallbackDispatcher
import com.smsoft.smartdisplay.service.radio.ExoPlayerImpl
import com.smsoft.smartdisplay.service.radio.RadioActiveState
import com.smsoft.smartdisplay.service.radio.RadioMediaServiceHandler
import dagger.Lazy
import com.smsoft.smartdisplay.service.sensor.SensorHandler
import com.smsoft.smartdisplay.service.timer.TimerHandler
import com.smsoft.smartdisplay.ui.screen.dashboard.controller.AsrController
import com.smsoft.smartdisplay.ui.screen.dashboard.controller.ClockAutoReturnController
import com.smsoft.smartdisplay.ui.screen.dashboard.controller.DimmerLightController
import com.smsoft.smartdisplay.ui.screen.dashboard.controller.DoorbellController
import com.smsoft.smartdisplay.ui.screen.dashboard.controller.MessageAlertController
import com.smsoft.smartdisplay.ui.screen.dashboard.controller.PushButtonController
import com.smsoft.smartdisplay.ui.screen.dashboard.controller.SensorRelayController
import com.smsoft.smartdisplay.ui.screen.dashboard.mqtt.DashboardMqttManager
import com.smsoft.smartdisplay.utils.TransientAudioDucking
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@UnstableApi
@SuppressLint("StaticFieldLeak")
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val dataStore: DataStore<Preferences>,
    mqttClient: MqttAndroidClient,
    mqttCallbackDispatcher: MqttCallbackDispatcher,
    speechRecognitionHandler: SpeechRecognitionHandler,
    sensorHandler: SensorHandler,
    private val alarmHandler: AlarmHandler,
    sensorRepository: SensorRepository,
    private val timerHandler: TimerHandler,
    private val radioActiveState: RadioActiveState,
    // Lazy: creating the handler creates the radio player, which must wait for the radio page.
    private val radioMediaServiceHandler: Lazy<RadioMediaServiceHandler>
) : ViewModel() {
    private val currentPageStateInt = MutableStateFlow(DashboardItem.CLOCK.ordinal)
    val currentPageState = currentPageStateInt.asStateFlow()

    /** Pending one-shot voice command for the page it targets; null when there is none. */
    private val voiceCommandStateInt = MutableStateFlow<VoiceCommand?>(null)
    val voiceCommandState = voiceCommandStateInt.asStateFlow()

    // Plays wake-word/error chimes and message alerts. It must not take audio focus, otherwise
    // every chime permanently pauses the radio; radio ducking is done with TransientAudioDucking.
    private var player = ExoPlayerImpl.getExoPlayer(
        context = context,
        audioAttributes = ExoPlayerImpl.getAudioAttributes(),
        handleAudioFocus = false
    )

    private val mqttManager = DashboardMqttManager(
        dataStore = dataStore,
        mqttClient = mqttClient,
        mqttCallbackDispatcher = mqttCallbackDispatcher,
        sensorRepository = sensorRepository
    )

    private val pushButtonController = PushButtonController(
        dataStore = dataStore,
        mqttManager = mqttManager,
        scope = viewModelScope
    )

    private val sensorRelayController = SensorRelayController(
        context = context,
        dataStore = dataStore,
        mqttManager = mqttManager,
        sensorHandler = sensorHandler,
        alarmHandler = alarmHandler,
        scope = viewModelScope
    )

    private val dimmerLightController = DimmerLightController(
        context = context,
        dataStore = dataStore,
        mqttManager = mqttManager,
        scope = viewModelScope
    )

    private val doorbellController = DoorbellController(
        dataStore = dataStore,
        scope = viewModelScope
    )

    private val messageAlertController = MessageAlertController(
        dataStore = dataStore,
        player = player,
        ducking = TransientAudioDucking(context),
        scope = viewModelScope
    )

    private val clockAutoReturnController = ClockAutoReturnController(
        dataStore = dataStore,
        isExemptPage = ::isAutoReturnExemptPage,
        onReturnToClock = { currentPageStateInt.value = DashboardItem.CLOCK.ordinal }
    )

    private val asrController = AsrController(
        context = context,
        dataStore = dataStore,
        speechRecognitionHandler = speechRecognitionHandler,
        player = player,
        listeningDucking = TransientAudioDucking(context),
        scope = viewModelScope,
        onCommand = ::processAsrCommand
    )

    val doorBellAlarmState = doorbellController.doorBellAlarmState
    val asrPermissionsState = asrController.permissionsState
    val asrRecognitionState = asrController.recognitionState
    val messageState = messageAlertController.messageState

    init {
        mqttManager.start(viewModelScope)
        viewModelScope.launch {
            mqttManager.incomingMessages.collect { message ->
                pushButtonController.onMqttMessage(message)
                doorbellController.onMqttMessage(message)
                messageAlertController.onMqttMessage(message)
            }
        }
        pushButtonController.start()
        sensorRelayController.start()
        dimmerLightController.start()
        doorbellController.start()
        messageAlertController.start()
        asrController.start()
        initAlerts()
        initRadio()
    }

    /** Last page the pager came to rest on. */
    private var settledPage = DashboardItem.CLOCK.ordinal

    /** Called with the page the pager has settled on, at the end of every scroll. */
    fun onPageChanged(pageId: Int) {
        viewModelScope.launch {
            settledPage = pageId
            currentPageStateInt.value = pageId
            // A command whose page was not reached (the jump was interrupted) must not fire
            // later, when the user happens to open that page.
            voiceCommandStateInt.value?.let { pending ->
                if (pending.type.page.ordinal != pageId) {
                    voiceCommandStateInt.value = null
                }
            }
            updateClockAutoReturn()
        }
    }

    private suspend fun updateClockAutoReturn() {
        clockAutoReturnController.update()
    }

    /** The radio page only blocks the return to the clock while the radio is actually on. */
    private fun isAutoReturnExemptPage(): Boolean {
        val pageId = currentPageStateInt.value
        return (pageId == DashboardItem.CLOCK.ordinal) ||
            ((pageId == DashboardItem.INTERNET_RADIO.ordinal) && radioActiveState.isActive.value)
    }

    fun togglePressButton() {
        pushButtonController.toggle()
    }

    fun resetDoorBellAlarmState() {
        doorbellController.reset()
    }

    fun resetVoiceCommand() {
        voiceCommandStateInt.value = null
    }

    fun startAsrService() {
        asrController.startAsrService()
    }

    fun disableAsr() {
        asrController.disableAsr()
    }

    fun cancelAsrAction() {
        asrController.cancelAsrAction()
    }

    fun cancelMessageAction() {
        messageAlertController.cancel()
    }

    // Both alerts are state: a dashboard created during one (e.g. one that replaces the dashboard
    // that showed it) starts on its page. The collectors run at once in init, so that page is the
    // pager's first page.
    private fun initAlerts() {
        viewModelScope.launch {
            alarmHandler.ringingAlarm.collect { showNewestAlert() }
        }
        viewModelScope.launch {
            timerHandler.ringingTimer.collect { showNewestAlert() }
        }
    }

    /** The newest alert still going on comes to the front; when it ends, the other one comes back. */
    private fun showNewestAlert() {
        val now = SystemClock.elapsedRealtime()
        val alarm = alarmHandler.ringingAlarm.value?.takeIf { it.remainingMs(now) > 0 }
        val timer = timerHandler.ringingTimer.value?.takeIf { it.remainingMs(now) > 0 }
        when {
            (alarm != null) && ((timer == null) || (alarm.startedAt >= timer.startedAt)) -> {
                currentPageStateInt.value = DashboardItem.ALARMS.ordinal
            }
            timer != null -> {
                voiceCommandStateInt.value = null
                currentPageStateInt.value = DashboardItem.TIMERS.ordinal
            }
        }
    }

    private fun initRadio() {
        viewModelScope.launch {
            radioActiveState.isActive.drop(1).collect {
                if (currentPageStateInt.value == DashboardItem.INTERNET_RADIO.ordinal) {
                    updateClockAutoReturn()
                }
            }
        }
    }

    private fun processAsrCommand(command: String, type: AsrCommand, params: Any?) {
        when (type) {
            AsrCommand.LIGHT1 -> pushButtonController.sendCommand(params as Boolean)
            AsrCommand.LIGHT2 -> sensorRelayController.sendProximityButtonEvent(params as Boolean)
            AsrCommand.DIMMER_LIGHT -> dimmerLightController.sendPowerEvent(params as Boolean)
            AsrCommand.DIMMER_LIGHT_SET -> dimmerLightController.processSetCommand(params as String)
            AsrCommand.DIMMER_LIGHT_STEP -> dimmerLightController.processStepCommand(params as Int)
            AsrCommand.TIMER -> processAsrTimerCommand(params)
            AsrCommand.PAGE -> processAsrPageCommand(command, params)
        }
    }

    private fun processAsrPageCommand(command: String, params: Any?) {
        if (params == null) {
            asrController.playErrorSoundIfEnabled()
            return
        }
        val item = params as DashboardItem
        if (item == DashboardItem.INTERNET_RADIO) {
            val type = VoiceCommandType.getByCommand(
                context = context,
                command = command
            )
            if (type == VoiceCommandType.INTERNET_RADIO_OFF) {
                switchRadioOff()
                return
            }
            voiceCommandStateInt.value = VoiceCommand(type)
        }
        currentPageStateInt.value = item.ordinal
    }

    /**
     * "Radio off" never navigates. The radio only plays while its page is shown, so it is
     * stopped where it is and the radio page then shows the stopped state. A ringing alarm that
     * plays a radio station is switched off as well.
     */
    private fun switchRadioOff() {
        // Nothing to stop when the radio is off and its page is not shown; this also avoids a
        // pointless round trip to an MPD server.
        // (In both cases the radio page has already created the handler.)
        if (radioActiveState.isActive.value ||
            (settledPage == DashboardItem.INTERNET_RADIO.ordinal)) {
            radioMediaServiceHandler.get().stop()
        }
        alarmHandler.requestStopRadioAlarm()
        // A radio command that has not reached the radio page yet must not start it later.
        if (voiceCommandStateInt.value?.type?.page == DashboardItem.INTERNET_RADIO) {
            voiceCommandStateInt.value = null
        }
        if ((currentPageStateInt.value == DashboardItem.INTERNET_RADIO.ordinal) &&
            (settledPage != DashboardItem.INTERNET_RADIO.ordinal)) {
            currentPageStateInt.value = settledPage
        }
    }

    private fun processAsrTimerCommand(params: Any?) {
        currentPageStateInt.value = DashboardItem.TIMERS.ordinal
        val duration = TimerDurationType.getByCommand(
            context = context,
            command = params as String
        ) ?: return
        voiceCommandStateInt.value = VoiceCommand(
            type = VoiceCommandType.TIMER_SET,
            payload = duration.id
        )
    }

    override fun onCleared() {
        super.onCleared()
        mqttManager.stop()
        clockAutoReturnController.release()
        messageAlertController.release()
        asrController.release()
        sensorRelayController.release()
        player.release()
    }
}
