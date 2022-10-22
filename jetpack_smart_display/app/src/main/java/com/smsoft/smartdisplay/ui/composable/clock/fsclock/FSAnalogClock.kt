package com.smsoft.smartdisplay.ui.composable.clock.fsclock

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.smsoft.smartdisplay.R

@Composable
fun FSAnalogClock(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    primaryColor: Color,
    secondaryColor: Color,
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int
) {
    val primaryColorFilter = ColorFilter.tint(
        color = primaryColor
    )
    val secondaryColorFilter = ColorFilter.tint(
        color = secondaryColor
    )

    OnDraw(
        modifier = modifier,
        primaryColor = primaryColorFilter,
        secondaryColor = secondaryColorFilter,
        hour = hour,
        minute = minute,
        second = second,
        milliSecond = milliSecond
    )
}

@Composable
fun OnDraw(
    modifier: Modifier,
    primaryColor: ColorFilter,
    secondaryColor: ColorFilter,
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int
) {
    val hourAngle = (hour + minute.toFloat() / 60) * 360 / 12
    val minuteAngle = (minute + second.toFloat() / 60) * 360 / 60
    val secondAngle = (second + milliSecond.toFloat() / 1000) * 360 / 60

    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Image(
                modifier = modifier
                    .fillMaxSize(),
                colorFilter = primaryColor,
                painter = painterResource(R.drawable.ic_background_fs),
                contentDescription = null
            )
            Image(
                modifier = modifier
                    .fillMaxSize()
                    .rotate(hourAngle),
                colorFilter = primaryColor,
                painter = painterResource(R.drawable.ic_hour_fs),
                contentDescription = null,
            )
            Image(
                modifier = modifier
                    .fillMaxSize()
                    .rotate(minuteAngle),
                colorFilter = primaryColor,
                painter = painterResource(R.drawable.ic_minute_fs),
                contentDescription = null
            )
            Image(
                modifier = modifier
                    .fillMaxSize()
                    .rotate(secondAngle),
                colorFilter = secondaryColor,
                painter = painterResource(R.drawable.ic_second_fs),
                contentDescription = null
            )
        }
    }
}