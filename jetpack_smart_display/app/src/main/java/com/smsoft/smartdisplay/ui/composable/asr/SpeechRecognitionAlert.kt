package com.smsoft.smartdisplay.ui.composable.asr

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.ui.common.AlertScreen

@Composable
fun SpeechRecognitionAlert(
    modifier: Modifier,
    text: String,
    onDismiss: () -> Unit
) {
    // The card over the dimmed page, in the app's own window: a pop-up window brought back the
    // system bars on Android 8 at every wake word. A tap beside the card closes it, as before.
    AlertScreen(
        onDismissRequest = onDismiss,
        onTap = onDismiss,
        opaque = false
    ) {
        Card(
            shape = RectangleShape,
            modifier = Modifier
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 48.dp,
                    vertical = 32.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (text.isNotEmpty()) {
                    Text(
                        modifier = Modifier.padding(
                            top = 20.dp,
                            bottom = 20.dp
                        ),
                        text = text,
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.primary
                    )
                }
                Image(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(20.dp)
                        .size(48.dp),
                    painter = painterResource(R.drawable.ic_mic_48),
                    colorFilter = ColorFilter.tint(
                        color = MaterialTheme.colors.onSurface
                    ),
                    contentDescription = null
                )
            }
        }
    }
}