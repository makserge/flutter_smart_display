package com.smsoft.smartdisplay.ui.screen.sensorssettings

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
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScreen
import com.jamal.composeprefs.ui.prefs.EditTextPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey

const val MQTT_DEFAULT_PORT = "1883"

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SensorsSettingsScreen(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background),
    viewModel: SensorsSettingsViewModel = hiltViewModel()
) {
    val dataStore = viewModel.dataStore
    val host by viewModel.host.collectAsStateWithLifecycle(
        initialValue = ""
    )
    val port by viewModel.port.collectAsStateWithLifecycle(
        initialValue = MQTT_DEFAULT_PORT
    )
    val userName by viewModel.userName.collectAsStateWithLifecycle(
        initialValue = ""
    )
    val password by viewModel.password.collectAsStateWithLifecycle(
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
            host = host.toString(),
            port = port.toString(),
            userName = userName.toString(),
            password = password.toString()
        )
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun Prefs(
    modifier: Modifier,
    dataStore: DataStore<Preferences>,
    host: String,
    port: String,
    userName: String,
    password: String
) {
    PrefsScreen(
        modifier = Modifier,
        dataStore = dataStore
    ) {
        prefsGroup({
            GroupHeader(
                title = stringResource(R.string.mqtt_broker)
            )
        }) {
            prefsItem {
                EditTextPref(
                    modifier = modifier,
                    key = PreferenceKey.MQTT_BROKER_HOST.key,
                    title = stringResource(PreferenceKey.MQTT_BROKER_HOST.title),
                    summary = host
                )
                EditTextPref(
                    modifier = modifier,
                    key = PreferenceKey.MQTT_BROKER_PORT.key,
                    title = stringResource(PreferenceKey.MQTT_BROKER_PORT.title),
                    defaultValue = MQTT_DEFAULT_PORT,
                    summary = port
                )
                EditTextPref(
                    modifier = modifier,
                    key = PreferenceKey.MQTT_BROKER_USERNAME.key,
                    title = stringResource(PreferenceKey.MQTT_BROKER_USERNAME.title),
                    summary = userName
                )
                EditTextPref(
                    modifier = modifier,
                    key = PreferenceKey.MQTT_BROKER_PASSWORD.key,
                    title = stringResource(PreferenceKey.MQTT_BROKER_PASSWORD.title),
                    summary = password
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
