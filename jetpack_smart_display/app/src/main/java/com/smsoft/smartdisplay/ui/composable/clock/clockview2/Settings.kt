package com.smsoft.smartdisplay.ui.composable.clock.clockview2

import android.content.Context
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScope
import com.jamal.composeprefs.ui.prefs.ListPref
import com.jamal.composeprefs.ui.prefs.SliderPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey

fun analogClockView2Prefs(
    modifier: Modifier,
    scope: PrefsScope,
    context: Context
) {
    drawDigits(
        modifier = modifier,
        scope = scope,
        context = context
    )
    drawHands(
        modifier = modifier,
        scope = scope
    )
    drawTicks(
        modifier = modifier,
        scope = scope
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
                key = PreferenceKey.DIGIT_FONT_CV2.key,
                title = stringResource(PreferenceKey.DIGIT_FONT_CV2.title),
                defaultValue = Font.getDefaultId(),
                useSelectedAsSummary = true,
                entries = Font.toMap(context)
            )
            ListPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_STYLE_CV2.key,
                title = stringResource(PreferenceKey.DIGIT_STYLE_CV2.title),
                defaultValue = DigitStyle.getDefaultId(),
                useSelectedAsSummary = true,
                entries = DigitStyle.toMap(context)
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_TEXT_SIZE_CV2.key,
                title = stringResource(PreferenceKey.DIGIT_TEXT_SIZE_CV2.title),
                defaultValue = DEFAULT_DIGIT_TEXT_SIZE_CV2,
                valueRange = 15F..35F,
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
fun drawHands(
    modifier: Modifier,
    scope: PrefsScope
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.hands)
        )
    }) {
        prefsItem {
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.MINUTE_HAND_WIDTH_CV2.key,
                title = stringResource(PreferenceKey.MINUTE_HAND_WIDTH_CV2.title),
                defaultValue = DEFAULT_MINUTE_HAND_WIDTH_CV2,
                valueRange = 1F..5F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HOUR_HAND_WIDTH_CV2.key,
                title = stringResource(PreferenceKey.HOUR_HAND_WIDTH_CV2.title),
                defaultValue = DEFAULT_HOUR_HAND_WIDTH_CV2,
                valueRange = 3F..5F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.SECOND_HAND_WIDTH_CV2.key,
                title = stringResource(PreferenceKey.SECOND_HAND_WIDTH_CV2.title),
                defaultValue = DEFAULT_SECOND_HAND_WIDTH_CV2,
                valueRange = 1F..3F,
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
fun drawTicks(
    modifier: Modifier,
    scope: PrefsScope
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.ticks)
        )
    }) {
        prefsItem {
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.OUTER_RIM_WIDTH_CV2.key,
                title = stringResource(PreferenceKey.OUTER_RIM_WIDTH_CV2.title),
                defaultValue = DEFAULT_OUTER_RIM_WIDTH_CV2,
                valueRange = 1F..5F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.INNER_RIM_WIDTH_CV2.key,
                title = stringResource(PreferenceKey.INNER_RIM_WIDTH_CV2.title),
                defaultValue = DEFAULT_INNER_RIM_WIDTH_CV2,
                valueRange = 1F..5F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.THICK_MARKER_WIDTH_CV2.key,
                title = stringResource(PreferenceKey.THICK_MARKER_WIDTH_CV2.title),
                defaultValue = DEFAULT_THICK_MARKER_WIDTH_CV2,
                valueRange = 1F..5F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.THIN_MARKER_WIDTH_CV2.key,
                title = stringResource(PreferenceKey.THIN_MARKER_WIDTH_CV2.title),
                defaultValue = DEFAULT_THIN_MARKER_WIDTH_CV2,
                valueRange = 1F..5F,
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
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.CENTER_CIRCLE_RADIUS_CV2.key,
                title = stringResource(PreferenceKey.CENTER_CIRCLE_RADIUS_CV2.title),
                defaultValue = DEFAULT_CENTER_CIRCLE_RADIUS_CV2,
                valueRange = 1F..10F,
            )
        }
    }
}