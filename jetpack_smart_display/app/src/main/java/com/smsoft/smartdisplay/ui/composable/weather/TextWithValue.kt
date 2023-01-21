package com.smsoft.smartdisplay.ui.composable.weather

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun TextWithValue(
    modifier: Modifier,
    title: String,
    value: String,
    style: TextStyle = MaterialTheme.typography.subtitle2
) {
    Row(
        modifier = Modifier
    ) {
        Text(
            modifier = Modifier,
            text = title,
            style = style,
            color = MaterialTheme.colors.secondary
        )
        Text(
            modifier = Modifier
                .padding(
                    start = 3.dp
                ),
            text = value,
            style = style,
            color = MaterialTheme.colors.primary
        )
    }
}