package com.smsoft.smartdisplay.ui.screen.dashboard.controller

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.data.LightBrightnessType
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_COMMAND_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_COMMAND_OFF_DEFAULT_PAYLOAD
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_COMMAND_ON_DEFAULT_PAYLOAD
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_COMMAND_ON_OFF_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.screen.dashboard.DIMMER_LIGHT_DEFAULT_BRIGHTNESS
import com.smsoft.smartdisplay.ui.screen.dashboard.DIMMER_LIGHT_STEP_PERCENT
import com.smsoft.smartdisplay.ui.screen.dashboard.mqtt.DashboardMqttManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DimmerLightController(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val mqttManager: DashboardMqttManager,
    private val scope: CoroutineScope,
) {
    private var onOffTopic = ALARM_LIGHT_DIMMER_COMMAND_ON_OFF_DEFAULT_TOPIC
    private var payloadOn = ALARM_LIGHT_DIMMER_COMMAND_ON_DEFAULT_PAYLOAD
    private var payloadOff = ALARM_LIGHT_DIMMER_COMMAND_OFF_DEFAULT_PAYLOAD
    private var levelTopic = ALARM_LIGHT_DIMMER_COMMAND_DEFAULT_TOPIC
    private var brightness = DIMMER_LIGHT_DEFAULT_BRIGHTNESS

    fun start() {
        scope.launch(Dispatchers.IO) {
            val data = dataStore.data.first()
            data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_ON_OFF_TOPIC.key)]?.let {
                onOffTopic = it.trim()
            }
            data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_ON_PAYLOAD.key)]?.let {
                payloadOn = it.trim()
            }
            data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_OFF_PAYLOAD.key)]?.let {
                payloadOff = it.trim()
            }
            data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_TOPIC.key)]?.let {
                levelTopic = it.trim()
            }
            data[intPreferencesKey(PreferenceKey.DIMMER_LIGHT_BRIGHTNESS.key)]?.let {
                brightness = it
            }
        }
    }

    fun sendPowerEvent(isOn: Boolean) {
        mqttManager.publish(scope, onOffTopic, if (isOn) payloadOn else payloadOff)
        if (isOn) {
            mqttManager.publish(scope, levelTopic, brightness.toString())
        }
    }

    fun sendLevelEvent(percent: Int) {
        val clamped = percent.coerceIn(0, 100)
        brightness = clamped
        persistBrightness(clamped)
        mqttManager.publish(scope, onOffTopic, payloadOn)
        mqttManager.publish(scope, levelTopic, clamped.toString())
    }

    fun processSetCommand(remainder: String) {
        val level = LightBrightnessType.getByCommand(
            context = context,
            command = remainder
        ) ?: return
        sendLevelEvent(level.percent)
    }

    fun processStepCommand(direction: Int) {
        sendLevelEvent(brightness + direction * DIMMER_LIGHT_STEP_PERCENT)
    }

    private fun persistBrightness(percent: Int) {
        scope.launch(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[intPreferencesKey(PreferenceKey.DIMMER_LIGHT_BRIGHTNESS.key)] = percent
            }
        }
    }
}
