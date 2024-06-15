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
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_COMMAND_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_DEFAULT_PAYLOAD_OFF
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_DEFAULT_PAYLOAD_ON
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_STATUS_DEFAULT_TOPIC

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
fun pushButtonSettings(
    modifier: Modifier.Companion,
    scope: PrefsScope,
    statusTopic: String,
    commandTopic: String,
    payloadOn: String,
    payloadOff: String,
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.push_button)
        )
    }) {
        prefsItem {
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.PUSH_BUTTON_STATUS_TOPIC.key,
                title = stringResource(PreferenceKey.PUSH_BUTTON_STATUS_TOPIC.title),
                summary = statusTopic,
                defaultValue = PUSH_BUTTON_STATUS_DEFAULT_TOPIC
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.PUSH_BUTTON_COMMAND_TOPIC.key,
                title = stringResource(PreferenceKey.PUSH_BUTTON_COMMAND_TOPIC.title),
                summary = commandTopic,
                defaultValue = PUSH_BUTTON_COMMAND_DEFAULT_TOPIC
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.PUSH_BUTTON_PAYLOAD_ON.key,
                title = stringResource(PreferenceKey.PUSH_BUTTON_PAYLOAD_ON.title),
                summary = payloadOn,
                defaultValue = PUSH_BUTTON_DEFAULT_PAYLOAD_ON
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.PUSH_BUTTON_PAYLOAD_OFF.key,
                title = stringResource(PreferenceKey.PUSH_BUTTON_PAYLOAD_OFF.title),
                summary = payloadOff,
                defaultValue = PUSH_BUTTON_DEFAULT_PAYLOAD_OFF
            )
        }
    }
}