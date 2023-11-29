package com.smsoft.smartdisplay.ui.composable.sensors

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.utils.getIcon

@Composable
fun TextWithIcon(
    modifier: Modifier,
    context: Context,
    text: String,
    icon: String,
) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon.isNotEmpty()) {
            Icon(
                modifier = Modifier
                    .padding(
                        end = 3.dp
                    )
                    .size(48.dp),
                painter = painterResource(
                    id = getIcon(
                        context = context,
                        item = icon
                    )
                ),
                contentDescription = null
            )
        }
        Text(
            modifier = Modifier,
            text = text,
            style = MaterialTheme.typography.h5,
            color = MaterialTheme.colors.secondary,
            textAlign = TextAlign.Center
        )
    }
}