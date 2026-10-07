package com.smsoft.smartdisplay.ui.common.prefs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Group title: primary colour, semi-bold, 85% of the current text size, 16dp start inset. */
@Composable
fun GroupHeader(
    title: String,
    color: Color = MaterialTheme.colors.primary,
) {
    Box(
        modifier = Modifier
            .padding(start = 16.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = title,
            color = color,
            fontSize = LocalTextStyle.current.fontSize * 0.85f,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
