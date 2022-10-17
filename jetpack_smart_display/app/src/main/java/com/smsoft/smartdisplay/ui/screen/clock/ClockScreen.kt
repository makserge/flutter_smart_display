package com.smsoft.smartdisplay.ui.screen.clock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsoft.smartdisplay.data.ClockType
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.getParam
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

    val defaultPrimaryColor = getColor(MaterialTheme.colors.primary)
    var primaryColor by remember { mutableStateOf(defaultPrimaryColor) }
    getColorSetings(
        key = PreferenceKey.PRIMARY_COLOR,
        dataStore = dataStore,
        defaultValue = MaterialTheme.colors.primary
    )?.let {
        primaryColor = Color(android.graphics.Color.parseColor(it))
    }

    val defaultSecondaryColor = getColor(MaterialTheme.colors.secondary)
    var secondaryColor by remember { mutableStateOf(defaultSecondaryColor) }
    getColorSetings(
        key = PreferenceKey.SECONDARY_COLOR,
        dataStore = dataStore,
        defaultValue = MaterialTheme.colors.secondary
    )?.let {
        secondaryColor = Color(android.graphics.Color.parseColor(it))
    }

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
            dataStore = dataStore,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
        )
    }
}

@Composable
fun DrawClock(
    modifier: Modifier = Modifier,
    clockType: ClockType,
    scale: Float,
    uiState: ClockUiState,
    dataStore: DataStore<Preferences>,
    primaryColor: Color,
    secondaryColor: Color
) {
    when (clockType) {
        ClockType.ANALOG_NIGHTDREAM ->
            NightdreamAnalogClock(
                modifier = modifier,
                dataStore = dataStore,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second
            )
        ClockType.ANALOG_CLOCKVIEW ->
            ClockView(
                modifier = modifier,
                dataStore = dataStore,
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
                dataStore = dataStore,
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
                dataStore = dataStore,
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
                dataStore = dataStore,
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
                dataStore = dataStore,
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
                dataStore = dataStore,
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
                dataStore = dataStore,
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
                dataStore = dataStore,
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
                dataStore = dataStore,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                hour = uiState.hour,
                minute = uiState.minute,
                second = uiState.second,
                millisecond = uiState.milliSecond
            )
        }
    }
}

@Composable
private fun getColorSetings(
    key: PreferenceKey,
    dataStore: DataStore<Preferences>,
    defaultValue: Color
): String? {
    return getParam(
        dataStore = dataStore,
        defaultValue = "#${Integer.toHexString(defaultValue.toArgb())}"
    ) { preferences -> preferences[stringPreferencesKey(key.key)] }
    as String?
}

private fun getColor(color: Color) = Color(android.graphics.Color.parseColor("#${Integer.toHexString(color.toArgb())}"))