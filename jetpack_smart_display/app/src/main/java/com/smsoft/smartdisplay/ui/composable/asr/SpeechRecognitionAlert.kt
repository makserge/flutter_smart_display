package com.smsoft.smartdisplay.ui.composable.asr

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

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
                    .size(250.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 25.dp),
                    text = text
                )
                Image(
                    modifier = Modifier,
                    painter = painterResource(android.R.drawable.ic_btn_speak_now),
                    contentDescription = null
                )
            }
        },
        buttons = {}
    )
}