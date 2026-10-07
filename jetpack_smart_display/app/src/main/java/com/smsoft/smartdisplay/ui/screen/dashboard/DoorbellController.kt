package com.smsoft.smartdisplay.ui.screen.dashboard.controller

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.screen.dashboard.mqtt.DashboardMqttMessage
import com.smsoft.smartdisplay.ui.screen.settings.DOORBELL_ALARM_DEFAULT_TOPIC
import com.smsoft.smartdisplay.utils.observe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DoorbellController(
    private val dataStore: DataStore<Preferences>,
    private val scope: CoroutineScope,
) {
    private val doorBellAlarmStateInt = MutableStateFlow(false)
    val doorBellAlarmState = doorBellAlarmStateInt.asStateFlow()

    private var topic = DOORBELL_ALARM_DEFAULT_TOPIC

    /** Follows the topic setting: a changed topic used to apply only after a restart. */
    fun start() {
        dataStore.observe(
            scope = scope,
            read = { it[stringPreferencesKey(PreferenceKey.DOORBELL_ALARM_TOPIC.key)]?.trim() ?: DOORBELL_ALARM_DEFAULT_TOPIC }
        ) {
            topic = it
        }
    }

    fun onMqttMessage(message: DashboardMqttMessage) {
        // A retained doorbell message is replayed on every (re)subscribe; it is not a new ring.
        if ((message.topic == topic) && !message.isRetained) {
            doorBellAlarmStateInt.value = true
        }
    }

    fun reset() {
        doorBellAlarmStateInt.value = false
    }
}
