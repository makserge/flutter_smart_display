package com.smsoft.smartdisplay.ui.screen.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smsoft.smartdisplay.data.ClockType
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.utils.getParamFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import info.mqtt.android.service.MqttAndroidClient
import info.mqtt.android.service.QoS
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val dataStore: DataStore<Preferences>,
    private val mqttClient: MqttAndroidClient
) : ViewModel() {

    val clockType = getParamFlow(
        dataStore = dataStore,
        defaultValue = ClockType.getDefaultId()
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.CLOCK_TYPE.key)] ?: ClockType.getDefaultId() }

    val cityLat = getParamFlow(
        dataStore = dataStore,
        defaultValue = ""
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.WEATHER_CITY_LAT.key)] ?: "" }

    val cityLon = getParamFlow(
        dataStore = dataStore,
        defaultValue = ""
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.WEATHER_CITY_LON.key)] ?: "" }

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
        defaultValue = ""
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_TOPIC.key)] ?: "" }

    init {
        updateDoorbellAlarmTopic()
    }

    private fun updateDoorbellAlarmTopic() {
        if (!mqttClient.isConnected) {
            return
        }
        viewModelScope.launch {
            var prevValue = ""
            doorbellAlarmTopic.collect { newValue ->
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
}

const val MQTT_SERVER_DEFAULT_HOST = "localhost"
const val MQTT_SERVER_DEFAULT_PORT = "1883"
const val MPD_SERVER_DEFAULT_HOST = ""
const val MPD_SERVER_DEFAULT_PORT = "6600"
const val DOORBELL_STREAM_DEFAULT_URL = "rtsp://*"
const val DOORBELL_ALARM_DEFAULT_TOPIC = "doorbell"
