package com.smsoft.smartdisplay.ui.composable.clock.digitalclock

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
import com.smsoft.smartdisplay.data.PreferenceKey

fun drawDigitalClockPrefs(
    modifier: Modifier,
    scope: PrefsScope,
    context: Context
) {
    drawDigits(
        modifier = modifier,
        scope = scope,
        context = context
    )
    drawDecoration(
        modifier = modifier,
        scope = scope
    )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
fun drawDigits(
    modifier: Modifier,
    scope: PrefsScope,
    context: Context
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.digits)
        )
    }) {
        prefsItem {
            ListPref(
                modifier = modifier,
                key = PreferenceKey.TIME_FONT_DIGITAL_CLOCK.key,
                title = stringResource(PreferenceKey.TIME_FONT_DIGITAL_CLOCK.title),
                defaultValue = DigitFont.getDefaultId(),
                useSelectedAsSummary = true,
                entries = DigitFont.toMap(context)
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.TIME_FONT_SIZE_DIGITAL_CLOCK.key,
                title = stringResource(PreferenceKey.TIME_FONT_SIZE_DIGITAL_CLOCK.title),
                defaultValue = DEFAULT_TIME_FONT_SIZE,
                valueRange = 100F..330F,
            )
            ListPref(
                modifier = modifier,
                key = PreferenceKey.DATE_FONT_DIGITAL_CLOCK.key,
                title = stringResource(PreferenceKey.DATE_FONT_DIGITAL_CLOCK.title),
                defaultValue = DigitFont.getDefaultId(),
                useSelectedAsSummary = true,
                entries = DigitFont.toMap(context)
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DATE_FONT_SIZE_DIGITAL_CLOCK.key,
                title = stringResource(PreferenceKey.DATE_FONT_SIZE_DIGITAL_CLOCK.title),
                defaultValue = DEFAULT_DATE_FONT_SIZE,
                valueRange = 20F..80F,
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
                key = PreferenceKey.SHOW_SECONDS_DIGITAL_CLOCK.key,
                title = stringResource(PreferenceKey.SHOW_SECONDS_DIGITAL_CLOCK.title),
                defaultChecked = DEFAULT_SHOW_SECONDS
            )
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.SHOW_DATE_DIGITAL_CLOCK.key,
                title = stringResource(PreferenceKey.SHOW_DATE_DIGITAL_CLOCK.title),
                defaultChecked = DEFAULT_SHOW_DATE
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.SPACE_HEIGHT_DIGITAL_CLOCK.key,
                title = stringResource(PreferenceKey.SPACE_HEIGHT_DIGITAL_CLOCK.title),
                defaultValue = DEFAULT_SPACE_HEIGHT,
                valueRange = 0F..100F,
            )
        }
    }
}