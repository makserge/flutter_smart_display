package com.firebirdberlin.nightdream

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.xenione.digit.TabDigit

@Composable
fun NightdreamFlipDigitalClock(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    color: Color = MaterialTheme.colors.primary,
    hour: Int,
    minute: Int
) {
    val digitColor = color.toArgb()

    val highHour = hour / 10
    val highMinute = minute / 10
    val lowMinute = minute - highMinute * 10

    var currentHourHigh by remember { mutableStateOf(highHour) }
    var currentHourLow by remember { mutableStateOf(hour) }
    var currentMinuteHigh by remember { mutableStateOf(highMinute) }
    var currentMinuteLow by remember { mutableStateOf(lowMinute) }

    Row (
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AndroidView(
            modifier = Modifier,
            factory = { context ->
                TabDigit(context).apply{
                    chars = HOURS
                    textSize = TEXT_SIZE
                    textColor = digitColor
                    setChar(highHour)
                }
            },
            update = { view ->
                if (currentHourHigh != highHour) {
                    view.start()
                }
                currentHourHigh = highHour
            }
        )
        AndroidView(
            modifier = Modifier,
            factory = { context ->
                TabDigit(context).apply{
                    chars = LOWHOURS24
                    textSize = TEXT_SIZE
                    textColor = digitColor
                    setChar(hour)
                }
            },
            update = { view ->
                if (currentHourLow != hour) {
                    view.start()
                }
                currentHourLow = hour
            }
        )
        AndroidView(
            modifier = Modifier,
            factory = { context ->
                TabDigit(context).apply{
                    chars = SEXAGISIMAL
                    textSize = TEXT_SIZE
                    textColor = digitColor
                    setChar(highMinute)
                }
            },
            update = { view ->
                if (currentMinuteHigh != highMinute) {
                    view.start()
                }
                currentMinuteHigh = highMinute
            }
        )
        AndroidView(
            modifier = Modifier,
            factory = { context ->
                TabDigit(context).apply{
                    chars = LOWHOURS24
                    textSize = TEXT_SIZE
                    textColor = digitColor
                    setChar(lowMinute)
                }
            },
            update = { view ->
                if (currentMinuteLow != lowMinute) {
                    view.start()
                }
                currentMinuteLow = lowMinute
            }
        )
    }
}

private val HOURS = charArrayOf('0', '1', '2')
private val LOWHOURS24 = charArrayOf(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3'
)
private val SEXAGISIMAL = charArrayOf('0', '1', '2', '3', '4', '5')
private const val TEXT_SIZE = 430