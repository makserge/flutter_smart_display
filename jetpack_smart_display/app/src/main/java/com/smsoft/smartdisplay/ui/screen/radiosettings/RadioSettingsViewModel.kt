package com.smsoft.smartdisplay.ui.screen.radiosettings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.utils.getParamFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RadioSettingsViewModel @Inject constructor(
    val dataStore: DataStore<Preferences>
) : ViewModel() {

    val host = getParamFlow(
        dataStore = dataStore,
        defaultValue = MPD_SERVER_DEFAULT_HOST
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.MPD_SERVER_HOST.key)] ?: "" }

    val port = getParamFlow(
        dataStore = dataStore,
        defaultValue = MPD_SERVER_DEFAULT_PORT
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.MPD_SERVER_PORT.key)] ?: MPD_SERVER_DEFAULT_PORT }

    val password = getParamFlow(
        dataStore = dataStore,
        defaultValue = ""
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.MPD_SERVER_PASSWORD.key)] ?: "" }

}
