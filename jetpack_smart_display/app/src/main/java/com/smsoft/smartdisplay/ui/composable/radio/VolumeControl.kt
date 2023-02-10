package com.smsoft.smartdisplay.ui.composable.radio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.unit.dp

@Composable
fun VolumeControl(
    modifier: Modifier,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    val state = rememberComposeVerticalSliderState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(50.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.End,
    ) {
        ComposeVerticalSlider(
            width = 40.dp,
            radius = CornerRadius(12F, 12F),
            trackColor = MaterialTheme.colors.primary,
            progressTrackColor = MaterialTheme.colors.secondary,
            state = state,
            enabled = state.isEnabled.value,
            progressValue = value,
            onProgressChanged = {
                onValueChange(it)
            },
            onStopTrackingTouch = {
                onValueChange(it)
            }
        )
    }
}