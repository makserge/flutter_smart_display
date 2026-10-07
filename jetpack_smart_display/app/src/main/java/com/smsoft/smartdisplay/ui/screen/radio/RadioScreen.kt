package com.smsoft.smartdisplay.ui.screen.radio

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.DashboardItem
import com.smsoft.smartdisplay.data.VoiceCommand
import com.smsoft.smartdisplay.service.radio.RadioVolume
import com.smsoft.smartdisplay.ui.composable.radio.RadioMediaPlayerUI
import com.smsoft.smartdisplay.ui.composable.radio.VolumeControl

/**
 * @param isSettled true once the pager has come to rest on this page. The radio starts only
 * then, so a programmatic jump that merely passes over the radio page does not start it.
 * @param command the pending voice command, or null. Radio commands are handled once and then
 * cleared with [onCommandHandled], so they are not replayed when the page is opened again.
 */
@UnstableApi
@Composable
fun RadioScreen(
    modifier: Modifier = Modifier,
    viewModel: RadioViewModel = hiltViewModel(),
    isSettled: Boolean,
    command: VoiceCommand?,
    onCommandHandled: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    val isShowVolume = viewModel.isShowVolume.collectAsStateWithLifecycle()

    if (state.value is UIState.Error) {
        LaunchedEffect(Unit) {
            viewModel.resetState()
            onSettingsClick()
        }
    }
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.onLeave()
        }
    }
    LaunchedEffect(isSettled, command?.timeStamp) {
        if (!isSettled) {
            return@LaunchedEffect
        }
        val radioCommand = command?.type?.takeIf { it.page == DashboardItem.INTERNET_RADIO }
        viewModel.onEnter(pendingCommand = radioCommand)
        if (radioCommand != null) {
            viewModel.processVoiceCommand(radioCommand)
            onCommandHandled()
        }
    }

    Box (
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit){
                detectTapGestures(
                    onTap = {
                        viewModel.setShowVolume()
                    },
                    onLongPress = {
                        onSettingsClick()
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (state.value) {
                UIState.Initial -> CircularProgressIndicator()
                UIState.Ready -> {
                    RadioMediaPlayerUI(
                        isProgressEnabled = viewModel.isInternalRadio.value,
                        presetTitle = viewModel.presetTitle.value,
                        metaTitle = viewModel.metaTitle.value,
                        durationString = if (viewModel.duration.longValue > 0) viewModel.formatDuration(viewModel.duration.longValue) else "",
                        playResourceProvider = {
                            if (viewModel.isPlaying.value) {
                                R.drawable.ic_pause_48
                            } else {
                                R.drawable.ic_play_arrow_48
                            }
                        },
                        progressProvider = { Pair(viewModel.progress.floatValue, viewModel.progressString.value) },
                        onUiEvent = viewModel::onUIEvent
                    )
                }
                UIState.Error -> {}
            }
        }
        if (isShowVolume.value) {
            // The slider shows and sets the position, even in loudness (RadioVolume), in percent
            VolumeControl(
                modifier = Modifier,
                value = RadioVolume.toPercent(viewModel.volume.floatValue),
                onValueChange = {
                    viewModel.setVolume(RadioVolume.fromPercent(it))
                }
            )
        }
    }
}
