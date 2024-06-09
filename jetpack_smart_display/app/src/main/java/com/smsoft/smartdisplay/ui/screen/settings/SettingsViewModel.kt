package com.smsoft.smartdisplay.ui.screen.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.data.ClockType
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionService
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_SENSOR_THRESHOLD_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.LIGHT_SENSOR_INTERVAL_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.LIGHT_SENSOR_TOPIC_DEFAULT
import com.smsoft.smartdisplay.ui.screen.dashboard.PROXIMITY_SENSOR_DEFAULT_PAYLOAD_OFF
import com.smsoft.smartdisplay.ui.screen.dashboard.PROXIMITY_SENSOR_DEFAULT_PAYLOAD_ON
import com.smsoft.smartdisplay.ui.screen.dashboard.PROXIMITY_SENSOR_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_DEFAULT_PAYLOAD_OFF
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_DEFAULT_PAYLOAD_ON
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_DEFAULT_TOPIC
import com.smsoft.smartdisplay.utils.getParamFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import info.mqtt.android.service.MqttAndroidClient
import info.mqtt.android.service.QoS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
@UnstableApi
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val dataStore: DataStore<Preferences>,
    private val mqttClient: MqttAndroidClient
) : ViewModel() {

    private val asrPermissionsStateInt = MutableStateFlow(false)
    val asrPermissionsState = asrPermissionsStateInt.asStateFlow()

    val clockType = getParamFlow(
        dataStore = dataStore,
        defaultValue = ClockType.getDefaultId()
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.CLOCK_TYPE.key)] ?: ClockType.getDefaultId() }

    val cityLat = getParamFlow(
        dataStore = dataStore,
        defaultValue = ""
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.WEATHER_CITY_LAT.key)] ?: WEATHER_CITY_DEFAULT_LAT }

    val cityLon = getParamFlow(
        dataStore = dataStore,
        defaultValue = ""
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.WEATHER_CITY_LON.key)] ?: WEATHER_CITY_DEFAULT_LON }

    val mqttHost = getParamFlow(
        dataStore = dataStore,
        defaultValue = MQTT_SERVER_DEFAULT_HOST
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.MQTT_BROKER_HOST.key)] ?: MQTT_SERVER_DEFAULT_HOST }

    val mqttPort = getParamFlow(
        dataStore = dataStore,
        defaultValue = MQTT_SERVER_DEFAULT_PORT
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.MQTT_BROKER_PORT.key)] ?: MQTT_SERVER_DEFAULT_PORT }

    val mqttUserName = getParamFlow(
        dataStore = dataStore,
        defaultValue = ""
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.MQTT_BROKER_USERNAME.key)] ?: "" }

    val mqttPassword = getParamFlow(
        dataStore = dataStore,
        defaultValue = ""
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.MQTT_BROKER_PASSWORD.key)] ?: "" }

    val mpdHost = getParamFlow(
        dataStore = dataStore,
        defaultValue = MPD_SERVER_DEFAULT_HOST
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.MPD_SERVER_HOST.key)] ?: "" }

    val mpdPort = getParamFlow(
        dataStore = dataStore,
        defaultValue = MPD_SERVER_DEFAULT_PORT
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.MPD_SERVER_PORT.key)] ?: MPD_SERVER_DEFAULT_PORT }

    val mpdPassword = getParamFlow(
        dataStore = dataStore,
        defaultValue = ""
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.MPD_SERVER_PASSWORD.key)] ?: "" }

    val doorbellAlarmTopic = getParamFlow(
        dataStore = dataStore,
        defaultValue = ""
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.DOORBELL_ALARM_TOPIC.key)] ?: "" }

    val doorbellStreamURL = getParamFlow(
        dataStore = dataStore,
        defaultValue = DOORBELL_STREAM_DEFAULT_URL
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.DOORBELL_STREAM_URL.key)] ?: DOORBELL_STREAM_DEFAULT_URL }

    val pushButtonTopic = getParamFlow(
        dataStore = dataStore,
        defaultValue = PUSH_BUTTON_DEFAULT_TOPIC
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_TOPIC.key)] ?: PUSH_BUTTON_DEFAULT_TOPIC }

    val pushButtonPayloadOn = getParamFlow(
        dataStore = dataStore,
        defaultValue = PUSH_BUTTON_DEFAULT_PAYLOAD_ON
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_PAYLOAD_ON.key)] ?: PUSH_BUTTON_DEFAULT_PAYLOAD_ON }

    val pushButtonPayloadOff = getParamFlow(
        dataStore = dataStore,
        defaultValue = PUSH_BUTTON_DEFAULT_PAYLOAD_OFF
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_PAYLOAD_OFF.key)] ?: PUSH_BUTTON_DEFAULT_PAYLOAD_OFF }

    val proximitySensorTopic = getParamFlow(
        dataStore = dataStore,
        defaultValue = PROXIMITY_SENSOR_DEFAULT_TOPIC
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.PROXIMITY_SENSOR_TOPIC.key)] ?: PROXIMITY_SENSOR_DEFAULT_TOPIC }

    val proximitySensorPayloadOn = getParamFlow(
        dataStore = dataStore,
        defaultValue = PROXIMITY_SENSOR_DEFAULT_PAYLOAD_ON
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.PROXIMITY_SENSOR_PAYLOAD_ON.key)] ?: PROXIMITY_SENSOR_DEFAULT_PAYLOAD_ON }

    val proximitySensorPayloadOff = getParamFlow(
        dataStore = dataStore,
        defaultValue = PROXIMITY_SENSOR_DEFAULT_PAYLOAD_OFF
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.PROXIMITY_SENSOR_PAYLOAD_OFF.key)] ?: PROXIMITY_SENSOR_DEFAULT_PAYLOAD_OFF }

    val lightSensorTopic = getParamFlow(
        dataStore = dataStore,
        defaultValue = LIGHT_SENSOR_TOPIC_DEFAULT
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.LIGHT_SENSOR_TOPIC.key)] ?: LIGHT_SENSOR_TOPIC_DEFAULT }

    val lightSensorInterval = getParamFlow(
        dataStore = dataStore,
        defaultValue = LIGHT_SENSOR_INTERVAL_DEFAULT
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.LIGHT_SENSOR_INTERVAL.key)] ?: LIGHT_SENSOR_INTERVAL_DEFAULT }

    val lightSensorThreshold = getParamFlow(
        dataStore = dataStore,
        defaultValue = ALARM_LIGHT_SENSOR_THRESHOLD_DEFAULT
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_SENSOR_THRESHOLD.key)] ?: ALARM_LIGHT_SENSOR_THRESHOLD_DEFAULT }

    init {
        updateDoorbellAlarmTopic()
        updateAsrServiceState()
    }

    private fun updateDoorbellAlarmTopic() {
        if (!mqttClient.isConnected) {
            return
        }
        viewModelScope.launch {
            var prevValue = ""
            doorbellAlarmTopic.collectLatest { newValue ->
                if (newValue == prevValue) {
                    return@collectLatest
                }
                if (prevValue.isNotEmpty()) {
                    mqttClient.unsubscribe(
                        topic = prevValue
                    )
                }
                mqttClient.subscribe(
                    topic = (newValue as String).trim(),
                    qos = QoS.AtMostOnce.value
                )
                prevValue = newValue.trim()
            }
        }
    }

    @UnstableApi
    private fun updateAsrServiceState() {
        viewModelScope.launch {
            val enabled = getParamFlow(
                dataStore = dataStore,
                defaultValue = false
            ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.ASR_ENABLED.key)] ?: false }
            enabled.collectLatest { newValue ->
                if (newValue as Boolean) {
                    asrPermissionsStateInt.value = true
                } else {
                    asrPermissionsStateInt.value = false
                    context.stopService(Intent(context, SpeechRecognitionService::class.java))
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
}

const val CLOCK_AUTO_RETURN_TIMEOUT_DEFAULT = 5F
const val MQTT_SERVER_DEFAULT_HOST = "localhost"
const val MQTT_SERVER_DEFAULT_PORT = "1883"
const val MPD_SERVER_DEFAULT_HOST = ""
const val MPD_SERVER_DEFAULT_PORT = "6600"
const val DOORBELL_ALARM_DEFAULT_TOPIC = "doorbell"
const val DOORBELL_STREAM_DEFAULT_URL = ""
const val DOORBELL_BACK_TIMER_DEFAULT_DELAY = 10F
const val WEATHER_CITY_DEFAULT_LAT = "48.137428"
const val WEATHER_CITY_DEFAULT_LON = "11.57549"
