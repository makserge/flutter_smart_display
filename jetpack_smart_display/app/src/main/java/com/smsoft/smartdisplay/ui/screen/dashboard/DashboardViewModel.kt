package com.smsoft.smartdisplay.ui.screen.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.data.AudioType
import com.smsoft.smartdisplay.data.DashboardItem
import com.smsoft.smartdisplay.data.MQTTServer
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.VoiceCommand
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionHandler
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionService
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionState
import com.smsoft.smartdisplay.service.asr.processCommand
import com.smsoft.smartdisplay.service.radio.ExoPlayerImpl
import com.smsoft.smartdisplay.service.sensor.SensorHandler
import com.smsoft.smartdisplay.service.sensor.SensorService
import com.smsoft.smartdisplay.ui.composable.settings.ASR_SOUND_ENABLED_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.ASR_SOUND_VOLUME_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.LIGHT_SENSOR_TOPIC_DEFAULT
import com.smsoft.smartdisplay.ui.screen.settings.CLOCK_AUTO_RETURN_TIMEOUT_DEFAULT
import com.smsoft.smartdisplay.ui.screen.settings.DOORBELL_ALARM_DEFAULT_TOPIC
import com.smsoft.smartdisplay.utils.getForegroundNotification
import com.smsoft.smartdisplay.utils.playAssetSound
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject

