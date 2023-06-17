package com.smsoft.smartdisplay.ui.screen.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.data.MQTTServer
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionHandler
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionService
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionState
import com.smsoft.smartdisplay.ui.screen.settings.DOORBELL_ALARM_DEFAULT_TOPIC
import com.smsoft.smartdisplay.utils.getForegroundNotification
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.Dispatchers
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
    private val dataStore: DataStore<Preferences>,
    private val mqttClient: MqttAndroidClient,
    private val speechRecognitionHandler: SpeechRecognitionHandler
) : ViewModel() {
    private val doorBellAlarmStateInt = MutableStateFlow(false)
    val doorBellAlarmState = doorBellAlarmStateInt.asStateFlow()

    private val asrPermissionsStateInt = MutableStateFlow(false)
    val asrPermissionsState = asrPermissionsStateInt.asStateFlow()

    private val asrRecognitionStateInt = MutableStateFlow<String?>(null)
    val asrRecognitionState = asrRecognitionStateInt.asStateFlow()


    private var pushButtonTopic = PUSH_BUTTON_DEFAULT_TOPIC
    private var doorbellTopic = DOORBELL_ALARM_DEFAULT_TOPIC


    init {
        viewModelScope.launch(Dispatchers.IO) {
            getMQTTTopics()
            initMQTT()
        }
        initASR()
    }

    fun resetDoorBellAlarmState() {
        doorBellAlarmStateInt.value = false
    }

    fun sendPressButtonEvent() {
        if (!mqttClient.isConnected) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val message = MqttMessage().apply {
                payload = MQTT_PRESS_BUTTON_MESSAGE.toByteArray()
            }
            mqttClient.publish(
                topic = pushButtonTopic,
                message = message,
                userContext = null,
                callback = object: IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d("DashboardViewModel", "sendPressButtonEvent Ok")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d("DashboardViewModel", "sendPressButtonEvent Error")
                    }
                }
            )
        }
    }

    private suspend fun getMQTTTopics() {
        val data = dataStore.data.first()
        data[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_TOPIC.key)]?.let {
            pushButtonTopic = it.trim()
        }
        data[stringPreferencesKey(PreferenceKey.DOORBELL_ALARM_TOPIC.key)]?.let {
            doorbellTopic = it.trim()
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
                            is SpeechRecognitionState.Error -> {

                            }
                            SpeechRecognitionState.Initial -> {

                            }
                            SpeechRecognitionState.Ready -> {

                            }
                            is SpeechRecognitionState.Result -> {
                                asrRecognitionStateInt.value = state.word
                            }
                            SpeechRecognitionState.WakeWordDetected -> {
                                asrRecognitionStateInt.value = ""
                            }
                        }
                    }
                }
            }
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

    private val MQTT_PRESS_BUTTON_MESSAGE = "1"
}

const val APP_CHANNEL = "SmartDisplaychannnel"
const val PUSH_BUTTON_DEFAULT_TOPIC = "pushbutton"