package com.smsoft.smartdisplay.ui.screen.weathersettings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.utils.getParamFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WeatherSettingsViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    class PreferencesRepository @Inject constructor(
        val dataStore: DataStore<Preferences>
    )

    val dataStore = preferencesRepository.dataStore

    val cityLat = getParamFlow(
        dataStore = dataStore,
        defaultValue = ""
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.WEATHER_CITY_LAT.key)] ?: "" }

    val cityLon = getParamFlow(
        dataStore = dataStore,
        defaultValue = ""
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.WEATHER_CITY_LON.key)] ?: "" }

}
