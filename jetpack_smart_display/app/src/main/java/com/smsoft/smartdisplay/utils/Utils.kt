package com.smsoft.smartdisplay.utils

import android.content.Context
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.RadioType
import com.smsoft.smartdisplay.ui.screen.settings.MPD_SERVER_DEFAULT_HOST
import com.smsoft.smartdisplay.ui.screen.settings.MPD_SERVER_DEFAULT_PORT
import com.smsoft.smartdisplay.utils.mpd.data.MPDCredentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

@Composable
fun getStateFromFlow(
    flow: Flow<*>,
    defaultValue: Any?
): Any? {
    return flow.collectAsStateWithLifecycle(initialValue = defaultValue).value
}

fun getParamFlow(
    dataStore: DataStore<Preferences>,
    defaultValue: Any?,
    getter: (preferences: Preferences) -> Any?
): Flow<*> {
    return dataStore.data.map {
        getter(it) ?: defaultValue
    }
}

fun getColor(value: androidx.compose.ui.graphics.Color) = Color.parseColor("#${Integer.toHexString(value.toArgb())}")

fun getIcon(
    context: Context,
    item: String
): Int {
    val res = if (item.isEmpty()) {
        "empty"
    } else {
        item
    }
    return context.resources.getIdentifier("outline_" + res + "_24", "drawable", context.packageName)
}

fun getRadioType(dataStore: DataStore<Preferences>): RadioType {
    var radioType = RadioType.getDefault()
    runBlocking {
        val data = dataStore.data.first()
        data[stringPreferencesKey(PreferenceKey.RADIO_TYPE.key)]?.let {
            radioType = RadioType.getById(it)
        }
    }
    return radioType
}

fun getRadioPreset(dataStore: DataStore<Preferences>): Int {
    var preset = 0
    runBlocking {
        val data = dataStore.data.first()
        data[intPreferencesKey(PreferenceKey.RADIO_PRESET.key)]?.let {
            preset = it
        }
    }
    return preset
}

fun getRadioSettings(dataStore: DataStore<Preferences>): MPDCredentials {
    var host = MPD_SERVER_DEFAULT_HOST
    var port = MPD_SERVER_DEFAULT_PORT
    var password = ""
    runBlocking {
        val data = dataStore.data.first()
        data[stringPreferencesKey(PreferenceKey.MPD_SERVER_HOST.key)]?.let {
            host = it
        }
        data[stringPreferencesKey(PreferenceKey.MPD_SERVER_PORT.key)]?.let {
            port = it
        }
        data[stringPreferencesKey(PreferenceKey.MPD_SERVER_PASSWORD.key)]?.let {
            password = it
        }
    }
    return MPDCredentials(
        host = host,
        port = port.toInt(),
        password = password
    )
}