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
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_DIGIT_EMP_QUARTERS
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_DIGIT_FONT_SIZE
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_DIGIT_POSITION
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_HAND_LEN_HOURS
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_HAND_LEN_MIN
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_HAND_STYLE
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_HAND_WIDTH_HOURS
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_HAND_WIDTH_MIN
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_INNER_CIRCLE_RADIUS
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_SHOW_SECOND_HAND
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_TICK_LEN_HOURS
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_TICK_LEN_MINUTES
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_TICK_START_HOURS
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_TICK_START_MINUTES
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_TICK_STYLE_HOURS
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Companion.DEFAULT_TICK_STYLE_MINUTES
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.DigitStyle
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig.Font

fun drawAnalogNightdreamPrefs(
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
                key = PreferenceKey.DIGIT_STYLE.key,
                title = stringResource(PreferenceKey.DIGIT_STYLE.title),
                defaultValue = DigitStyle.getDefaultId(),
                useSelectedAsSummary = true,
                entries = DigitStyle.toMap(context),
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_POSITION.key,
                title = stringResource(PreferenceKey.DIGIT_POSITION.title),
                defaultValue = DEFAULT_DIGIT_POSITION,
                valueRange = 0.1F..1F,
            )
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_EMP_QUARTERS.key,
                title = stringResource(PreferenceKey.DIGIT_EMP_QUARTERS.title),
                defaultChecked = DEFAULT_DIGIT_EMP_QUARTERS
            )
            ListPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_FONT.key,
                title = stringResource(PreferenceKey.DIGIT_FONT.title),
                defaultValue = Font.getDefault(),
                useSelectedAsSummary = true,
                entries = Font.toMap(context),
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_FONT_SIZE.key,
                title = stringResource(PreferenceKey.DIGIT_FONT_SIZE.title),
                defaultValue = DEFAULT_DIGIT_FONT_SIZE,
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
                key = PreferenceKey.HAND_STYLE.key,
                title = stringResource(PreferenceKey.HAND_STYLE.title),
                defaultValue = DEFAULT_HAND_STYLE,
                useSelectedAsSummary = true,
                entries = AnalogClockConfig.HandStyle.toMap(context),
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_LENGTH_MINUTES.key,
                title = stringResource(PreferenceKey.HAND_LENGTH_MINUTES.title),
                defaultValue = DEFAULT_HAND_LEN_MIN,
                valueRange = 0.3F..1F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_LENGTH_HOURS.key,
                title = stringResource(PreferenceKey.HAND_LENGTH_HOURS.title),
                defaultValue = DEFAULT_HAND_LEN_HOURS,
                valueRange = 0.2F..0.8F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_WIDTH_MINUTES.key,
                title = stringResource(PreferenceKey.HAND_WIDTH_MINUTES.title),
                defaultValue = DEFAULT_HAND_WIDTH_MIN,
                valueRange = 3F..10F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_WIDTH_HOURS.key,
                title = stringResource(PreferenceKey.HAND_WIDTH_HOURS.title),
                defaultValue = DEFAULT_HAND_WIDTH_HOURS,
                valueRange = 3F..10F,
            )
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.HAND_SHOW_SECOND_HAND.key,
                title = stringResource(PreferenceKey.HAND_SHOW_SECOND_HAND.title),
                defaultChecked = DEFAULT_SHOW_SECOND_HAND
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
                key = PreferenceKey.TICK_STYLE_MINUTES.key,
                title = stringResource(PreferenceKey.TICK_STYLE_MINUTES.title),
                defaultValue = DEFAULT_TICK_STYLE_MINUTES,
                useSelectedAsSummary = true,
                entries = AnalogClockConfig.TickStyle.toMap(context),
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.TICK_START_MINUTES.key,
                title = stringResource(PreferenceKey.TICK_START_MINUTES.title),
                defaultValue = DEFAULT_TICK_START_MINUTES,
                valueRange = 0.2F..1F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.TICK_LEN_MINUTES.key,
                title = stringResource(PreferenceKey.TICK_LEN_MINUTES.title),
                defaultValue = DEFAULT_TICK_LEN_MINUTES,
                valueRange = 1F..15F,
            )
            ListPref(
                modifier = modifier,
                key = PreferenceKey.TICK_STYLE_HOURS.key,
                title = stringResource(PreferenceKey.TICK_STYLE_HOURS.title),
                defaultValue = DEFAULT_TICK_STYLE_HOURS,
                useSelectedAsSummary = true,
                entries = AnalogClockConfig.TickStyle.toMap(context),
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.TICK_START_HOURS.key,
                title = stringResource(PreferenceKey.TICK_START_HOURS.title),
                defaultValue = DEFAULT_TICK_START_HOURS,
                valueRange = 0.3F..1F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.TICK_LEN_HOURS.key,
                title = stringResource(PreferenceKey.TICK_LEN_HOURS.title),
                defaultValue = DEFAULT_TICK_LEN_HOURS,
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
                key = PreferenceKey.INNER_CIRCLE_RADIUS.key,
                title = stringResource(PreferenceKey.INNER_CIRCLE_RADIUS.title),
                defaultValue = DEFAULT_INNER_CIRCLE_RADIUS,
                valueRange = 0F..0.5F,
            )
        }
    }
}

fun drawDigitalFlipClockPrefs(
    modifier: Modifier,
    scope: PrefsScope
) {

}