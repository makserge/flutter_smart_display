package com.smsoft.smartdisplay.ui.screen.dashboard.controller

import android.content.Context
import android.content.Intent
import android.util.Log
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
import com.smsoft.smartdisplay.utils.observe

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
        // Follows the topic settings: they used to apply only after a restart.
        dataStore.observe(scope, ::readRelayConfig) {
            lightSensorTopic = it.lightSensorTopic
            proximitySensorTopic = it.proximitySensorTopic
            proximitySensorPayloadOn = it.proximitySensorPayloadOn
            proximitySensorPayloadOff = it.proximitySensorPayloadOff
        }

        scope.launch {
            // A plain start while the dashboard is visible: the service promotes itself to the
            // foreground and keeps working as a normal service if Android refuses that. With
            // startForegroundService() a refused promotion was fatal ("did not call startForeground").
            try {
                context.startService(Intent(context, SensorService::class.java))
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Sensor service not started", e)
            }
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
            // 0 = no current reading (sensor switched off): the alarm light must not act on the
            // last reading from before.
            alarmHandler.lightSensorState.emit(state)
            if (state > 0) {
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

private const val TAG = "SensorRelayController"

private data class RelayConfig(
    val lightSensorTopic: String,
    val proximitySensorTopic: String,
    val proximitySensorPayloadOn: String,
    val proximitySensorPayloadOff: String
)

private fun readRelayConfig(data: Preferences) = RelayConfig(
    lightSensorTopic = data[stringPreferencesKey(PreferenceKey.LIGHT_SENSOR_TOPIC.key)]?.trim() ?: LIGHT_SENSOR_TOPIC_DEFAULT,
    proximitySensorTopic = data[stringPreferencesKey(PreferenceKey.PROXIMITY_SENSOR_TOPIC.key)]?.trim() ?: PROXIMITY_SENSOR_DEFAULT_TOPIC,
    proximitySensorPayloadOn = data[stringPreferencesKey(PreferenceKey.PROXIMITY_SENSOR_PAYLOAD_ON.key)]?.trim() ?: PROXIMITY_SENSOR_DEFAULT_PAYLOAD_ON,
    proximitySensorPayloadOff = data[stringPreferencesKey(PreferenceKey.PROXIMITY_SENSOR_PAYLOAD_OFF.key)]?.trim() ?: PROXIMITY_SENSOR_DEFAULT_PAYLOAD_OFF
)
