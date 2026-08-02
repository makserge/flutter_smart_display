package com.smsoft.smartdisplay.ui.screen.dashboard.controller

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.service.alarm.AlarmHandler
import com.smsoft.smartdisplay.service.sensor.SensorHandler
import com.smsoft.smartdisplay.service.sensor.SensorService
import com.smsoft.smartdisplay.ui.composable.settings.LIGHT_SENSOR_TOPIC_DEFAULT
import com.smsoft.smartdisplay.ui.screen.dashboard.PROXIMITY_SENSOR_DEFAULT_PAYLOAD_OFF
import com.smsoft.smartdisplay.ui.screen.dashboard.PROXIMITY_SENSOR_DEFAULT_PAYLOAD_ON
import com.smsoft.smartdisplay.ui.screen.dashboard.PROXIMITY_SENSOR_DEFAULT_STATE
import com.smsoft.smartdisplay.ui.screen.dashboard.PROXIMITY_SENSOR_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.screen.dashboard.PROXIMITY_SENSOR_MIN_DETECTION_DURATION
import com.smsoft.smartdisplay.ui.screen.dashboard.PROXIMITY_SENSOR_THRESHOLD
import com.smsoft.smartdisplay.ui.screen.dashboard.mqtt.DashboardMqttManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SensorRelayController(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val mqttManager: DashboardMqttManager,
    private val sensorHandler: SensorHandler,
    private val alarmHandler: AlarmHandler,
    private val scope: CoroutineScope,
) {
    private var lightSensorTopic = LIGHT_SENSOR_TOPIC_DEFAULT

    private var proximitySensorTopic = PROXIMITY_SENSOR_DEFAULT_TOPIC
    private var proximitySensorPayloadOn = PROXIMITY_SENSOR_DEFAULT_PAYLOAD_ON
    private var proximitySensorPayloadOff = PROXIMITY_SENSOR_DEFAULT_PAYLOAD_OFF

    private var proximityButtonState = PROXIMITY_SENSOR_DEFAULT_STATE
    private var proximityButtonThresholdCrossed = false
    private var proximityLongDetectionJob: Job? = null

    fun start() {
        scope.launch(Dispatchers.IO) {
            val data = dataStore.data.first()
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

        scope.launch {
            ContextCompat.startForegroundService(
                context,
                Intent(context, SensorService::class.java)
            )
        }

        scope.launch {
            sensorHandler.isServiceStarted.asStateFlow().collect { isStarted ->
                if (isStarted) {
                    observeLightSensor()
                }
            }
        }

        scope.launch {
            sensorHandler.isServiceStarted.asStateFlow().collect { isStarted ->
                if (isStarted) {
                    observeProximitySensor()
                }
            }
        }
    }

        fun sendProximityButtonEvent(isOn: Boolean) {
        mqttManager.publish(
            scope = scope,
            topic = proximitySensorTopic,
            messagePayload = if (isOn) proximitySensorPayloadOn else proximitySensorPayloadOff
        )
    }

    fun release() {
        proximityLongDetectionJob?.cancel()
    }

    private suspend fun observeLightSensor() {
        val lightSensorState = sensorHandler.lightSensorState?.stateIn(
            initialValue = 0,
            scope = scope,
            started = WhileSubscribed(5000)
        )
        lightSensorState?.collect { state ->
            if (state > 0) {
                alarmHandler.lightSensorState.emit(state)
                mqttManager.publish(
                    scope = scope,
                    topic = lightSensorTopic,
                    messagePayload = state.toString()
                )
            }
        }
    }

    private suspend fun observeProximitySensor() {
        val proximitySensorState = sensorHandler.proximitySensorState?.stateIn(
            initialValue = 0,
            scope = scope,
            started = WhileSubscribed(5000)
        )
        proximitySensorState?.collect { state ->
            if ((state > PROXIMITY_SENSOR_THRESHOLD) && !proximityButtonThresholdCrossed) {
                proximityButtonThresholdCrossed = true
                proximityLongDetectionJob?.cancel()
                proximityLongDetectionJob = scope.launch {
                    delay(PROXIMITY_SENSOR_MIN_DETECTION_DURATION)
                    proximityButtonState = !proximityButtonState
                    sendProximityButtonEvent(proximityButtonState)
                }
            } else if (state < PROXIMITY_SENSOR_THRESHOLD) {
                proximityButtonThresholdCrossed = false
                proximityLongDetectionJob?.cancel()
                proximityLongDetectionJob = null
            }
        }
    }
}
