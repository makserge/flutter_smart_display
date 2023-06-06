package com.smsoft.smartdisplay.ui.composable.clock.digitalclock2

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScope
import com.jamal.composeprefs.ui.prefs.SliderPref
import com.jamal.composeprefs.ui.prefs.SwitchPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey

fun digitalClockPrefs2(
    modifier: Modifier,
    scope: PrefsScope
) {
    drawDigits(
        modifier = modifier,
        scope = scope
    )
    drawDecoration(
        modifier = modifier,
        scope = scope
    )
}

@OptIn(ExperimentalMaterialApi::class)
fun drawDigits(
    modifier: Modifier,
    scope: PrefsScope
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.digits)
        )
    }) {
        prefsItem {
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.FONT_SIZE_DC2.key,
                title = stringResource(PreferenceKey.FONT_SIZE_DC2.title),
                defaultValue = DEFAULT_FONT_SIZE_DC2,
                valueRange = 0.5F..1F,
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
fun drawDecoration(
    modifier: Modifier,
    scope: PrefsScope
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.decoration)
        )
    }) {
        prefsItem {
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.SHOW_SECONDS_DC2.key,
                title = stringResource(PreferenceKey.SHOW_SECONDS_DC2.title),
                defaultChecked = DEFAULT_SHOW_SECONDS_DC2
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.SHADOW_RADIUS_DC2.key,
                title = stringResource(PreferenceKey.SHADOW_RADIUS_DC2.title),
                defaultValue = DEFAULT_SHADOW_RADIUS_DC2,
                valueRange = 0F..14F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.ANIMATION_DURATION_DC2.key,
                title = stringResource(PreferenceKey.ANIMATION_DURATION_DC2.title),
                defaultValue = DEFAULT_ANIMATION_DURATION_DC2,
                valueRange = 100F..1000F,
            )
        }
    }
}