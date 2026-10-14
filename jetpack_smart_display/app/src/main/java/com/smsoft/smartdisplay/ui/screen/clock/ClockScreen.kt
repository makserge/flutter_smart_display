package com.smsoft.smartdisplay.ui.screen.clock

import com.smsoft.smartdisplay.utils.ensureContrast
import androidx.compose.foundation.layout.Box
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsoft.smartdisplay.data.ClockType
import com.smsoft.smartdisplay.ui.composable.clock.clockview.ClockView
import com.smsoft.smartdisplay.ui.composable.clock.clockview2.ClockView2
import com.smsoft.smartdisplay.ui.composable.clock.digitalclock.DigitalClock
import com.smsoft.smartdisplay.ui.composable.clock.digitalclock2.DigitalClock2
import com.smsoft.smartdisplay.ui.composable.clock.digitalmatrixclock.DigitalMatrixClock
import com.smsoft.smartdisplay.ui.composable.clock.fsclock.FSAnalogClock
import com.smsoft.smartdisplay.ui.composable.clock.jetalarm.JetAlarm
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.DigitalFlipClock
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.NightdreamAnalogClock
import com.smsoft.smartdisplay.ui.composable.clock.rectangular.AnalogClockRectangular
import com.smsoft.smartdisplay.utils.getStateFromFlow

@Composable
fun ClockScreen(
    modifier: Modifier = Modifier,
    scale: Float = 1F,
    viewModel: ClockViewModel = hiltViewModel()
) {
    val clockType = getStateFromFlow(
        flow = viewModel.clockType,
        defaultValue = ClockType.getDefault()
    ) as ClockType

    val clockUiState: ClockUiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Collected from the StateFlow itself: a default here would draw a 12-hour panel in
    // 24-hour format for the first frame
    val is24Hour by viewModel.is24Hour.collectAsStateWithLifecycle()

    // The theme's colors unless one is picked in Settings; either way kept clear of the
    // background, which changes with the day/night theme (a white picked at night would vanish
    // on the day background). The defaults used to be remembered once and missed a theme change.
    val background = MaterialTheme.colors.background
    val customPrimary = getStateFromFlow(flow = viewModel.primaryColor, defaultValue = null) as String?
    val primaryColor = clockColor(customPrimary, MaterialTheme.colors.primary, background)
    val customSecondary = getStateFromFlow(flow = viewModel.secondaryColor, defaultValue = null) as String?
    val secondaryColor = clockColor(customSecondary, MaterialTheme.colors.secondary, background)

    Box(
        modifier = Modifier
    ) {
        DrawClock(
            modifier = Modifier,
            clockType = clockType,
            scale = scale,
            uiState = clockUiState,
            is24Hour = is24Hour,
            viewModel = viewModel,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor
        )
    }
}

@Composable
fun DrawClock(
    modifier: Modifier = Modifier,
    clockType: ClockType,
    scale: Float,
    uiState: ClockUiState,
    is24Hour: Boolean,
    viewModel: ClockViewModel,
    primaryColor: Color,
    secondaryColor: Color
) {
    when (clockType) {
        ClockType.ANALOG_NIGHTDREAM ->
            NightdreamAnalogClock(
                modifier = modifier,
                viewModel = viewModel,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second
            )
        ClockType.ANALOG_CLOCKVIEW ->
            ClockView(
                modifier = modifier,
                viewModel = viewModel,
                scale = scale,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second
            )
        ClockType.ANALOG_CLOCKVIEW2 ->
            ClockView2(
                modifier = modifier,
                viewModel = viewModel,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second,
                milliSecond = uiState.milliSecond
            )
        ClockType.ANALOG_RECTANGULAR ->
            AnalogClockRectangular(
                modifier = modifier,
                viewModel = viewModel,
                scale = scale,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second,
                millisecond = uiState.milliSecond
            )
        ClockType.ANALOG_FSCLOCK ->
            FSAnalogClock(
                modifier = modifier,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second,
                milliSecond = uiState.milliSecond
            )
        ClockType.ANALOG_JETALARM -> {
            JetAlarm(
                modifier = modifier,
                viewModel = viewModel,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second,
                milliSecond = uiState.milliSecond
            )
        }
        ClockType.DIGITAL_FLIPCLOCK -> {
            DigitalFlipClock(
                modifier = modifier,
                viewModel = viewModel,
                scale = scale,
                hour = uiState.hour,
                minute = uiState.minute,
                is24Hour = is24Hour
            )
        }
        ClockType.DIGITAL_MATRIXCLOCK -> {
            DigitalMatrixClock(
                modifier = modifier,
                viewModel = viewModel,
                scale = scale,
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second,
                is24Hour = is24Hour
            )
        }
        ClockType.DIGITAL_CLOCK -> {
            DigitalClock(
                modifier = modifier,
                viewModel = viewModel,
                scale = scale,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                year = uiState.year,
                month = uiState.month,
                day = uiState.day,
                dayOfWeek = uiState.dayOfWeek,
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second,
                is24Hour = is24Hour
            )
        }
        ClockType.DIGITAL_CLOCK2 -> {
            DigitalClock2(
                modifier = modifier,
                viewModel = viewModel,
                scale = scale,
                primaryColor = primaryColor,
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second,
                is24Hour = is24Hour
            )
        }
    }
}

private fun getColor(color: Color) = Color(android.graphics.Color.parseColor("#${Integer.toHexString(color.toArgb())}"))

/** [custom] (a "#RRGGBB" setting) or [default], with enough contrast to [background]. */
private fun clockColor(custom: String?, default: Color, background: Color): Color {
    val color = custom?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() } ?: default
    return ensureContrast(color, background)
}
