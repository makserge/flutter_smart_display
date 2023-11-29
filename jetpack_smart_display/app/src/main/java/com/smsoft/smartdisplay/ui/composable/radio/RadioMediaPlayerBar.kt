package com.smsoft.smartdisplay.ui.composable.radio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.ui.screen.radio.UIEvent

@Composable
fun RadioMediaPlayerBar(
    modifier: Modifier = Modifier,
    progress: Float,
    durationString: String,
    progressString: String,
    onUiEvent: (UIEvent) -> Unit
) {
    val newProgressValue = remember { mutableFloatStateOf(0f) }
    val useNewProgressValue = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Slider(
            modifier = Modifier
                .padding(
                    horizontal = 8.dp
                ),
            value = if (useNewProgressValue.value) newProgressValue.floatValue else progress,
            onValueChange = { newValue ->
                useNewProgressValue.value = true
                newProgressValue.floatValue = newValue
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