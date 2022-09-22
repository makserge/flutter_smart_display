package com.github.chenglei1986.clock

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ClockView2(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    color: ColorFilter = ColorFilter.tint(color = MaterialTheme.colors.primary),
    hour: Int,
    minute: Int,
    second: Int,
    millisecond: Int
) {
    AndroidView(
        modifier = modifier
            .fillMaxSize(),
        factory = { context ->
            Clock(context).apply {
                //setTimeZone("GMT")
            }
        },
        update = { view ->
            view.setTime(
                hour = hour,
                minute = minute,
                second = second,
                milliSecond = millisecond
            )
        }
    )
}
