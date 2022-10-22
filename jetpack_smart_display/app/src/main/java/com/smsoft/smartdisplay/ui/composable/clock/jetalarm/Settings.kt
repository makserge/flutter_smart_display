package com.smsoft.smartdisplay.ui.composable.clock.jetalarm

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScope
import com.jamal.composeprefs.ui.prefs.SliderPref
import com.jamal.composeprefs.ui.prefs.SwitchPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey

@OptIn(ExperimentalMaterialApi::class)
fun drawAnalogJetAlarmPrefs(
    modifier: Modifier,
    scope: PrefsScope
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.border)
        )
    }) {
        prefsItem {
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.BORDER_RADIUS_JA.key,
                title = stringResource(PreferenceKey.BORDER_RADIUS_JA.title),
                defaultValue = DEFAULT_BORDER_RADIUS_JA,
                valueRange = 0.5F..1F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.BORDER_THICKNESS_JA.key,
                title = stringResource(PreferenceKey.BORDER_THICKNESS_JA.title),
                defaultValue = DEFAULT_BORDER_THICKNESS_JA,
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
                key = PreferenceKey.HAND_LENGTH_SECONDS_JA.key,
                title = stringResource(PreferenceKey.HAND_LENGTH_SECONDS_JA.title),
                defaultValue = DEFAULT_HAND_LEN_SECONDS_JA,
                valueRange = 0.3F..1F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_LENGTH_MINUTES_JA.key,
                title = stringResource(PreferenceKey.HAND_LENGTH_MINUTES_JA.title),
                defaultValue = DEFAULT_HAND_LEN_MINUTES_JA,
                valueRange = 0.3F..1F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_LENGTH_HOURS_JA.key,
                title = stringResource(PreferenceKey.HAND_LENGTH_HOURS_JA.title),
                defaultValue = DEFAULT_HAND_LEN_HOURS_JA,
                valueRange = 0.2F..0.8F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_WIDTH_SECONDS_JA.key,
                title = stringResource(PreferenceKey.HAND_WIDTH_SECONDS_JA.title),
                defaultValue = DEFAULT_HAND_WIDTH_SECONDS_JA,
                valueRange = 4F..10F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_WIDTH_MINUTES_JA.key,
                title = stringResource(PreferenceKey.HAND_WIDTH_MINUTES_JA.title),
                defaultValue = DEFAULT_HAND_WIDTH_MINUTES_JA,
                valueRange = 5F..10F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_WIDTH_HOURS_JA.key,
                title = stringResource(PreferenceKey.HAND_WIDTH_HOURS_JA.title),
                defaultValue = DEFAULT_HAND_WIDTH_HOURS_JA,
                valueRange = 5F..10F,
            )
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.HAND_SHOW_SECOND_HAND_JA.key,
                title = stringResource(PreferenceKey.HAND_SHOW_SECOND_HAND_JA.title),
                defaultChecked = DEFAULT_SHOW_SECOND_HAND_JA
            )
        }
    }
}