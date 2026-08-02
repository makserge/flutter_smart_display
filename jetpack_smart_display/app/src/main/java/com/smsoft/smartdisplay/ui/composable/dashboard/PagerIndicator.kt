package com.smsoft.smartdisplay.ui.composable.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Small dot-style page indicator, replacing accompanist-pager's HorizontalPagerIndicator
 * (accompanist removed the whole Pager module in v0.37.0 - it's been superseded by
 * androidx.compose.foundation.pager, which this app already uses for the pager itself).
 */
@Composable
fun HorizontalPagerIndicator(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    pageCount: Int,
    activeColor: Color = MaterialTheme.colors.primary,
    inactiveColor: Color = activeColor.copy(alpha = 0.3f),
    indicatorSize: Dp = 8.dp,
    indicatorSpacing: Dp = 8.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(indicatorSpacing)
    ) {
        repeat(pageCount) { page ->
            Box(
                modifier = Modifier
                    .size(indicatorSize)
                    .clip(CircleShape)
                    .background(
                        color = if (pagerState.currentPage == page) activeColor else inactiveColor
                    )
            )
        }
    }
}
