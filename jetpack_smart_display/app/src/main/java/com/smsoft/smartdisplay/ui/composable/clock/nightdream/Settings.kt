package com.smsoft.smartdisplay.ui.composable.clock.nightdream

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
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_DIGIT_EMP_QUARTERS_ND
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_DIGIT_FONT_SIZE_ND
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_DIGIT_POSITION_ND
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_HAND_LEN_HOURS_ND
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_HAND_LEN_MIN_ND
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_HAND_STYLE_ND
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_HAND_WIDTH_HOURS_ND
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_HAND_WIDTH_MIN_ND
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_INNER_CIRCLE_RADIUS_ND
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_SHOW_SECOND_HAND_ND
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_TICK_LEN_HOURS_ND
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_TICK_LEN_MINUTES_ND
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_TICK_START_HOURS_ND
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_TICK_START_MINUTES_ND
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_TICK_STYLE_HOURS_ND
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_TICK_STYLE_MINUTES_ND
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.DigitStyle
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Font

fun analogNightdreamPrefs(
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
                key = PreferenceKey.DIGIT_STYLE_ND.key,
                title = stringResource(PreferenceKey.DIGIT_STYLE_ND.title),
                defaultValue = DigitStyle.getDefaultId(),
                useSelectedAsSummary = true,
                entries = DigitStyle.toMap(context),
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_POSITION_ND.key,
                title = stringResource(PreferenceKey.DIGIT_POSITION_ND.title),
                defaultValue = DEFAULT_DIGIT_POSITION_ND,
                valueRange = 0.1F..1F,
            )
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_EMP_QUARTERS_ND.key,
                title = stringResource(PreferenceKey.DIGIT_EMP_QUARTERS_ND.title),
                defaultChecked = DEFAULT_DIGIT_EMP_QUARTERS_ND
            )
            ListPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_FONT_ND.key,
                title = stringResource(PreferenceKey.DIGIT_FONT_ND.title),
                defaultValue = Font.getDefault(),
                useSelectedAsSummary = true,
                entries = Font.toMap(context),
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_FONT_SIZE_ND.key,
                title = stringResource(PreferenceKey.DIGIT_FONT_SIZE_ND.title),
                defaultValue = DEFAULT_DIGIT_FONT_SIZE_ND,
                valueRange = 5F..20F,
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
fun drawHands(
    modifier: Modifier,
    scope: PrefsScope,
    context: Context
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.hands)
        )
    }) {
        prefsItem {
            ListPref(
                modifier = modifier,
                key = PreferenceKey.HAND_STYLE_ND.key,
                title = stringResource(PreferenceKey.HAND_STYLE_ND.title),
                defaultValue = DEFAULT_HAND_STYLE_ND,
                useSelectedAsSummary = true,
                entries = AnalogClockConfig.HandStyle.toMap(context),
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_LENGTH_MINUTES_ND.key,
                title = stringResource(PreferenceKey.HAND_LENGTH_MINUTES_ND.title),
                defaultValue = DEFAULT_HAND_LEN_MIN_ND,
                valueRange = 0.3F..1F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_LENGTH_HOURS_ND.key,
                title = stringResource(PreferenceKey.HAND_LENGTH_HOURS_ND.title),
                defaultValue = DEFAULT_HAND_LEN_HOURS_ND,
                valueRange = 0.2F..0.8F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_WIDTH_MINUTES_ND.key,
                title = stringResource(PreferenceKey.HAND_WIDTH_MINUTES_ND.title),
                defaultValue = DEFAULT_HAND_WIDTH_MIN_ND,
                valueRange = 3F..10F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_WIDTH_HOURS_ND.key,
                title = stringResource(PreferenceKey.HAND_WIDTH_HOURS_ND.title),
                defaultValue = DEFAULT_HAND_WIDTH_HOURS_ND,
                valueRange = 3F..10F,
            )
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.HAND_SHOW_SECOND_HAND_ND.key,
                title = stringResource(PreferenceKey.HAND_SHOW_SECOND_HAND_ND.title),
                defaultChecked = DEFAULT_SHOW_SECOND_HAND_ND
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
            ListPref(
                modifier = modifier,
                key = PreferenceKey.TICK_STYLE_MINUTES_ND.key,
                title = stringResource(PreferenceKey.TICK_STYLE_MINUTES_ND.title),
                defaultValue = DEFAULT_TICK_STYLE_MINUTES_ND,
                useSelectedAsSummary = true,
                entries = AnalogClockConfig.TickStyle.toMap(context),
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.TICK_START_MINUTES_ND.key,
                title = stringResource(PreferenceKey.TICK_START_MINUTES_ND.title),
                defaultValue = DEFAULT_TICK_START_MINUTES_ND,
                valueRange = 0.2F..1F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.TICK_LEN_MINUTES_ND.key,
                title = stringResource(PreferenceKey.TICK_LEN_MINUTES_ND.title),
                defaultValue = DEFAULT_TICK_LEN_MINUTES_ND,
                valueRange = 1F..15F,
            )
            ListPref(
                modifier = modifier,
                key = PreferenceKey.TICK_STYLE_HOURS_ND.key,
                title = stringResource(PreferenceKey.TICK_STYLE_HOURS_ND.title),
                defaultValue = DEFAULT_TICK_STYLE_HOURS_ND,
                useSelectedAsSummary = true,
                entries = AnalogClockConfig.TickStyle.toMap(context),
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.TICK_START_HOURS_ND.key,
                title = stringResource(PreferenceKey.TICK_START_HOURS_ND.title),
                defaultValue = DEFAULT_TICK_START_HOURS_ND,
                valueRange = 0.3F..1F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.TICK_LEN_HOURS_ND.key,
                title = stringResource(PreferenceKey.TICK_LEN_HOURS_ND.title),
                defaultValue = DEFAULT_TICK_LEN_HOURS_ND,
                valueRange = 2F..8F,
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
                key = PreferenceKey.INNER_CIRCLE_RADIUS_ND.key,
                title = stringResource(PreferenceKey.INNER_CIRCLE_RADIUS_ND.title),
                defaultValue = DEFAULT_INNER_CIRCLE_RADIUS_ND,
                valueRange = 0F..0.5F,
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
fun digitalFlipClockPrefs(
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
                key = PreferenceKey.FONT_SIZE_FC.key,
                title = stringResource(PreferenceKey.FONT_SIZE_FC.title),
                defaultValue = DEFAULT_TEXT_SIZE_FC,
                valueRange = 200F..450F,
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
                key = PreferenceKey.REVERSE_ROTATION_FC.key,
                title = stringResource(PreferenceKey.REVERSE_ROTATION_FC.title),
                defaultChecked = DEFAULT_REVERSE_ROTATION_FC
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.PADDING_FC.key,
                title = stringResource(PreferenceKey.PADDING_FC.title),
                defaultValue = DEFAULT_PADDING_FC,
                valueRange = 0F..15F,
            )
        }
    }
}