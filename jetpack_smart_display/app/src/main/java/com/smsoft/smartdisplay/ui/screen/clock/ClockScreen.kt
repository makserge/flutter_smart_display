package com.smsoft.smartdisplay.ui.screen.clock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import systems.sieber.fsclock.FSClock

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ClockScreen(
    modifier: Modifier = Modifier,
    viewModel: ClockViewModel = hiltViewModel(),
) {
    //AnalogClock()

    val uiState: ClockUiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose {
            viewModel.onStop()
        }
    }

    FSClock(
        hour = uiState.hour,
        minute = uiState.minute,
        second = uiState.second,
        millisecond = uiState.millisecond
    )
}