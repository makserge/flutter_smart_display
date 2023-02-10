package com.smsoft.smartdisplay.ui.screen.sensorssettings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.utils.getParamFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SensorsSettingsViewModel @Inject constructor(
    val dataStore: DataStore<Preferences>
) : ViewModel() {

    val host = getParamFlow(
        dataStore = dataStore,
        defaultValue = ""
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.MQTT_BROKER_HOST.key)] ?: "" }

    val port = getParamFlow(
        dataStore = dataStore,
        defaultValue = MQTT_DEFAULT_PORT
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.MQTT_BROKER_PORT.key)] ?: MQTT_DEFAULT_PORT }

    val userName = getParamFlow(
        dataStore = dataStore,
        defaultValue = ""
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.MQTT_BROKER_USERNAME.key)] ?: "" }

    val password = getParamFlow(
        dataStore = dataStore,
        defaultValue = ""
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.MQTT_BROKER_PASSWORD.key)] ?: "" }

}
