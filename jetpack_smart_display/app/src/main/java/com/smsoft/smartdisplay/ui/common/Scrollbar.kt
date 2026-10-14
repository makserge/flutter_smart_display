package com.smsoft.smartdisplay.ui.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.unit.dp

/*
 * A scroll bar on the right edge of a scrolling column or list, shown whenever the content is
 * longer than the screen: on a wall panel there is no other hint that more rows follow below. It
 * only shows the position; scrolling is done by swiping the content.
 */

/** A scroll bar for a column scrolled with [state] (Modifier.verticalScroll). Put it before verticalScroll. */
@Composable
fun Modifier.verticalScrollbar(state: ScrollState): Modifier {
    val color = scrollbarColor()
    return drawWithContent {
        drawContent()
        val total = size.height + state.maxValue
        if (state.maxValue > 0) {
            drawScrollbar(color, size.height / total, state.value / total)
        }
    }
}

/** A scroll bar for a LazyColumn scrolled with [state]; its rows may differ in height. */
@Composable
fun Modifier.verticalScrollbar(state: LazyListState): Modifier {
    val color = scrollbarColor()
    return drawWithContent {
        drawContent()
        val info = state.layoutInfo
        val visible = info.visibleItemsInfo
        val count = info.totalItemsCount
        if (visible.isEmpty() || count == 0) {
            return@drawWithContent
        }
        // Estimated from the visible rows: the share of the rows that are visible, and the
        // first visible row with the part of it that is scrolled away
        val first = visible.first()
        val averageHeight = visible.sumOf { it.size }.toFloat() / visible.size
        val estimatedTotal = averageHeight * count + info.beforeContentPadding + info.afterContentPadding
        val viewport = (info.viewportEndOffset - info.viewportStartOffset).toFloat()
        if (estimatedTotal <= viewport) {
            return@drawWithContent
        }
        val scrolled = first.index * averageHeight - first.offset
        drawScrollbar(color, viewport / estimatedTotal, scrolled / estimatedTotal)
    }
}

@Composable
private fun scrollbarColor(): Color = MaterialTheme.colors.onBackground.copy(alpha = 0.45F)

/** The bar: [shown] is the visible share of the content, [start] the share scrolled away above. */
private fun ContentDrawScope.drawScrollbar(color: Color, shown: Float, start: Float) {
    val width = SCROLLBAR_WIDTH.toPx()
    val minHeight = SCROLLBAR_MIN_HEIGHT.toPx()
    val height = (size.height * shown.coerceIn(0F, 1F)).coerceAtLeast(minHeight).coerceAtMost(size.height)
    val top = ((size.height - height) * (start / (1F - shown).coerceAtLeast(0.0001F)).coerceIn(0F, 1F))
    drawRoundRect(
        color = color,
        topLeft = Offset(size.width - width - SCROLLBAR_MARGIN.toPx(), top),
        size = Size(width, height),
        cornerRadius = CornerRadius(width / 2, width / 2)
    )
}

private val SCROLLBAR_WIDTH = 4.dp
private val SCROLLBAR_MIN_HEIGHT = 24.dp
private val SCROLLBAR_MARGIN = 2.dp
