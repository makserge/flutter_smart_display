package com.smsoft.smartdisplay.ui.composable.settings

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScope
import com.jamal.composeprefs.ui.prefs.EditTextPref
import com.jamal.composeprefs.ui.prefs.SliderPref
import com.jamal.composeprefs.ui.prefs.SwitchPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
fun alarmSettings(
    modifier: Modifier.Companion,
    scope: PrefsScope,
    lightSensorThreshold: String
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.alarm)
        )
    }) {
        prefsItem {
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.ALARM_LIGHT_ENABLED.key,
                title = stringResource(PreferenceKey.ALARM_LIGHT_ENABLED.title),
                defaultChecked = ALARM_LIGHT_ENABLED_DEFAULT
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.ALARM_LIGHT_SENSOR_THRESHOLD.key,
                title = stringResource(PreferenceKey.ALARM_LIGHT_SENSOR_THRESHOLD.title),
                summary = lightSensorThreshold,
                defaultValue = ALARM_LIGHT_SENSOR_THRESHOLD_DEFAULT
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.ALARM_TIMEOUT.key,
                title = stringResource(PreferenceKey.ALARM_TIMEOUT.title),
                valueRange = 1F..5F,
                defaultValue = ALARM_TIMEOUT_DEFAULT
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.ALARM_SOUND_VOLUME.key,
                title = stringResource(PreferenceKey.ALARM_SOUND_VOLUME.title),
                valueRange = 0.1F..1F,
                defaultValue = ALARM_SOUND_VOLUME_DEFAULT
            )
        }
    }
}

const val ALARM_LIGHT_ENABLED_DEFAULT = false
const val ALARM_LIGHT_SENSOR_THRESHOLD_DEFAULT = "1000"
const val ALARM_TIMEOUT_DEFAULT = 1F
const val ALARM_SOUND_VOLUME_DEFAULT = 0.2F