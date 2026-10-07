package com.smsoft.smartdisplay.ui.composable.clock.digitalmatrixclock

import android.content.Context
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.smsoft.smartdisplay.ui.common.prefs.GroupHeader
import com.smsoft.smartdisplay.ui.common.prefs.PrefsScope
import com.smsoft.smartdisplay.ui.common.prefs.ListPref
import com.smsoft.smartdisplay.ui.common.prefs.SliderPref
import com.smsoft.smartdisplay.ui.common.prefs.SwitchPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.composable.settings.ColorPickerPref

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
fun digitalMatrixClockPrefs(
    modifier: Modifier,
    scope: PrefsScope,
    context: Context
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.dots)
        )
    }) {
        prefsItem {
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DOT_RADIUS_ROUND_MC.key,
                title = stringResource(PreferenceKey.DOT_RADIUS_ROUND_MC.title),
                defaultValue = DEFAULT_DOT_RADIUS_ROUND_MC,
                valueRange = 2F..14F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DOT_SPACING_ROUND_MC.key,
                title = stringResource(PreferenceKey.DOT_SPACING_ROUND_MC.title),
                defaultValue = DEFAULT_DOT_SPACING_ROUND_MC,
                valueRange = 2F..7F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DOT_RADIUS_ROUND_SEC_MC.key,
                title = stringResource(PreferenceKey.DOT_RADIUS_ROUND_SEC_MC.title),
                defaultValue = DEFAULT_DOT_RADIUS_ROUND_SECONDS_MC,
                valueRange = 2F..10F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DOT_SPACING_ROUND_SEC_MC.key,
                title = stringResource(PreferenceKey.DOT_SPACING_ROUND_SEC_MC.title),
                defaultValue = DEFAULT_DOT_SPACING_ROUND_SECONDS_MC,
                valueRange = 2F..5F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DOT_RADIUS_SQUARE_MC.key,
                title = stringResource(PreferenceKey.DOT_RADIUS_SQUARE_MC.title),
                defaultValue = DEFAULT_DOT_RADIUS_SQUARE_MC,
                valueRange = 8F..24F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DOT_SPACING_SQUARE_MC.key,
                title = stringResource(PreferenceKey.DOT_SPACING_SQUARE_MC.title),
                defaultValue = DEFAULT_DOT_SPACING_SQUARE_MC,
                valueRange = 4F..12F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DOT_RADIUS_SQUARE_SEC_MC.key,
                title = stringResource(PreferenceKey.DOT_RADIUS_SQUARE_SEC_MC.title),
                defaultValue = DEFAULT_DOT_RADIUS_SQUARE_SECONDS_MC,
                valueRange = 8F..16F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DOT_SPACING_SQUARE_SEC_MC.key,
                title = stringResource(PreferenceKey.DOT_SPACING_SQUARE_SEC_MC.title),
                defaultValue = DEFAULT_DOT_SPACING_SQUARE_SECONDS_MC,
                valueRange = 2F..8F,
            )
        }
    }
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.decoration)
        )
    }) {
        prefsItem {
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.SHOW_SECONDS_MC.key,
                title = stringResource(PreferenceKey.SHOW_SECONDS_MC.title),
                defaultChecked = DEFAULT_SHOW_SECONDS_MC
            )
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.BLINK_SEPARATOR_MC.key,
                title = stringResource(PreferenceKey.BLINK_SEPARATOR_MC.title),
                defaultChecked = DEFAULT_BLINK_SEPARATOR_MC
            )
            ColorPickerPref(
                modifier = modifier,
                key = PreferenceKey.DOT_COLOR_MC.key,
                title = stringResource(PreferenceKey.DOT_COLOR_MC.title),
                defaultValue = DEFAULT_DOT_COLOR_MC
            )
            ListPref(
                modifier = modifier,
                key = PreferenceKey.DOT_STYLE_MC.key,
                title = stringResource(PreferenceKey.DOT_STYLE_MC.title),
                defaultValue = DotStyle.getDefaultId(),
                useSelectedAsSummary = true,
                entries = DotStyle.toMap(context),
            )
        }
    }
}