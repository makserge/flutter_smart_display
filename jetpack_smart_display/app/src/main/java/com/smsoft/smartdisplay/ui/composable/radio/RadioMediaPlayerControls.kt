package com.smsoft.smartdisplay.ui.composable.radio

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.ui.screen.radio.UIEvent

@Composable
fun RadioMediaPlayerControls(
    modifier: Modifier = Modifier,
    playResourceProvider: () -> Int,
    onUiEvent: (UIEvent) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(35.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.skip_previous_48),
            contentDescription = stringResource(com.smsoft.smartdisplay.R.string.previous_button),
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = {
                    onUiEvent(UIEvent.Backward)
                })
                .padding(12.dp)
                .size(48.dp)
        )
        Image(
            painter = painterResource(id = playResourceProvider()),
            contentDescription = stringResource(R.string.play_pause_button),
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = {
                    onUiEvent(UIEvent.PlayPause)
                })
                .padding(8.dp)
                .size(80.dp)
        )
        Icon(
            painter = painterResource(R.drawable.skip_next_48),
            contentDescription = stringResource(com.smsoft.smartdisplay.R.string.next_button),
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = {
                    onUiEvent(UIEvent.Forward)
                })
                .padding(12.dp)
                .size(48.dp)
        )
    }
}