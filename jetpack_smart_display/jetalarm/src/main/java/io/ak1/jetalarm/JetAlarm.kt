package io.ak1.jetalarm

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun JetAlarm(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    color: Color = MaterialTheme.colors.primary,
    hour: Int,
    minute: Int,
    second: Int,
    millisecond: Int
) {
    ClockView(
        modifier = modifier,
        color = color,
        clockType = ClockType.CLOCK_ONE,
        hour = hour,
        minute = minute,
        second = second,
        millisecond = millisecond
    )
}
