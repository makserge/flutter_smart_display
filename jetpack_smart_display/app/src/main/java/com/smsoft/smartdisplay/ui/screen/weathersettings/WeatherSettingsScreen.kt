package com.smsoft.smartdisplay.ui.screen.weathersettings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScreen
import com.jamal.composeprefs.ui.prefs.EditTextPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey

@Composable
fun WeatherSettingsScreen(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background),
    viewModel: WeatherSettingsViewModel = hiltViewModel()
) {
    val dataStore = viewModel.dataStore
    val cityLat by viewModel.cityLat.collectAsStateWithLifecycle(
        initialValue = ""
    )
    val cityLon by viewModel.cityLon.collectAsStateWithLifecycle(
        initialValue = ""
    )

    Scaffold(
        modifier = Modifier,
        topBar = {
            SettingsTopBar(
                modifier = Modifier,
            )
        }
    ) {
        Prefs(
            modifier = Modifier,
            dataStore = dataStore,
            cityLat = cityLat as String,
            cityLon = cityLon as String
        )
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun Prefs(
    modifier: Modifier,
    dataStore: DataStore<Preferences>,
    cityLat: String,
    cityLon: String
) {
    PrefsScreen(
        modifier = Modifier,
        dataStore = dataStore
    ) {
        prefsGroup({
            GroupHeader(
                title = stringResource(R.string.weather_city)
            )
        }) {
            prefsItem {
                EditTextPref(
                    modifier = modifier,
                    key = PreferenceKey.WEATHER_CITY_LAT.key,
                    title = stringResource(PreferenceKey.WEATHER_CITY_LAT.title),
                    summary = cityLat
                )
                EditTextPref(
                    modifier = modifier,
                    key = PreferenceKey.WEATHER_CITY_LON.key,
                    title = stringResource(PreferenceKey.WEATHER_CITY_LON.title),
                    summary = cityLon
                )
            }
        }
    }
}
@Composable
fun SettingsTopBar(
    modifier: Modifier,
) {
    TopAppBar(
        modifier = Modifier,
        title = {
            Text(
                modifier = Modifier,
                text = stringResource(R.string.settings)
            )
        }
    )
}
//lat: 48.137428
//lon: 11.57549