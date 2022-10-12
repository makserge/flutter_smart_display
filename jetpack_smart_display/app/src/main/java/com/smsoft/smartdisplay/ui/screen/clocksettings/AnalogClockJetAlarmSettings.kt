package com.smsoft.smartdisplay.ui.screen.clocksettings

import android.content.Context
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScope
import com.jamal.composeprefs.ui.prefs.ListPref
import com.jamal.composeprefs.ui.prefs.SliderPref
import com.jamal.composeprefs.ui.prefs.SwitchPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.Font
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.composable.analog.*
import io.ak1.jetalarm.*

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
fun drawAnalogJetAlarmPrefs(
    modifier: Modifier,
    scope: PrefsScope,
    context: Context
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.border)
        )
    }) {
        prefsItem {
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.BORDER_RADIUS_JET.key,
                title = stringResource(PreferenceKey.BORDER_RADIUS_JET.title),
                defaultValue = DEFAULT_BORDER_RADIUS,
                valueRange = 0.5F..1F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.BORDER_THICKNESS_JET.key,
                title = stringResource(PreferenceKey.BORDER_THICKNESS_JET.title),
                defaultValue = DEFAULT_BORDER_THICKNESS,
                valueRange = 5F..15F,
            )
        }
    }
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.hands)
        )
    }) {
        prefsItem {
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_LENGTH_SECONDS_JET.key,
                title = stringResource(PreferenceKey.HAND_LENGTH_SECONDS_JET.title),
                defaultValue = DEFAULT_HAND_LEN_SECONDS_JET,
                valueRange = 0.3F..1F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_LENGTH_MINUTES_JET.key,
                title = stringResource(PreferenceKey.HAND_LENGTH_MINUTES_JET.title),
                defaultValue = DEFAULT_HAND_LEN_MINUTES_JET,
                valueRange = 0.3F..1F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_LENGTH_HOURS_JET.key,
                title = stringResource(PreferenceKey.HAND_LENGTH_HOURS_JET.title),
                defaultValue = DEFAULT_HAND_LEN_HOURS_JET,
                valueRange = 0.2F..0.8F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_WIDTH_SECONDS_JET.key,
                title = stringResource(PreferenceKey.HAND_WIDTH_SECONDS_JET.title),
                defaultValue = DEFAULT_HAND_WIDTH_SECONDS_JET,
                valueRange = 4F..10F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_WIDTH_MINUTES_JET.key,
                title = stringResource(PreferenceKey.HAND_WIDTH_MINUTES_JET.title),
                defaultValue = DEFAULT_HAND_WIDTH_MINUTES_JET,
                valueRange = 5F..10F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_WIDTH_HOURS_JET.key,
                title = stringResource(PreferenceKey.HAND_WIDTH_HOURS_JET.title),
                defaultValue = DEFAULT_HAND_WIDTH_HOURS_JET,
                valueRange = 5F..10F,
            )
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.HAND_SHOW_SECOND_HAND_JET.key,
                title = stringResource(PreferenceKey.HAND_SHOW_SECOND_HAND_JET.title),
                defaultChecked = DEFAULT_SHOW_SECOND_HAND_JET
            )
        }
    }
}