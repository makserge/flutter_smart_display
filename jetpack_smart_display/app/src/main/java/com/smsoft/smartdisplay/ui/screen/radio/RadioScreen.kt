package com.smsoft.smartdisplay.ui.screen.radio

//noinspection SuspiciousImport
import android.R
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startForegroundService
import androidx.hilt.navigation.compose.hiltViewModel
import com.smsoft.smartdisplay.service.RadioMediaService

@Composable
fun RadioScreen(
    modifier: Modifier = Modifier,
    viewModel: RadioViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val state = viewModel.uiState.collectAsState()
    val presetTitle = viewModel.presetTitle

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 16.dp
            ),
        text = presetTitle
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (state.value) {
            UIState.Initial -> CircularProgressIndicator()
            is UIState.Ready -> {
                onStartService(context)
                fadeInVolume(viewModel)
                RadioMediaPlayerUI(
                    metaTitle = viewModel.metaTitle,
                    durationString = if (viewModel.duration > 0) viewModel.formatDuration(viewModel.duration) else "",
                    playResourceProvider = {
                        if (viewModel.isPlaying) {
                            R.drawable.ic_media_pause
                        }
                        else {
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if ("".equals(durationString)) {
            Text(
                text = progressString
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
        Text(
            text = metaTitle
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
                text = progressString
            )
            Text(
                modifier = Modifier,
                text = durationString
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
            contentDescription = "Previous Button",
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
            contentDescription = "Play/Pause Button",
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
            contentDescription = "Next Button",
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

private fun onStartService(context: Context) {
    val intent = Intent(context, RadioMediaService::class.java)
    startForegroundService(context, intent)
}