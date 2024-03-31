package com.smsoft.smartdisplay.ui.composable.settings

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScope
import com.jamal.composeprefs.ui.prefs.EditTextPref
import com.jamal.composeprefs.ui.prefs.SwitchPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
fun lightSensorSettings(
    modifier: Modifier.Companion,
    scope: PrefsScope,
    topic: String,
    interval: String
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.light_sensor)
        )
    }) {
        prefsItem {
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.LIGHT_SENSOR_ENABLED.key,
                title = stringResource(PreferenceKey.LIGHT_SENSOR_ENABLED.title)
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.LIGHT_SENSOR_TOPIC.key,
                title = stringResource(PreferenceKey.LIGHT_SENSOR_TOPIC.title),
                summary = topic,
                defaultValue = LIGHT_SENSOR_TOPIC_DEFAULT
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.LIGHT_SENSOR_INTERVAL.key,
                title = stringResource(PreferenceKey.LIGHT_SENSOR_INTERVAL.title),
                summary = interval,
                defaultValue = LIGHT_SENSOR_INTERVAL_DEFAULT
            )
        }
    }
}

const val LIGHT_SENSOR_ENABLED_DEFAULT = false
const val LIGHT_SENSOR_TOPIC_DEFAULT = "light"
const val LIGHT_SENSOR_INTERVAL_DEFAULT = "5"