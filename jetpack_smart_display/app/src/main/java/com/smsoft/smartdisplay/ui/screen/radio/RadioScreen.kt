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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.VoiceCommand
import com.smsoft.smartdisplay.ui.composable.radio.RadioMediaPlayerUI
import com.smsoft.smartdisplay.ui.composable.radio.VolumeControl

@UnstableApi
@Composable
fun RadioScreen(
    modifier: Modifier = Modifier,
    viewModel: RadioViewModel = hiltViewModel(),
    command: VoiceCommand,
    onResetCommand: () -> Unit,
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
        viewModel.onStartService()
        onDispose {
            viewModel.onStopService()
        }
    }
    LaunchedEffect(command.timeStamp) {
        viewModel.processVoiceCommand(command.type)
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
                        isProgressEnabled = viewModel.isInternalPlayer(),
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
            VolumeControl(
                modifier = Modifier,
                value = (viewModel.volume.floatValue * 100).toInt(),
                onValueChange = {
                    viewModel.setVolume(it.toFloat() / 100)
                }
            )
        }
    }
}