package com.smsoft.smartdisplay.ui.composable.alarms

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.ui.common.AlertScreen
import com.smsoft.smartdisplay.utils.formatTime

@Composable
fun AlarmAlert(
    modifier: Modifier,
    time: Int,
    onDismiss: () -> Unit
) {
    // A full screen, tapped anywhere to stop (was a pop-up window)
    AlertScreen(
        onDismissRequest = onDismiss,
        onTap = onDismiss
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(
                    top = 20.dp,
                    bottom = 20.dp
                ),
                text = formatTime(time),
                style = MaterialTheme.typography.h2,
                color = MaterialTheme.colors.primary
            )
            Image(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(20.dp)
                    .size(48.dp),
                painter = painterResource(R.drawable.ic_alarm_off_48),
                colorFilter = ColorFilter.tint(
                    color = MaterialTheme.colors.onSurface
                ),
                contentDescription = null
            )
        }
    }
}