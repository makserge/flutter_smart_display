package com.smsoft.smartdisplay.ui.screen.dashboard

import android.annotation.SuppressLint
import android.content.Context
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
import com.smsoft.smartdisplay.service.alarm.AlarmHandler
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionHandler
import com.smsoft.smartdisplay.service.mqtt.MqttCallbackDispatcher
import com.smsoft.smartdisplay.service.radio.ExoPlayerImpl
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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val timerHandler: TimerHandler
) : ViewModel() {
    private val currentPageStateInt = MutableStateFlow(DashboardItem.CLOCK.ordinal)
    val currentPageState = currentPageStateInt.asStateFlow()

    private val voiceCommandStateInt = MutableStateFlow(VoiceCommand(VoiceCommandType.CLOCK))
    val voiceCommandState = voiceCommandStateInt.asStateFlow()

    private var player = ExoPlayerImpl.getExoPlayer(
        context = context,
        audioAttributes = ExoPlayerImpl.getAudioAttributes()
    )

    private val mqttManager = DashboardMqttManager(
        dataStore = dataStore,
        mqttClient = mqttClient,
        mqttCallbackDispatcher = mqttCallbackDispatcher
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
        scope = viewModelScope
    )

    private val clockAutoReturnController = ClockAutoReturnController(
        dataStore = dataStore,
        onReturnToClock = { currentPageStateInt.value = DashboardItem.CLOCK.ordinal }
    )

    private val asrController = AsrController(
        context = context,
        dataStore = dataStore,
        speechRecognitionHandler = speechRecognitionHandler,
        player = player,
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
        initAlarm()
        initTimer()
    }

    fun onPageChanged(pageId: Int) {
        viewModelScope.launch {
            currentPageStateInt.value = pageId
            val isExemptPage = (pageId == DashboardItem.CLOCK.ordinal) ||
                (pageId == DashboardItem.INTERNET_RADIO.ordinal)
            clockAutoReturnController.onPageChanged(isExemptPage)
        }
    }

    fun togglePressButton() {
        pushButtonController.toggle()
    }

    fun resetDoorBellAlarmState() {
        doorbellController.reset()
    }

    fun resetVoiceCommand() {
        voiceCommandStateInt.value = VoiceCommand(VoiceCommandType.CLOCK)
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

    private fun initAlarm() {
        viewModelScope.launch {
            alarmHandler.alarmFireState.collect {
                currentPageStateInt.value = DashboardItem.ALARMS.ordinal
            }
        }
    }

    private fun initTimer() {
        viewModelScope.launch {
            timerHandler.timerEndState.collect {
                voiceCommandStateInt.value = VoiceCommand(VoiceCommandType.CLOCK)
                currentPageStateInt.value = DashboardItem.TIMERS.ordinal
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
            voiceCommandStateInt.value = VoiceCommand(type)
        }
        currentPageStateInt.value = item.ordinal
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
        sensorRelayController.release()
        player.release()
    }
}
