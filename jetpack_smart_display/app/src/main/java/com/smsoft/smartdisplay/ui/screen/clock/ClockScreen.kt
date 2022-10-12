package com.smsoft.smartdisplay.ui.screen.clock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.firebirdberlin.nightdream.NightdreamAnalogClock
import com.smsoft.smartdisplay.data.ClockType
import com.smsoft.smartdisplay.ui.composable.analog.AnalogClockRectangular
import io.ak1.jetalarm.JetAlarm
import systems.sieber.fsclock.FSAnalogClock

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ClockScreen(
    modifier: Modifier = Modifier,
    scale: Float = 1F,
    viewModel: ClockViewModel = hiltViewModel(),
    onClick: () -> Unit,
) {
    val clockType = viewModel.clockType.collectAsStateWithLifecycle(
        initialValue = ClockType.getDefault()
    ).value

    val clockUiState: ClockUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dataStore = viewModel.dataStore

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose {
            viewModel.onStop()
        }
    }
    Box(
        modifier = Modifier
            .clickable(
                onClick = onClick
            ),
    ) {
        DrawClock(
            modifier = Modifier,
            clockType = clockType,
            scale = scale,
            uiState = clockUiState,
            dataStore = dataStore
        )
    }
}

@Composable
fun DrawClock(
    modifier: Modifier = Modifier,
    clockType: ClockType,
    scale: Float,
    uiState: ClockUiState,
    dataStore: DataStore<Preferences>
) {
    when (clockType) {
        ClockType.ANALOG_ROUND ->
            NightdreamAnalogClock(
                modifier = modifier,
                dataStore = dataStore,
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second
            )
        ClockType.ANALOG_RECTANGULAR ->
            AnalogClockRectangular(
                modifier = modifier,
                dataStore = dataStore,
                scale = scale,
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second,
                millisecond = uiState.milliSecond
            )
        ClockType.ANALOG_FSCLOCK ->
            FSAnalogClock(
                modifier = modifier,
                dataStore = dataStore,
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second,
                milliSecond = uiState.milliSecond
            )
        ClockType.ANALOG_JETALARM -> {
            JetAlarm(
                modifier = modifier,
                dataStore = dataStore,
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second,
                milliSecond = uiState.milliSecond
            )
        }
        ClockType.DIGITAL -> {

        }
    }
/*


    ClockView2(
        hour = uiState.hour,
        minute = uiState.minute,
        second = uiState.second,
        milliSecond = uiState.milliSecond
    )

    JetAlarm(
        hour = uiState.hour,
        minute = uiState.minute,
        second = uiState.second,
        milliSecond = uiState.milliSecond
    )
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
