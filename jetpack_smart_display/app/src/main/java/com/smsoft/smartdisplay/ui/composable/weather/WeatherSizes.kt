package com.smsoft.smartdisplay.ui.composable.weather

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The sizes of the weather page. [compact] is the layout of a small panel, with the icon beside the
 * text and the forecast days below; otherwise the current weather and the two days are three
 * columns. Each layout is designed for one page (the 4 inch and the 8 inch panel) and scaled to the
 * actual page by its width and height, whichever is tighter, so it fills the page on any screen.
 * Material's largest style (h1, 96 sp) is still small on a wall display seen from across a room,
 * hence the explicit sizes.
 */
class WeatherSizes(
    val compact: Boolean,
    val pagePadding: Dp,
    val currentIcon: Dp,
    val temperature: TextStyle,
    val detail: TextStyle,
    val day: TextStyle,
    val forecastIcon: Dp,
    val line: TextStyle
)

@Composable
fun weatherSizes(compact: Boolean, pageWidth: Dp, pageHeight: Dp): WeatherSizes {
    val typography = MaterialTheme.typography
    return if (compact) {
        // Designed for the 4 inch panel's page; scaled to the page, so it fills it
        val k = scaleFor(pageWidth, pageHeight, COMPACT_PAGE_WIDTH, COMPACT_PAGE_HEIGHT)
        WeatherSizes(
            compact = true,
            pagePadding = 8.dp,
            currentIcon = 110.dp * k,
            temperature = typography.h3.copy(fontSize = 64.sp * k),
            detail = typography.subtitle1.copy(fontSize = 19.sp * k),
            day = typography.h6.copy(fontSize = 22.sp * k),
            forecastIcon = 64.dp * k,
            line = typography.body2.copy(fontSize = 16.sp * k)
        )
    } else {
        // Designed for the 8 inch panel's page; the three columns (now, and the two days) fill it
        val k = scaleFor(pageWidth, pageHeight, REGULAR_PAGE_WIDTH, REGULAR_PAGE_HEIGHT)
        WeatherSizes(
            compact = false,
            pagePadding = 16.dp,
            // The weather icons have wide transparent margins, so they get a large box
            currentIcon = 240.dp * k,
            temperature = typography.h1.copy(fontSize = 170.sp * k),
            detail = typography.h5.copy(fontSize = 40.sp * k),
            day = typography.h4.copy(fontSize = 54.sp * k),
            forecastIcon = 230.dp * k,
            line = typography.h5.copy(fontSize = 38.sp * k)
        )
    }
}

/** How much larger or smaller the page is than the one the sizes were designed for. */
private fun scaleFor(width: Dp, height: Dp, designWidth: Dp, designHeight: Dp): Float =
    minOf(width / designWidth, height / designHeight).coerceIn(0.5F, 3F)

// The pages the sizes are designed for: the 4 inch panel (360 dp wide after the small-screen
// scaling) and the 8 inch one (1280 by 800 dp), each without the pager indicator
private val COMPACT_PAGE_WIDTH = 360.dp
// The compact layout is about 345 dp tall at these sizes
private val COMPACT_PAGE_HEIGHT = 350.dp
private val REGULAR_PAGE_WIDTH = 1280.dp
private val REGULAR_PAGE_HEIGHT = 650.dp
