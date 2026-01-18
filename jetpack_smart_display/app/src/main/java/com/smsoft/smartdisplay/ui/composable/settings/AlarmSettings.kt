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
    lightSensorThreshold: String,
    dimmerCommandOnOffTopic: String,
    dimmerCommandOnPayload: String,
    dimmerCommandOffPayload: String,
    dimmerCommandTopic: String
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
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.ALARM_LIGHT_DIMMER_ENABLED.key,
                title = stringResource(PreferenceKey.ALARM_LIGHT_DIMMER_ENABLED.title),
                defaultChecked = ALARM_LIGHT_DIMMER_ENABLED_DEFAULT
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_ON_OFF_TOPIC.key,
                title = stringResource(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_ON_OFF_TOPIC.title),
                summary = dimmerCommandOnOffTopic,
                defaultValue = ALARM_LIGHT_DIMMER_COMMAND_ON_OFF_DEFAULT_TOPIC
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_ON_PAYLOAD.key,
                title = stringResource(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_ON_PAYLOAD.title),
                summary = dimmerCommandOnPayload,
                defaultValue = ALARM_LIGHT_DIMMER_COMMAND_ON_DEFAULT_PAYLOAD
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_OFF_PAYLOAD.key,
                title = stringResource(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_OFF_PAYLOAD.title),
                summary = dimmerCommandOffPayload,
                defaultValue = ALARM_LIGHT_DIMMER_COMMAND_OFF_DEFAULT_PAYLOAD
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_TOPIC.key,
                title = stringResource(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_TOPIC.title),
                summary = dimmerCommandTopic,
                defaultValue = ALARM_LIGHT_DIMMER_COMMAND_DEFAULT_TOPIC
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
const val ALARM_LIGHT_DIMMER_ENABLED_DEFAULT = false
const val ALARM_LIGHT_DIMMER_COMMAND_ON_OFF_DEFAULT_TOPIC = "cmnd/led_strip_hall_dimmer/led_enableAll"
const val ALARM_LIGHT_DIMMER_COMMAND_ON_DEFAULT_PAYLOAD = "1"
const val ALARM_LIGHT_DIMMER_COMMAND_OFF_DEFAULT_PAYLOAD = "0"
const val ALARM_LIGHT_DIMMER_COMMAND_DEFAULT_TOPIC = "cmnd/led_strip_hall_dimmer/led_dimmer"
const val ALARM_TIMEOUT_DEFAULT = 1F
const val ALARM_SOUND_VOLUME_DEFAULT = 0.2F