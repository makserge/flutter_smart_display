package com.smsoft.smartdisplay.ui.composable.clock.clockview

import android.content.Context
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScope
import com.jamal.composeprefs.ui.prefs.ListPref
import com.jamal.composeprefs.ui.prefs.SwitchPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey

fun drawAnalogClockViewPrefs(
    modifier: Modifier,
    scope: PrefsScope,
    context: Context
) {
    drawDigits(
        modifier = modifier,
        scope = scope,
        context = context
    )
    drawTicks(
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
                key = PreferenceKey.DIGIT_FONT_CLOCKVIEW.key,
                title = stringResource(PreferenceKey.DIGIT_FONT_CLOCKVIEW.title),
                defaultValue = Font.getDefaultId(),
                useSelectedAsSummary = true,
                entries = Font.toMap(context)
            )
            ListPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_STYLE_CLOCKVIEW.key,
                title = stringResource(PreferenceKey.DIGIT_STYLE_CLOCKVIEW.title),
                defaultValue = DigitStyle.getDefaultId(),
                useSelectedAsSummary = true,
                entries = DigitStyle.toMap(context)
            )
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_SHOW_HOURS_CLOCKVIEW.key,
                title = stringResource(PreferenceKey.DIGIT_SHOW_HOURS_CLOCKVIEW.title),
                defaultChecked = DEFAULT_SHOW_HOURS
            )
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_SHOW_MINUTES_CLOCKVIEW.key,
                title = stringResource(PreferenceKey.DIGIT_SHOW_MINUTES_CLOCKVIEW.title),
                defaultChecked = DEFAULT_SHOW_MINUTES
            )
            ListPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_STEP_CLOCKVIEW.key,
                title = stringResource(PreferenceKey.DIGIT_STEP_CLOCKVIEW.title),
                defaultValue = DigitStep.getDefaultId(),
                useSelectedAsSummary = true,
                entries = DigitStep.toMap(context)
            )
            ListPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_DISPOSITION_CLOCKVIEW.key,
                title = stringResource(PreferenceKey.DIGIT_DISPOSITION_CLOCKVIEW.title),
                defaultValue = DigitDisposition.getDefaultId(),
                useSelectedAsSummary = true,
                entries = DigitDisposition.toMap(context)
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
fun drawTicks(
    modifier: Modifier,
    scope: PrefsScope,
    context: Context
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.ticks)
        )
    }) {
        prefsItem {
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_SHOW_DEGREES_CLOCKVIEW.key,
                title = stringResource(PreferenceKey.DIGIT_SHOW_DEGREES_CLOCKVIEW.title),
                defaultChecked = DEFAULT_SHOW_DEGREES
            )
            ListPref(
                modifier = modifier,
                key = PreferenceKey.DEGREE_TYPE_CLOCKVIEW.key,
                title = stringResource(PreferenceKey.DEGREE_TYPE_CLOCKVIEW.title),
                defaultValue = DegreeType.getDefaultId(),
                useSelectedAsSummary = true,
                entries = DegreeType.toMap(context)
            )
            ListPref(
                modifier = modifier,
                key = PreferenceKey.DEGREE_STEP_CLOCKVIEW.key,
                title = stringResource(PreferenceKey.DEGREE_STEP_CLOCKVIEW.title),
                defaultValue = DegreesStep.getDefaultId(),
                useSelectedAsSummary = true,
                entries = DegreesStep.toMap(context)
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
                key = PreferenceKey.SHOW_CENTER_CLOCKVIEW.key,
                title = stringResource(PreferenceKey.SHOW_CENTER_CLOCKVIEW.title),
                defaultChecked = DEFAULT_SHOW_CENTER
            )
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.SHOW_SECOND_HAND_CLOCKVIEW.key,
                title = stringResource(PreferenceKey.SHOW_SECOND_HAND_CLOCKVIEW.title),
                defaultChecked = DEFAULT_SHOW_SECOND_HAND
            )
        }
    }
}