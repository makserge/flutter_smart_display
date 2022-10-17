package com.smsoft.smartdisplay.ui.composable.clock.digitalmatrixclock

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

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
fun drawDigitalMatrixClockPrefs(
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
                key = PreferenceKey.DOT_RADIUS_ROUND_MATRIX_CLOCK.key,
                title = stringResource(PreferenceKey.DOT_RADIUS_ROUND_MATRIX_CLOCK.title),
                defaultValue = DEFAULT_DOT_RADIUS_ROUND,
                valueRange = 2F..14F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DOT_SPACING_ROUND_MATRIX_CLOCK.key,
                title = stringResource(PreferenceKey.DOT_SPACING_ROUND_MATRIX_CLOCK.title),
                defaultValue = DEFAULT_DOT_SPACING_ROUND,
                valueRange = 2F..7F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DOT_RADIUS_ROUND_SEC_MATRIX_CLOCK.key,
                title = stringResource(PreferenceKey.DOT_RADIUS_ROUND_SEC_MATRIX_CLOCK.title),
                defaultValue = DEFAULT_DOT_RADIUS_ROUND_SECONDS,
                valueRange = 2F..10F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DOT_SPACING_ROUND_SEC_MATRIX_CLOCK.key,
                title = stringResource(PreferenceKey.DOT_SPACING_ROUND_SEC_MATRIX_CLOCK.title),
                defaultValue = DEFAULT_DOT_SPACING_ROUND_SECONDS,
                valueRange = 2F..5F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DOT_RADIUS_SQUARE_MATRIX_CLOCK.key,
                title = stringResource(PreferenceKey.DOT_RADIUS_SQUARE_MATRIX_CLOCK.title),
                defaultValue = DEFAULT_DOT_RADIUS_SQUARE,
                valueRange = 8F..24F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DOT_SPACING_SQUARE_MATRIX_CLOCK.key,
                title = stringResource(PreferenceKey.DOT_SPACING_SQUARE_MATRIX_CLOCK.title),
                defaultValue = DEFAULT_DOT_SPACING_SQUARE,
                valueRange = 4F..12F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DOT_RADIUS_SQUARE_SEC_MATRIX_CLOCK.key,
                title = stringResource(PreferenceKey.DOT_RADIUS_SQUARE_SEC_MATRIX_CLOCK.title),
                defaultValue = DEFAULT_DOT_RADIUS_SQUARE_SECONDS,
                valueRange = 8F..16F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DOT_SPACING_SQUARE_SEC_MATRIX_CLOCK.key,
                title = stringResource(PreferenceKey.DOT_SPACING_SQUARE_SEC_MATRIX_CLOCK.title),
                defaultValue = DEFAULT_DOT_SPACING_SQUARE_SECONDS,
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
                key = PreferenceKey.SHOW_SECONDS_MATRIX_CLOCK.key,
                title = stringResource(PreferenceKey.SHOW_SECONDS_MATRIX_CLOCK.title),
                defaultChecked = DEFAULT_SHOW_SECONDS
            )
            ListPref(
                modifier = modifier,
                key = PreferenceKey.DOT_STYLE_MATRIX_CLOCK.key,
                title = stringResource(PreferenceKey.DOT_STYLE_MATRIX_CLOCK.title),
                defaultValue = DotStyle.getDefaultId(),
                useSelectedAsSummary = true,
                entries = DotStyle.toMap(context),
            )
        }
    }
}