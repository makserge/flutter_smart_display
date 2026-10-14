package com.smsoft.smartdisplay.ui.composable.settings

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.common.prefs.EditTextPref
import com.smsoft.smartdisplay.ui.common.prefs.GroupHeader
import com.smsoft.smartdisplay.ui.common.prefs.PrefsScope
import com.smsoft.smartdisplay.ui.screen.dashboard.CINEMA_MODE_COMMAND_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.screen.dashboard.CINEMA_MODE_DEFAULT_PAYLOAD_OFF
import com.smsoft.smartdisplay.ui.screen.dashboard.CINEMA_MODE_DEFAULT_PAYLOAD_ON

/** The MQTT topic and payloads of the cinema mode, switched by voice (CinemaModeController). */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
fun cinemaModeSettings(
    modifier: Modifier.Companion,
    scope: PrefsScope,
    commandTopic: String,
    payloadOn: String,
    payloadOff: String,
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.cinema_mode)
        )
    }) {
        prefsItem {
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.CINEMA_MODE_COMMAND_TOPIC.key,
                title = stringResource(PreferenceKey.CINEMA_MODE_COMMAND_TOPIC.title),
                summary = commandTopic,
                defaultValue = CINEMA_MODE_COMMAND_DEFAULT_TOPIC
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.CINEMA_MODE_PAYLOAD_ON.key,
                title = stringResource(PreferenceKey.CINEMA_MODE_PAYLOAD_ON.title),
                summary = payloadOn,
                defaultValue = CINEMA_MODE_DEFAULT_PAYLOAD_ON
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.CINEMA_MODE_PAYLOAD_OFF.key,
                title = stringResource(PreferenceKey.CINEMA_MODE_PAYLOAD_OFF.title),
                summary = payloadOff,
                defaultValue = CINEMA_MODE_DEFAULT_PAYLOAD_OFF
            )
        }
    }
}
