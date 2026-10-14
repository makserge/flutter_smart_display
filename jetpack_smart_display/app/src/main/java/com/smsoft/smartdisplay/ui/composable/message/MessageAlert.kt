package com.smsoft.smartdisplay.ui.composable.message

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

@Composable
fun MessageAlert(
    modifier: Modifier,
    text: String,
    onDismiss: () -> Unit
) {
    // A full screen, tapped anywhere to close (was a pop-up window)
    AlertScreen(
        onDismissRequest = onDismiss,
        onTap = onDismiss
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(
                    top = 20.dp,
                    bottom = 20.dp
                ),
                text = text,
                style = MaterialTheme.typography.h4,
                color = MaterialTheme.colors.primary
            )
            Image(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(20.dp)
                    .size(48.dp),
                painter = painterResource(R.drawable.ic_cancel_48),
                colorFilter = ColorFilter.tint(
                    color = MaterialTheme.colors.onSurface
                ),
                contentDescription = null
            )
        }
    }
}