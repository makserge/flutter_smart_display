package com.smsoft.smartdisplay.ui.composable.message

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.window.Dialog
import com.smsoft.smartdisplay.R

@Composable
fun MessageAlert(
    modifier: Modifier,
    text: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RectangleShape,
            modifier = Modifier
                .clickable {
                    onDismiss()
                }
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 50.dp,
                    vertical = 50.dp
                )
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
                        color = Color.White
                    ),
                    contentDescription = null
                )
            }
        }
    }
}