package com.smsoft.smartdisplay.ui.composable.radio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.ui.screen.radio.UIEvent
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