package com.smsoft.smartdisplay.ui.screen.dashboard.controller

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.screen.dashboard.CINEMA_MODE_COMMAND_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.screen.dashboard.CINEMA_MODE_DEFAULT_PAYLOAD_OFF
import com.smsoft.smartdisplay.ui.screen.dashboard.CINEMA_MODE_DEFAULT_PAYLOAD_ON
import com.smsoft.smartdisplay.ui.screen.dashboard.mqtt.DashboardMqttManager
import com.smsoft.smartdisplay.utils.observe
import kotlinx.coroutines.CoroutineScope

/**
 * The cinema mode, switched by voice ("включи режим кино", "отключи режим кино"): one MQTT topic
 * with an "on" and an "off" payload, all three from Settings (by default
 * "ha_automations/cinema_mode", "1" and "0"). With an empty topic nothing is sent. As every
 * command, it is sent only while the broker is
 * connected (DashboardMqttManager.canPublish); there is no state on the screen to keep in step.
 */
class CinemaModeController(
    private val dataStore: DataStore<Preferences>,
    private val mqttManager: DashboardMqttManager,
    private val scope: CoroutineScope,
) {
    private var commandTopic = CINEMA_MODE_COMMAND_DEFAULT_TOPIC
    private var payloadOn = CINEMA_MODE_DEFAULT_PAYLOAD_ON
    private var payloadOff = CINEMA_MODE_DEFAULT_PAYLOAD_OFF

    /** Follows the cinema mode settings. */
    fun start() {
        dataStore.observe(scope, ::readCinemaModeConfig) {
            commandTopic = it.commandTopic
            payloadOn = it.payloadOn
            payloadOff = it.payloadOff
        }
    }

    fun sendCommand(isOn: Boolean) {
        if (!mqttManager.canPublish(commandTopic)) {
            Log.i(TAG, "Cinema mode ${if (isOn) "on" else "off"} not sent: no topic set, or the broker is not connected")
            return
        }
        mqttManager.publish(
            scope = scope,
            topic = commandTopic,
            messagePayload = if (isOn) payloadOn else payloadOff
        )
    }
}

private data class CinemaModeConfig(
    val commandTopic: String,
    val payloadOn: String,
    val payloadOff: String
)

private fun readCinemaModeConfig(data: Preferences) = CinemaModeConfig(
    commandTopic = data[stringPreferencesKey(PreferenceKey.CINEMA_MODE_COMMAND_TOPIC.key)]?.trim() ?: CINEMA_MODE_COMMAND_DEFAULT_TOPIC,
    payloadOn = data[stringPreferencesKey(PreferenceKey.CINEMA_MODE_PAYLOAD_ON.key)]?.trim() ?: CINEMA_MODE_DEFAULT_PAYLOAD_ON,
    payloadOff = data[stringPreferencesKey(PreferenceKey.CINEMA_MODE_PAYLOAD_OFF.key)]?.trim() ?: CINEMA_MODE_DEFAULT_PAYLOAD_OFF
)

private const val TAG = "CinemaModeController"
