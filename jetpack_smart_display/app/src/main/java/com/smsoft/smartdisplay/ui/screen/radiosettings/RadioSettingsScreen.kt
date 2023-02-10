package com.smsoft.smartdisplay.ui.screen.radiosettings

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScreen
import com.jamal.composeprefs.ui.prefs.EditTextPref
import com.jamal.composeprefs.ui.prefs.ListPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.RadioType

const val MPD_SERVER_DEFAULT_HOST = ""
const val MPD_SERVER_DEFAULT_PORT = "6600"

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RadioSettingsScreen(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background),
    viewModel: RadioSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val dataStore = viewModel.dataStore
    val host by viewModel.host.collectAsStateWithLifecycle(
        initialValue = MPD_SERVER_DEFAULT_HOST
    )
    val port by viewModel.port.collectAsStateWithLifecycle(
        initialValue = MPD_SERVER_DEFAULT_PORT
    )
    val password by viewModel.password.collectAsStateWithLifecycle(
        initialValue = ""
    )

    Scaffold(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount.y > 0) {
                        onBack()
                    }
                }
            },
        topBar = {
            SettingsTopBar(
                modifier = Modifier
            )
        }
    ) {
        Prefs(
            modifier = Modifier,
            context = context,
            dataStore = dataStore,
            host = host.toString(),
            port = port.toString(),
            password = password.toString()
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
fun Prefs(
    modifier: Modifier,
    context: Context,
    dataStore: DataStore<Preferences>,
    host: String,
    port: String,
    password: String
) {
    PrefsScreen(
        modifier = Modifier,
        dataStore = dataStore
    ) {
        prefsItem {
            ListPref(
                modifier = modifier,
                key = PreferenceKey.RADIO_TYPE.key,
                title = stringResource(PreferenceKey.RADIO_TYPE.title),
                defaultValue = RadioType.getDefaultId(),
                useSelectedAsSummary = true,
                entries = RadioType.toMap(context)
            )
        }
        prefsGroup({
            GroupHeader(
                title = stringResource(R.string.mpd_server)
            )
        }) {
            prefsItem {
                EditTextPref(
                    modifier = modifier,
                    key = PreferenceKey.MPD_SERVER_HOST.key,
                    title = stringResource(PreferenceKey.MPD_SERVER_HOST.title),
                    defaultValue = MPD_SERVER_DEFAULT_HOST,
                    summary = host
                )
                EditTextPref(
                    modifier = modifier,
                    key = PreferenceKey.MPD_SERVER_PORT.key,
                    title = stringResource(PreferenceKey.MPD_SERVER_PORT.title),
                    defaultValue = MPD_SERVER_DEFAULT_PORT,
                    summary = port
                )
                EditTextPref(
                    modifier = modifier,
                    key = PreferenceKey.MPD_SERVER_PASSWORD.key,
                    title = stringResource(PreferenceKey.MPD_SERVER_PASSWORD.title),
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
