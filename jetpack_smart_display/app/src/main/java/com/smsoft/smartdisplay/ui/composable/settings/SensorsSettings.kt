package com.smsoft.smartdisplay.ui.composable.settings

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScope
import com.jamal.composeprefs.ui.prefs.EditTextPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.screen.settings.MQTT_SERVER_DEFAULT_HOST
import com.smsoft.smartdisplay.ui.screen.settings.MQTT_SERVER_DEFAULT_PORT

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
fun sensorsSettings (
    modifier: Modifier,
    scope: PrefsScope,
    host: String,
    port: String,
    userName: String,
    password: String
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.mqtt_broker)
        )
    }) {
        prefsItem {
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.MQTT_BROKER_HOST.key,
                title = stringResource(PreferenceKey.MQTT_BROKER_HOST.title),
                defaultValue = MQTT_SERVER_DEFAULT_HOST,
                summary = host
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.MQTT_BROKER_PORT.key,
                title = stringResource(PreferenceKey.MQTT_BROKER_PORT.title),
                defaultValue = MQTT_SERVER_DEFAULT_PORT,
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
