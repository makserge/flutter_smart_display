package com.smsoft.smartdisplay.ui.screen.clock

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.hilt.navigation.compose.hiltViewModel
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

@OptIn(ExperimentalFoundationApi::class)
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

    val defaultPrimaryColor = getColor(MaterialTheme.colors.primary)
    var primaryColor by remember { mutableStateOf(defaultPrimaryColor) }
    getStateFromFlow(
        flow = viewModel.primaryColor,
        defaultValue = null
    )?.let {
        primaryColor = Color(android.graphics.Color.parseColor(it as String))
    }

    val defaultSecondaryColor = getColor(MaterialTheme.colors.secondary)
    var secondaryColor by remember { mutableStateOf(defaultSecondaryColor) }
    getStateFromFlow(
        flow = viewModel.secondaryColor,
        defaultValue = null
    )?.let {
        secondaryColor = Color(android.graphics.Color.parseColor(it as String))
    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.onStart()
    }
    Box(
        modifier = Modifier
    ) {
        DrawClock(
            modifier = Modifier,
            clockType = clockType,
            scale = scale,
            uiState = clockUiState,
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
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                hour = uiState.hour,
                minute = uiState.minute
            )
        }
        ClockType.DIGITAL_MATRIXCLOCK -> {
            DigitalMatrixClock(
                modifier = modifier,
                viewModel = viewModel,
                scale = scale,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second
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
                second = uiState.second
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
                second = uiState.second
            )
        }
    }
}

private fun getColor(color: Color) = Color(android.graphics.Color.parseColor("#${Integer.toHexString(color.toArgb())}"))