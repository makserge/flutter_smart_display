package com.smsoft.smartdisplay.ui.screen.radio

//noinspection SuspiciousImport
import android.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RadioScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit,
    viewModel: RadioViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    val presetTitle = viewModel.presetTitle

    DisposableEffect(key1 = viewModel) {
        onDispose {
            viewModel.onStopService()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = onSettingsClick
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (state.value) {
            UIState.Initial -> CircularProgressIndicator()
            UIState.Ready -> {
                viewModel.onStartService()
                fadeInVolume(viewModel)
                RadioMediaPlayerUI(
                    presetTitle = presetTitle,
                    metaTitle = viewModel.metaTitle,
                    durationString = if (viewModel.duration > 0) viewModel.formatDuration(
                        viewModel.duration
                    ) else "",
                    playResourceProvider = {
                        if (viewModel.isPlaying) {
                            R.drawable.ic_media_pause
                        } else {
                            R.drawable.ic_media_play
                        }
                    },
                    progressProvider = { Pair(viewModel.progress, viewModel.progressString) },
                    onUiEvent = viewModel::onUIEvent
                )
            }
        }
    }
}

fun fadeInVolume(viewModel: RadioViewModel) {
    viewModel.fadeInVolume()
}

@Composable
fun RadioMediaPlayerUI(
    modifier: Modifier = Modifier,
    presetTitle: String,
    metaTitle: String,
    durationString: String,
    playResourceProvider: () -> Int,
    progressProvider: () -> Pair<Float, String>,
    onUiEvent: (UIEvent) -> Unit,
) {
    val (progress, progressString) = progressProvider()
    Column(
        modifier = Modifier
            .padding(
                horizontal = 16.dp
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = modifier,
            text = presetTitle,
            style = MaterialTheme.typography.h5,
            color = MaterialTheme.colors.primary
        )
        Spacer(
            modifier = Modifier
                .padding(20.dp)
        )
        if ("" == durationString) {
            Text(
                text = progressString,
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.primary
            )
        } else {
            RadioMediaPlayerBar(
                modifier = modifier,
                progress = progress,
                durationString = durationString,
                progressString = progressString,
                onUiEvent = onUiEvent
            )
        }
        RadioMediaPlayerControls(
            modifier = modifier,
            playResourceProvider = playResourceProvider,
            onUiEvent = onUiEvent
        )
        Spacer(
            modifier = Modifier
                .padding(20.dp)
        )
        Text(
            modifier = modifier,
            text = metaTitle,
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.primary
        )
    }
}

@Composable
private fun RadioMediaPlayerBar(
    modifier: Modifier = Modifier,
    progress: Float,
    durationString: String,
    progressString: String,
    onUiEvent: (UIEvent) -> Unit
) {
    val newProgressValue = remember { mutableStateOf(0f) }
    val useNewProgressValue = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Slider(
            modifier = Modifier
                .padding(
                    horizontal = 8.dp
                ),
            value = if (useNewProgressValue.value) newProgressValue.value else progress,
            onValueChange = { newValue ->
                useNewProgressValue.value = true
                newProgressValue.value = newValue
                onUiEvent(UIEvent.UpdateProgress(newProgress = newValue))
            },
            onValueChangeFinished = {
                useNewProgressValue.value = false
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                modifier = Modifier,
                text = progressString,
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.primary
            )
            Text(
                modifier = Modifier,
                text = durationString,
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.primary
            )
        }
    }
}

@Composable
private fun RadioMediaPlayerControls(
    modifier: Modifier = Modifier,
    playResourceProvider: () -> Int,
    onUiEvent: (UIEvent) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(35.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_media_previous),
            contentDescription = stringResource(com.smsoft.smartdisplay.R.string.previous_button),
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = {
                    onUiEvent(UIEvent.Backward)
                })
                .padding(12.dp)
                .size(34.dp)
        )
        Image(
            painter = painterResource(id = playResourceProvider()),
            contentDescription = stringResource(com.smsoft.smartdisplay.R.string.play_pause_button),
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = {
                    onUiEvent(UIEvent.PlayPause)
                })
                .padding(8.dp)
                .size(56.dp)
        )
        Icon(
            painter = painterResource(R.drawable.ic_media_next),
            contentDescription = stringResource(com.smsoft.smartdisplay.R.string.next_button),
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = {
                    onUiEvent(UIEvent.Forward)
                })
                .padding(12.dp)
                .size(34.dp)
        )
    }
}