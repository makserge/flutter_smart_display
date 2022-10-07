package com.smsoft.smartdisplay.ui.screen.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
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
import com.smsoft.smartdisplay.ui.composable.analog.AnalogClock
import systems.sieber.fsclock.FSAnalogClock

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ClockScreen(
    modifier: Modifier = Modifier,
    viewModel: ClockViewModel = hiltViewModel(),
    width: Int? = null,
    height: Int? = null,
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
    DrawClock(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .clickable(
                onClick = onClick
            ),
        width = width,
        height = height,
        clockType = clockType,
        uiState = clockUiState,
        dataStore = dataStore
    )
}

@Composable
fun DrawClock(
    modifier: Modifier = Modifier,
    width: Int? = null,
    height: Int? = null,
    clockType: ClockType,
    uiState: ClockUiState,
    dataStore: DataStore<Preferences>
) {
    when (clockType) {
        ClockType.ANALOG_ROUND ->
            NightdreamAnalogClock(
                modifier = modifier,
                width = width,
                height = height,
                dataStore = dataStore,
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second
            )
        ClockType.ANALOG_RECTANGULAR ->
            AnalogClock(
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second,
                millisecond = uiState.milliSecond
            )
        ClockType.ANALOG_FSCLOCK ->
            FSAnalogClock(
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second,
                millisecond = uiState.milliSecond
            )
        ClockType.DIGITAL -> {

        }
    }
/*

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
