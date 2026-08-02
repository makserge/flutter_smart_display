package com.smsoft.smartdisplay.ui.screen.dashboard.controller

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_COMMAND_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_DEFAULT_PAYLOAD_OFF
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_DEFAULT_PAYLOAD_ON
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_DEFAULT_STATE
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_ON_PAYLOAD
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_STATUS_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.screen.dashboard.mqtt.DashboardMqttManager
import com.smsoft.smartdisplay.ui.screen.dashboard.mqtt.DashboardMqttMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PushButtonController(
    private val dataStore: DataStore<Preferences>,
    private val mqttManager: DashboardMqttManager,
    private val scope: CoroutineScope,
) {
    private val pushButtonStateInt = MutableStateFlow(PUSH_BUTTON_DEFAULT_STATE)
    val pushButtonState = pushButtonStateInt.asStateFlow()

    private var statusTopic = PUSH_BUTTON_STATUS_DEFAULT_TOPIC
    private var commandTopic = PUSH_BUTTON_COMMAND_DEFAULT_TOPIC
    private var payloadOn = PUSH_BUTTON_DEFAULT_PAYLOAD_ON
    private var payloadOff = PUSH_BUTTON_DEFAULT_PAYLOAD_OFF

    fun start() {
        scope.launch(Dispatchers.IO) {
            val data = dataStore.data.first()
            data[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_STATUS_TOPIC.key)]?.let {
                statusTopic = it.trim()
            }
            data[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_COMMAND_TOPIC.key)]?.let {
                commandTopic = it.trim()
            }
            data[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_PAYLOAD_ON.key)]?.let {
                payloadOn = it.trim()
            }
            data[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_PAYLOAD_OFF.key)]?.let {
                payloadOff = it.trim()
            }
        }
    }

        fun toggle() {
        val newState = !pushButtonStateInt.value
        pushButtonStateInt.value = newState
        publishCommand(newState)
    }

        fun sendCommand(isOn: Boolean) {
        publishCommand(isOn)
    }

    fun onMqttMessage(message: DashboardMqttMessage) {
        if (message.topic == statusTopic) {
            pushButtonStateInt.value = (message.payload == PUSH_BUTTON_ON_PAYLOAD)
        }
    }

    private fun publishCommand(isOn: Boolean) {
        mqttManager.publish(
            scope = scope,
            topic = commandTopic,
            messagePayload = if (isOn) payloadOn else payloadOff
        )
    }
}
