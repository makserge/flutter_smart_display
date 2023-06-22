package com.smsoft.smartdisplay.ui.composable.asr

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
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

@Composable
fun SpeechRecognitionAlert(
    modifier: Modifier,
    text: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        text = {
            Column(
                modifier = Modifier
                    .size(300.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .padding(vertical = 25.dp),
                    text = text,
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary
                )
                Image(
                    painter = painterResource(R.drawable.ic_mic_48),
                    modifier = Modifier
                        .size(48.dp),
                    colorFilter = ColorFilter.tint(
                        color = Color.White
                    ),
                    contentDescription = null
                )
            }
        },
        buttons = {}
    )
}