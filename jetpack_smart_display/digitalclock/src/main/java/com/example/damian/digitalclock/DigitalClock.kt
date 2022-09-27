package com.example.damian.digitalclock

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@Composable
fun DigitalClock(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    color: Color = MaterialTheme.colors.primary,
    year: Int,
    month: Int,
    day: Int,
    dayOfWeek: Int,
    hour: Int,
    minute: Int,
    second: Int
) {
    val digitalFont = FontFamily(
        Font(R.font.dseg14classic, weight = FontWeight.Normal),
        //Font(R.font.digital, weight = FontWeight.Normal),
    )

    val time = addLeadingZeros(
        hour = hour,
        minute = minute,
        second = second,
        isShowSeconds = false)
    val date = getDayOfWeek(dayOfWeek) + " " + day.toString() + " . " + (month + 1) + " . " + year.toString()

    Column (
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier,
            text = time,
            color = color,
            fontFamily = digitalFont,
            //fontSize = 140.sp
            fontSize = 200.sp
        )
        Spacer(
            modifier = Modifier
                .height(70.dp)
        )
        Text(
            modifier = Modifier,
            text = date,
            color = color,
            fontFamily = digitalFont,
            fontSize = 48.sp
        )
    }
}

private fun addLeadingZeros(hour: Int, minute: Int, second: Int, isShowSeconds: Boolean): String {
    val hoursStr = hour.toString()
    val minutesStr = if (minute <= 9) "0$minute" else minute.toString()
    val secondsStr = if (second <= 9) "0$second" else second.toString()
    return if (isShowSeconds) "$hoursStr:$minutesStr:$secondsStr" else "$hoursStr:$minutesStr"
}

private fun getDayOfWeek(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        Calendar.MONDAY -> "MONDAY"
        Calendar.TUESDAY -> "TUESDAY"
        Calendar.WEDNESDAY -> "WEDNESDAY"
        Calendar.THURSDAY -> "THURSDAY"
        Calendar.FRIDAY -> "FRIDAY"
        Calendar.SATURDAY -> "SATURDAY"
        Calendar.SUNDAY -> "SUNDAY"
        else -> "MONDAY"
    }
}