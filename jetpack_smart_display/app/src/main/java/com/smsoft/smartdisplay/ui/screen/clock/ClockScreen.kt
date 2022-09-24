package com.smsoft.smartdisplay.ui.screen.clock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.damian.digitalclock.DigitalClock

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
    //AnalogClock()

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
        hour = uiState.hour,
        minute = uiState.minute,
        second = uiState.second
    )
    ClockView2(
        hour = uiState.hour,
        minute = uiState.minute,
        second = uiState.second,
        millisecond = uiState.millisecond
    )

    JetAlarm(
        hour = uiState.hour,
        minute = uiState.minute,
        second = uiState.second,
        millisecond = uiState.millisecond
    )


    MatrixDisplay(
        hour = uiState.hour,
        minute = uiState.minute,
        second = uiState.second
    )

     */
    DigitalClock(
        year = uiState.year,
        month = uiState.month,
        day = uiState.day,
        dayOfWeek = uiState.dayOfWeek,
        hour = uiState.hour,
        minute = uiState.minute,
        second = uiState.second
    )
}