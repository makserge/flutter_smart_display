package com.firebirdberlin.nightdream

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun NightdreamAnalogClock(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    color: ColorFilter = ColorFilter.tint(color = MaterialTheme.colors.primary),
    hour: Int,
    minute: Int,
    second: Int
) {
    AndroidView(
        modifier = modifier
            .widthIn(max = 500.dp)
            .heightIn(max = 500.dp),
        factory = { context ->
            CustomAnalogClock(context).apply {
                setStyle(
                    style = AnalogClockConfig.Style.DEFAULT,
                    allow_second_hand = true
                )
               // setPrimaryColor()
               //setSecondaryColor()
            }
        },
        update = { view ->
            view.setTime(hour, minute, second)
        }
    )
}
