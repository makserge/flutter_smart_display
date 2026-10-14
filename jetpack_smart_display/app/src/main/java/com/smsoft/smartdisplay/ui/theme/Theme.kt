package com.smsoft.smartdisplay.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
        primary = white,
        primaryVariant = purple700,
        secondary = teal200,
        background = black
)

// The app is always dark: a wall panel lights up the room, and the light theme by day was removed.
@Composable
fun SmartDisplayTheme(
    content: @Composable() () -> Unit
) {
    MaterialTheme(
        colors = DarkColorPalette,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}