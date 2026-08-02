package com.smsoft.smartdisplay.ui.screen.dashboard.controller

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.screen.dashboard.mqtt.DashboardMqttMessage
import com.smsoft.smartdisplay.ui.screen.settings.DOORBELL_ALARM_DEFAULT_TOPIC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DoorbellController(
    private val dataStore: DataStore<Preferences>,
    private val scope: CoroutineScope,
) {
    private val doorBellAlarmStateInt = MutableStateFlow(false)
    val doorBellAlarmState = doorBellAlarmStateInt.asStateFlow()

    private var topic = DOORBELL_ALARM_DEFAULT_TOPIC

    fun start() {
        scope.launch(Dispatchers.IO) {
            val data = dataStore.data.first()
            data[stringPreferencesKey(PreferenceKey.DOORBELL_ALARM_TOPIC.key)]?.let {
                topic = it.trim()
            }
        }
    }

    fun onMqttMessage(message: DashboardMqttMessage) {
        if (message.topic == topic) {
            doorBellAlarmStateInt.value = true
        }
    }

    fun reset() {
        doorBellAlarmStateInt.value = false
    }
}
