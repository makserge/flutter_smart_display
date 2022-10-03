package com.smsoft.smartdisplay.ui.screen.clock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.ak1.jetalarm.JetAlarm

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ClockScreen(
    modifier: Modifier = Modifier,
    viewModel: ClockViewModel = hiltViewModel(),
) {
    val uiState: ClockUiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose {
            viewModel.onStop()
        }
    }
/*
    AnalogClock(
        hour = uiState.hour,
        minute = uiState.minute,
        second = uiState.second,
        millisecond = uiState.millisecond
    )

    FSAnalogClock(
        hour = uiState.hour,
        minute = uiState.minute,
        second = uiState.second,
        millisecond = uiState.millisecond
    )
    NightdreamAnalogClock(
        hour = uiState.hour,
        minute = uiState.minute,
        second = uiState.second
    )

    ClockView(
        modifier = modifier,
        hour = uiState.hour,
        minute = uiState.minute,
        second = uiState.second
    )
    ClockView2(
        hour = uiState.hour,
        minute = uiState.minute,
        second = uiState.second,
        milliSecond = uiState.milliSecond
    )
    */
    JetAlarm(
        hour = uiState.hour,
        minute = uiState.minute,
        second = uiState.second,
        milliSecond = uiState.milliSecond
    )
/*
    MatrixDisplay(
        hour = uiState.hour,
        minute = uiState.minute,
        second = uiState.second
    )
    DigitalClock(
        year = uiState.year,
        month = uiState.month,
        day = uiState.day,
        dayOfWeek = uiState.dayOfWeek,
        hour = uiState.hour,
        minute = uiState.minute,
        second = uiState.second
    )

    DigitalClock2(
        hour = uiState.hour,
        minute = uiState.minute,
        second = uiState.second,
        millisecond = uiState.millisecond
    )

    NightdreamFlipDigitalClock(
        hour = uiState.hour,
        minute = uiState.minute
    )*/
}