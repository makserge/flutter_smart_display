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
import com.smsoft.smartdisplay.ui.screen.dashboard.PROXIMITY_SENSOR_DEFAULT_PAYLOAD_OFF
import com.smsoft.smartdisplay.ui.screen.dashboard.PROXIMITY_SENSOR_DEFAULT_PAYLOAD_ON
import com.smsoft.smartdisplay.ui.screen.dashboard.PROXIMITY_SENSOR_DEFAULT_TOPIC

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
fun proximitySensorSettings(
    modifier: Modifier.Companion,
    scope: PrefsScope,
    topic: String,
    payloadOn: String,
    payloadOff: String,
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.proximity_sensor)
        )
    }) {
        prefsItem {
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.PROXIMITY_SENSOR_TOPIC.key,
                title = stringResource(PreferenceKey.PROXIMITY_SENSOR_TOPIC.title),
                summary = topic,
                defaultValue = PROXIMITY_SENSOR_DEFAULT_TOPIC
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.PROXIMITY_SENSOR_PAYLOAD_ON.key,
                title = stringResource(PreferenceKey.PROXIMITY_SENSOR_PAYLOAD_ON.title),
                summary = payloadOn,
                defaultValue = PROXIMITY_SENSOR_DEFAULT_PAYLOAD_ON
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.PROXIMITY_SENSOR_PAYLOAD_OFF.key,
                title = stringResource(PreferenceKey.PROXIMITY_SENSOR_PAYLOAD_OFF.title),
                summary = payloadOff,
                defaultValue = PROXIMITY_SENSOR_DEFAULT_PAYLOAD_OFF
            )
        }
    }
}