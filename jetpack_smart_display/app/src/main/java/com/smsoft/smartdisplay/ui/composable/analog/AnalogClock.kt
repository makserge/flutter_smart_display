package com.smsoft.smartdisplay.ui.composable.analog

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.smsoft.smartdisplay.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogClock(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary,
    hour: Int,
    minute: Int,
    second: Int,
    millisecond: Int
) {
    Box(
        modifier = modifier
    ) {
        StaticUi(
            modifier = modifier,
            color = color
        )
       Hands(
            modifier = modifier,
            color = color,
            hour = hour,
            minute = minute,
            second = second,
            millisecond = millisecond
       )
    }
}

@Composable
fun StaticUi(
    modifier: Modifier,
    color: Color
) {
    val digitalFont = FontFamily(
        Font(R.font.digital, weight = FontWeight.Normal),
    )
    val fontSize = 80.sp

    Image(
        modifier = Modifier
            .fillMaxSize()
            .absolutePadding(top = 9.dp),
        painter = painterResource(R.drawable.clock_background),
        contentDescription = null
    )
    Row (
        modifier = modifier
            .fillMaxSize()
            .padding(25.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier,
            text = "9",
            color = color,
            fontFamily = digitalFont,
            fontSize = fontSize
        )
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Right,
            text = "3",
            color = color,
            fontFamily = digitalFont,
            fontSize = fontSize
        )
    }
    Column (
        modifier = modifier
            .fillMaxSize()
            .padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier,
            text = "12",
            color = color,
            fontFamily = digitalFont,
            fontSize = fontSize
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier,
            text = "6",
            color = color,
            fontFamily = digitalFont,
            fontSize = fontSize
        )
    }
}

@Composable
fun Hands(
    modifier: Modifier,
    color: Color,
    hour: Int,
    minute: Int,
    second: Int,
    millisecond: Int
) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val progression = ((millisecond % 1000) / 1000.0)

        val centerX = (size.width / 2)
        val centerY = (size.height / 2)

        val hourInt = if (hour > 12) hour - 12 else hour

        val animatedSecond = second + progression
        val animatedMinute = minute + animatedSecond / 60
        val animatedHour = (hourInt + (animatedMinute / 60)) * 5F
        minuteHand(centerX, centerY, size.getRadius(0.7F), animatedMinute, color)
        hourHand(centerX, centerY, size.getRadius(0.4F), animatedHour, color)
    }
}

fun DrawScope.minuteHand(
    centerX: Float,
    centerY: Float,
    clockRadius: Float,
    animatedMinute: Double,
    color: Color
) {
    val degree = animatedMinute * oneMinuteRadians - pieByTwo
    val x = centerX + cos(degree) * clockRadius
    val y = centerY + sin(degree) * clockRadius
    drawLine(
        start = Offset(centerX, centerY),
        end = Offset(x.toFloat(), y.toFloat()),
        color = color,
        strokeWidth = 8F,
        cap = StrokeCap.Round
    )
}

fun DrawScope.hourHand(
    centerX: Float,
    centerY: Float,
    clockRadius: Float,
    animatedHour: Double,
    color: Color
) {
    val degree = animatedHour * oneMinuteRadians - pieByTwo
    val x = centerX + cos(degree) * clockRadius
    val y = centerY + sin(degree) * clockRadius
    drawLine(
        start = Offset(centerX, centerY),
        end = Offset(x.toFloat(), y.toFloat()),
        color = color,
        strokeWidth = 16F,
        cap = StrokeCap.Round
    )
}

private fun Size.getRadius(expo: Float = 1F) = expo * min(Dp(width / 2), Dp(height / 2)).value
private const val oneMinuteRadians = Math.PI / 30
private const val pieByTwo = Math.PI / 2
