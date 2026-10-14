package com.smsoft.smartdisplay.ui.composable.weather

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun TextWithValue(
    modifier: Modifier,
    title: String,
    value: String,
    style: TextStyle = MaterialTheme.typography.subtitle2
) {
    // The gap grows with the text, so the large sizes of a wide screen keep it visible
    val gap = with(LocalDensity.current) {
        if (style.fontSize.isSp) (style.fontSize * GAP_PER_FONT_SIZE).toDp() else 3.dp
    }
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
                    start = gap
                ),
            text = value,
            style = style,
            color = MaterialTheme.colors.primary
        )
    }
}

private const val GAP_PER_FONT_SIZE = 0.25F