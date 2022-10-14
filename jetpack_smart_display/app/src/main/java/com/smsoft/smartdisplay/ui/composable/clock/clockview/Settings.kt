package com.smsoft.smartdisplay.ui.composable.clock.clockview

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
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.AnalogClockConfig

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
    /*
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

     */
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
        /*
        prefsItem {
            ListPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_STYLE.key,
                title = stringResource(PreferenceKey.DIGIT_STYLE.title),
                defaultValue = AnalogClockConfig.DigitStyle.getDefaultId(),
                useSelectedAsSummary = true,
                entries = AnalogClockConfig.DigitStyle.toMap(context),
            )
        }
        prefsItem {
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_POSITION.key,
                title = stringResource(PreferenceKey.DIGIT_POSITION.title),
                defaultValue = AnalogClockConfig.DEFAULT_DIGIT_POSITION,
                valueRange = 0.1F..1F,
            )
        }
        prefsItem {
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_EMP_QUARTERS.key,
                title = stringResource(PreferenceKey.DIGIT_EMP_QUARTERS.title),
                defaultChecked = AnalogClockConfig.DEFAULT_DIGIT_EMP_QUARTERS
            )
        }
        prefsItem {
            ListPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_FONT.key,
                title = stringResource(PreferenceKey.DIGIT_FONT.title),
                defaultValue = AnalogClockConfig.Font.getDefault(),
                useSelectedAsSummary = true,
                entries = AnalogClockConfig.Font.toMap(context),
            )
        }
        prefsItem {
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_FONT_SIZE.key,
                title = stringResource(PreferenceKey.DIGIT_FONT_SIZE.title),
                defaultValue = AnalogClockConfig.DEFAULT_DIGIT_FONT_SIZE,
                valueRange = 5F..20F,
            )
        }

         */
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
                defaultValue = AnalogClockConfig.DEFAULT_HAND_STYLE,
                useSelectedAsSummary = true,
                entries = AnalogClockConfig.HandStyle.toMap(context),
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_LENGTH_MINUTES.key,
                title = stringResource(PreferenceKey.HAND_LENGTH_MINUTES.title),
                defaultValue = AnalogClockConfig.DEFAULT_HAND_LEN_MIN,
                valueRange = 0.3F..1F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_LENGTH_HOURS.key,
                title = stringResource(PreferenceKey.HAND_LENGTH_HOURS.title),
                defaultValue = AnalogClockConfig.DEFAULT_HAND_LEN_HOURS,
                valueRange = 0.2F..0.8F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_WIDTH_MINUTES.key,
                title = stringResource(PreferenceKey.HAND_WIDTH_MINUTES.title),
                defaultValue = AnalogClockConfig.DEFAULT_HAND_WIDTH_MIN,
                valueRange = 3F..10F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_WIDTH_HOURS.key,
                title = stringResource(PreferenceKey.HAND_WIDTH_HOURS.title),
                defaultValue = AnalogClockConfig.DEFAULT_HAND_WIDTH_HOURS,
                valueRange = 3F..10F,
            )
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.HAND_SHOW_SECOND_HAND.key,
                title = stringResource(PreferenceKey.HAND_SHOW_SECOND_HAND.title),
                defaultChecked = AnalogClockConfig.DEFAULT_SHOW_SECOND_HAND
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
                defaultValue = AnalogClockConfig.DEFAULT_TICK_STYLE_MINUTES,
                useSelectedAsSummary = true,
                entries = AnalogClockConfig.TickStyle.toMap(context),
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.TICK_START_MINUTES.key,
                title = stringResource(PreferenceKey.TICK_START_MINUTES.title),
                defaultValue = AnalogClockConfig.DEFAULT_TICK_START_MINUTES,
                valueRange = 0.2F..1F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.TICK_LEN_MINUTES.key,
                title = stringResource(PreferenceKey.TICK_LEN_MINUTES.title),
                defaultValue = AnalogClockConfig.DEFAULT_TICK_LEN_MINUTES,
                valueRange = 1F..15F,
            )
            ListPref(
                modifier = modifier,
                key = PreferenceKey.TICK_STYLE_HOURS.key,
                title = stringResource(PreferenceKey.TICK_STYLE_HOURS.title),
                defaultValue = AnalogClockConfig.DEFAULT_TICK_STYLE_HOURS,
                useSelectedAsSummary = true,
                entries = AnalogClockConfig.TickStyle.toMap(context),
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.TICK_START_HOURS.key,
                title = stringResource(PreferenceKey.TICK_START_HOURS.title),
                defaultValue = AnalogClockConfig.DEFAULT_TICK_START_HOURS,
                valueRange = 0.3F..1F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.TICK_LEN_HOURS.key,
                title = stringResource(PreferenceKey.TICK_LEN_HOURS.title),
                defaultValue = AnalogClockConfig.DEFAULT_TICK_LEN_HOURS,
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
                defaultValue = AnalogClockConfig.DEFAULT_INNER_CIRCLE_RADIUS,
                valueRange = 0F..0.5F,
            )
        }
    }
}