@HiltViewModel
@UnstableApi
@SuppressLint("StaticFieldLeak")
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val dataStore: DataStore<Preferences>,
    private val mqttClient: MqttAndroidClient,
    private val speechRecognitionHandler: SpeechRecognitionHandler,
    private val sensorHandler: SensorHandler
) : ViewModel() {
    private val currentPageStateInt = MutableStateFlow(DashboardItem.CLOCK.ordinal)
    val currentPageState = currentPageStateInt.asStateFlow()

    private val voiceCommandStateInt = MutableStateFlow(VoiceCommand.CLOCK)
    val voiceCommandState = voiceCommandStateInt.asStateFlow()

    private val doorBellAlarmStateInt = MutableStateFlow(false)
    val doorBellAlarmState = doorBellAlarmStateInt.asStateFlow()

    private val asrPermissionsStateInt = MutableStateFlow(false)
    val asrPermissionsState = asrPermissionsStateInt.asStateFlow()

    private val asrRecognitionStateInt = MutableStateFlow<String?>(null)
    val asrRecognitionState = asrRecognitionStateInt.asStateFlow()

    private var isAsrSoundEnabled = ASR_SOUND_ENABLED_DEFAULT
    private var asrSoundVolume = ASR_SOUND_VOLUME_DEFAULT

    private var pushButtonState = PUSH_BUTTON_DEFAULT_STATE
    private var proximitySensorButtonState = PROXIMITY_SENSOR_DEFAULT_STATE

    private var pushButtonTopic = PUSH_BUTTON_DEFAULT_TOPIC
    private var pushButtonPayloadOn = PUSH_BUTTON_DEFAULT_PAYLOAD_ON
    private var pushButtonPayloadOff = PUSH_BUTTON_DEFAULT_PAYLOAD_OFF

    private var proximitySensorTopic = PROXIMITY_SENSOR_DEFAULT_TOPIC
    private var proximitySensorPayloadOn = PROXIMITY_SENSOR_DEFAULT_PAYLOAD_ON
    private var proximitySensorPayloadOff = PROXIMITY_SENSOR_DEFAULT_PAYLOAD_OFF
    private var proximitySensorButtonThreshold = false

    private var doorbellTopic = DOORBELL_ALARM_DEFAULT_TOPIC

    private var lightSensorTopic = LIGHT_SENSOR_TOPIC_DEFAULT

    private var clockAutoReturn = false
    private var clockAutoReturnTimeout = CLOCK_AUTO_RETURN_TIMEOUT_DEFAULT

    private var clockAutoReturnTimer: CountDownTimer? = null

    private var player = ExoPlayerImpl.getExoPlayer(
        context = context,
        audioAttributes = ExoPlayerImpl.getAudioAttributes()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getMQTTTopics()
            initMQTT()
        }
        initASR()
        initSensors()
    }

    fun resetDoorBellAlarmState() {
        doorBellAlarmStateInt.value = false
    }

    fun togglePressButton() {
        pushButtonState = !pushButtonState
        sendPressButtonEvent(pushButtonState)
    }

    private fun sendPressButtonEvent(
        isOn: Boolean
    ) {
        publishMQTT(
            topic = pushButtonTopic,
            messagePayload = if (isOn) {
                pushButtonPayloadOn
            } else {
                pushButtonPayloadOff
            }
        )
    }

    private fun sendProximityButtonEvent(
        isOn: Boolean
    ) {
        publishMQTT(
            topic = proximitySensorTopic,
            messagePayload = if (isOn) {
                proximitySensorPayloadOn
            } else {
                proximitySensorPayloadOff
            }
        )
    }

    private fun initClockAutoReturn() {
        viewModelScope.launch(Dispatchers.IO) {
           val data = dataStore.data.first()
            data[booleanPreferencesKey(PreferenceKey.CLOCK_AUTO_RETURN.key)]?.let {
                clockAutoReturn = it
            }
            data[floatPreferencesKey(PreferenceKey.CLOCK_AUTO_RETURN_TIMEOUT.key)]?.let {
                clockAutoReturnTimeout = it
            }
        }
    }

    private fun reStartClockAutoReturnTimer() {
        if (clockAutoReturnTimer != null) {
            clockAutoReturnTimer!!.cancel()
            clockAutoReturnTimer = null
        }
        clockAutoReturnTimer = object : CountDownTimer(clockAutoReturnTimeout.toLong() * 60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                currentPageStateInt.value = DashboardItem.CLOCK.ordinal
            }
        }
        clockAutoReturnTimer!!.start()
    }

    private suspend fun getMQTTTopics() {
        val data = dataStore.data.first()
        data[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_TOPIC.key)]?.let {
            pushButtonTopic = it.trim()
        }
        data[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_PAYLOAD_ON.key)]?.let {
            pushButtonPayloadOn = it.trim()
        }
        data[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_PAYLOAD_OFF.key)]?.let {
            pushButtonPayloadOff = it.trim()
        }
        data[stringPreferencesKey(PreferenceKey.DOORBELL_ALARM_TOPIC.key)]?.let {
            doorbellTopic = it.trim()
        }
        data[stringPreferencesKey(PreferenceKey.LIGHT_SENSOR_TOPIC.key)]?.let {
            lightSensorTopic = it.trim()
        }
        data[stringPreferencesKey(PreferenceKey.PROXIMITY_SENSOR_TOPIC.key)]?.let {
            proximitySensorTopic = it.trim()
        }
        data[stringPreferencesKey(PreferenceKey.PROXIMITY_SENSOR_PAYLOAD_ON.key)]?.let {
            proximitySensorPayloadOn = it.trim()
        }
        data[stringPreferencesKey(PreferenceKey.PROXIMITY_SENSOR_PAYLOAD_OFF.key)]?.let {
            proximitySensorPayloadOff = it.trim()
        }
    }

    private val mqttClientCallback = object : MqttCallbackExtended {
        override fun connectComplete(
            reconnect: Boolean,
            serverURI: String
        ) {
            if (reconnect) {
                Log.d("MQTT", "Reconnected: $serverURI")
            } else {
                Log.d("MQTT","Connected: $serverURI")
            }
        }

        override fun connectionLost(
            cause: Throwable?
        ) {
            Log.d("MQTT", "The Connection was lost.")
        }

        override fun messageArrived(
            topic: String,
            message: MqttMessage
        ) {
            Log.d("MQTT", "messageArrived: $topic: $message")
            if (topic == doorbellTopic) {
                doorBellAlarmStateInt.value = true
            }
        }

        override fun deliveryComplete(
            token: IMqttDeliveryToken
        ) {
        }
    }

    @UnstableApi
    private fun initMQTT() {
        val mqttServer = getMQTTServerCredentials(dataStore)
        mqttClient.apply {
            setForegroundService(
                notification = getForegroundNotification(
                    context = context
                )
            )
        }
        val mqttConnectOptions = MqttConnectOptions().apply {
            userName = mqttServer.login.trim()
            password = mqttServer.password.trim().toCharArray()
            isAutomaticReconnect = true
            isCleanSession = false
        }
        val mqttClientListener = object : IMqttActionListener {
            override fun onSuccess(
                asyncActionToken: IMqttToken
            ) {
                val disconnectedBufferOptions = DisconnectedBufferOptions().apply {
                    isBufferEnabled = true
                    bufferSize = 100
                    isPersistBuffer = false
                    isDeleteOldestMessages = false
                }
                mqttClient.setBufferOpts(disconnectedBufferOptions)
                mqttClient.addCallback(mqttClientCallback)
            }

            override fun onFailure(
                asyncActionToken: IMqttToken?,
                exception: Throwable?
            ) {
                Log.d("MQTT", "Failed to connect")
            }
        }
        mqttClient.connect(
            options = mqttConnectOptions,
            userContext = null,
            callback = mqttClientListener
        )
    }

    private fun getMQTTServerCredentials(
        dataStore: DataStore<Preferences>,
    ) : MQTTServer {
        return runBlocking {
            val data = dataStore.data.first()
            var login = ""
            data[stringPreferencesKey(PreferenceKey.MQTT_BROKER_USERNAME.key)]?.let {
                login = it
            }
            var password = ""
            data[stringPreferencesKey(PreferenceKey.MQTT_BROKER_PASSWORD.key)]?.let {
                password = it
            }
            return@runBlocking MQTTServer(
                login = login,
                password = password
            )
        }
    }

    @UnstableApi
    private fun initASR() {
        viewModelScope.launch {
            val data = dataStore.data.first()
            var isEnabled = false
            data[booleanPreferencesKey(PreferenceKey.ASR_ENABLED.key)]?.let {
                isEnabled = it
            }
            if (isEnabled) {
                asrPermissionsStateInt.value = true
            } else {
                asrPermissionsStateInt.value = false
                context.stopService(Intent(context, SpeechRecognitionService::class.java))
            }
            data[booleanPreferencesKey(PreferenceKey.ASR_SOUND_ENABLED.key)]?.let {
                isAsrSoundEnabled = it
            }
            data[floatPreferencesKey(PreferenceKey.ASR_SOUND_VOLUME.key)]?.let {
                asrSoundVolume = it
            }
        }

        viewModelScope.launch {
            speechRecognitionHandler.isServiceStarted.asStateFlow().collect { isStarted ->
                if (isStarted) {
                    val speechRecognitionState = speechRecognitionHandler.speechRecognitionState!!.stateIn(
                        initialValue = SpeechRecognitionState.Initial,
                        scope = viewModelScope,
                        started = WhileSubscribed(5000)
                    )
                    speechRecognitionState.collect { state ->
                        Log.d("DashboardViewModel", state.toString())
                        when (state) {
                            is SpeechRecognitionState.Initial,
                                SpeechRecognitionState.Ready -> {
                                asrRecognitionStateInt.value = null
                            }
                            is SpeechRecognitionState.Result -> {
                                asrRecognitionStateInt.value = state.word
                                processCommand(
                                    context = context,
                                    command = state.word,
                                    onPageChanged = {pageId, command ->
                                        currentPageStateInt.value = pageId
                                        voiceCommandStateInt.value = command ?: VoiceCommand.CLOCK
                                        resetAsrState()
                                        onPageChanged(pageId)
                                    },
                                    onError = {
                                        if (isAsrSoundEnabled) {
                                            playAssetSound(
                                                player = player,
                                                audioType = AudioType.ERROR,
                                                soundVolume = asrSoundVolume
                                            )
                                        }
                                        resetAsrState()
                                    },
                                    onPressButton = {
                                        sendPressButtonEvent(it)
                                        resetAsrState()
                                    },
                                    onPressButton2 = {
                                        sendProximityButtonEvent(it)
                                        resetAsrState()
                                    }
                                )

                            }
                            SpeechRecognitionState.WakeWordDetected -> {
                                asrRecognitionStateInt.value = ""
                                if (isAsrSoundEnabled) {
                                    playAssetSound(
                                        player = player,
                                        audioType = AudioType.WAKE_WORD,
                                        soundVolume = asrSoundVolume
                                    )
                                }
                            }
                            is SpeechRecognitionState.Error -> {
                                if (isAsrSoundEnabled) {
                                    playAssetSound(
                                        player = player,
                                        audioType = AudioType.ERROR,
                                        soundVolume = asrSoundVolume
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun resetAsrState() {
        runBlocking {
            delay(SHOW_ASR_RESULT_TIMEOUT)
            asrRecognitionStateInt.value = null
        }
    }

    fun startAsrService() {
        asrPermissionsStateInt.value = false

        viewModelScope.launch {
            ContextCompat.startForegroundService(
                context,
                Intent(context, SpeechRecognitionService::class.java)
            )
        }
    }

    fun disableAsr() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(PreferenceKey.ASR_ENABLED.key)] = false
            }
        }
    }

    fun cancelAsrAction() {
        asrRecognitionStateInt.value = null
    }

    fun onPageChanged(pageId: Int) {
        viewModelScope.launch {
            currentPageStateInt.value = pageId
            if (pageId == DashboardItem.CLOCK.ordinal) {
                clockAutoReturnTimer?.cancel()
            } else {
                initClockAutoReturn()
                if (clockAutoReturn) {
                    reStartClockAutoReturnTimer()
                }
            }
        }
    }

    private fun initSensors() {
        viewModelScope.launch {
            ContextCompat.startForegroundService(
                context,
                Intent(context, SensorService::class.java)
            )
        }
        viewModelScope.launch {
            sensorHandler.isServiceStarted.asStateFlow().collect { isStarted ->
                if (isStarted) {
                    val lightSensorState = sensorHandler.lightSensorState?.stateIn(
                        initialValue = 0,
                        scope = viewModelScope,
                        started = WhileSubscribed(5000)
                    )
                    lightSensorState?.collect { state ->
                        publishMQTT(
                            topic = lightSensorTopic,
                            messagePayload = state.toString()
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            sensorHandler.isServiceStarted.asStateFlow().collect { isStarted ->
                if (isStarted) {
                    val proximitySensorState = sensorHandler.proximitySensorState?.stateIn(
                        initialValue = 0,
                        scope = viewModelScope,
                        started = WhileSubscribed(5000)
                    )
                    proximitySensorState?.collect { state ->
                        Log.d("DashboardViewModel: proximitySensorState", state.toString())
                        if ((state > PROXIMITY_SENSOR_THRESHOLD)
                            && !proximitySensorButtonThreshold) {
                            proximitySensorButtonThreshold = true
                            proximitySensorButtonState = !proximitySensorButtonState
                            sendProximityButtonEvent(proximitySensorButtonState)
                        }
                        if (state < PROXIMITY_SENSOR_THRESHOLD) {
                            proximitySensorButtonThreshold = false
                        }
                    }
                }
            }
        }
    }

    private fun publishMQTT(topic: String, messagePayload: String) {
        if (!mqttClient.isConnected) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            mqttClient.publish(
                topic = topic,
                message = MqttMessage().apply {
                    payload = messagePayload.toByteArray()
                },
                userContext = null,
                callback = object: IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d("DashboardViewModel", "publishMQTT Ok")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d("DashboardViewModel", "publishMQTT Error")
                    }
                }
            )
        }
    }
}

const val APP_CHANNEL = "SmartDisplaychannnel"
const val PUSH_BUTTON_DEFAULT_TOPIC = "pushbutton"
const val PUSH_BUTTON_DEFAULT_STATE = false
const val PUSH_BUTTON_DEFAULT_PAYLOAD_ON = "1"
const val PUSH_BUTTON_DEFAULT_PAYLOAD_OFF = "0"
const val PROXIMITY_SENSOR_DEFAULT_TOPIC = "pushbutton2"
const val PROXIMITY_SENSOR_DEFAULT_STATE = false
const val PROXIMITY_SENSOR_DEFAULT_PAYLOAD_ON = "1"
const val PROXIMITY_SENSOR_DEFAULT_PAYLOAD_OFF = "0"
const val PROXIMITY_SENSOR_THRESHOLD = 500
const val SHOW_ASR_RESULT_TIMEOUT = 2000L //2s
