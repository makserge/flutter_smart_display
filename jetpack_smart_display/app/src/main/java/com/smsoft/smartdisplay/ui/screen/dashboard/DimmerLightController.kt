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
import com.smsoft.smartdisplay.utils.observe
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
        // Follows the topic settings: they used to apply only after a restart.
        dataStore.observe(scope, ::readDimmerConfig) {
            onOffTopic = it.onOffTopic
            payloadOn = it.payloadOn
            payloadOff = it.payloadOff
            levelTopic = it.levelTopic
        }
        // The brightness is this controller's own state (no setting): read once, so that a late
        // echo of its own write cannot replace a newer value.
        scope.launch(Dispatchers.IO) {
            dataStore.data.first()[intPreferencesKey(PreferenceKey.DIMMER_LIGHT_BRIGHTNESS.key)]?.let {
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

private data class DimmerConfig(
    val onOffTopic: String,
    val payloadOn: String,
    val payloadOff: String,
    val levelTopic: String
)

private fun readDimmerConfig(data: Preferences) = DimmerConfig(
    onOffTopic = data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_ON_OFF_TOPIC.key)]?.trim() ?: ALARM_LIGHT_DIMMER_COMMAND_ON_OFF_DEFAULT_TOPIC,
    payloadOn = data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_ON_PAYLOAD.key)]?.trim() ?: ALARM_LIGHT_DIMMER_COMMAND_ON_DEFAULT_PAYLOAD,
    payloadOff = data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_OFF_PAYLOAD.key)]?.trim() ?: ALARM_LIGHT_DIMMER_COMMAND_OFF_DEFAULT_PAYLOAD,
    levelTopic = data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_TOPIC.key)]?.trim() ?: ALARM_LIGHT_DIMMER_COMMAND_DEFAULT_TOPIC
)
