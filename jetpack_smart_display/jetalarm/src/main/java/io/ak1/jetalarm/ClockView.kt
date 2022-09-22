package io.ak1.jetalarm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.min
import kotlin.math.cos
import kotlin.math.sin

/**
 * Created by akshay on 06/10/21
 * https://ak1.io
 */
@Composable
fun ClockView(
    modifier: Modifier,
    color: Color,
    clockType: ClockType,
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
            color,
            clockType,
            hour,
            minute,
            second,
            millisecond
        )
    }
}

@Composable
fun Hands(
    modifier: Modifier,
    color: Color,
    clockType: ClockType,
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
        if (clockType == ClockType.CLOCK_ONE) {
            minuteHand(centerX, centerY, size.getRadius(0.6F), animatedMinute, color)
            hourHand(centerX, centerY, size.getRadius(0.45F), animatedHour, color)
            secondHand(centerX, centerY, size.getRadius(0.7F), animatedSecond, color)
        } else {
            hourHand2(centerX, centerY, size.getRadius(0.45F), animatedHour, color)
            minuteHand2(centerX, centerY, size.getRadius(0.6F), animatedMinute, color)
        }
    }
}

fun DrawScope.secondHand(
    centerX: Float,
    centerY: Float,
    clockRadius: Float,
    animatedSecond: Double,
    color: Color
) {
    val degree = animatedSecond * oneMinuteRadians - pieByTwo
    val x = centerX + cos(degree) * clockRadius
    val y = centerY + sin(degree) * clockRadius
    drawLine(
        start = Offset(centerX, centerY),
        end = Offset(x.toFloat(), y.toFloat()),
        color = color,
        strokeWidth = 4F,
        cap = StrokeCap.Round
    )
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
        strokeWidth = 8F,
        cap = StrokeCap.Round
    )
}

@Composable
fun StaticUi(
    modifier: Modifier,
    color: Color
) {
    val color2 = MaterialTheme.colors.background
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = color,
            radius = 15F
        )
        drawCircle(
            color = color2,
            radius = 7F
        )
        drawCircle(
            color = color,
            radius = size.getRadius(0.8F),
            style = Stroke(7F)
        )
    }
}

// second clock UI methods
fun DrawScope.minuteHand2(
    centerX: Float,
    centerY: Float,
    clockRadius: Float,
    animatedMinute: Double,
    color: Color
) {
    val degree = animatedMinute * oneMinuteRadians - pieByTwo
    val x = centerX + cos(degree) * clockRadius
    val y = centerY + sin(degree) * clockRadius
    val minusX = centerX + cos(degree) * -30
    val minusY = centerY + sin(degree) * -30
    drawLine(
        start = Offset(minusX.toFloat(), minusY.toFloat()),
        end = Offset(x.toFloat(), y.toFloat()),
        color = color,
        strokeWidth = 8F,
        cap = StrokeCap.Round
    )
}

fun DrawScope.hourHand2(
    centerX: Float,
    centerY: Float,
    clockRadius: Float,
    animatedHour: Double,
    color: Color
) {
    val degree = animatedHour * oneMinuteRadians - pieByTwo
    val x = centerX + cos(degree) * clockRadius
    val y = centerY + sin(degree) * clockRadius
    val minusX = centerX + cos(degree) * -30
    val minusY = centerY + sin(degree) * -30
    drawLine(
        start = Offset(minusX.toFloat(), minusY.toFloat()),
        end = Offset(x.toFloat(), y.toFloat()),
        color = color,
        strokeWidth = 8F,
        cap = StrokeCap.Round
    )
}

private fun Size.getRadius(expo: Float = 1F) = expo * min(Dp(width / 2), Dp(height / 2)).value

private const val oneMinuteRadians = Math.PI / 30
private const val pieByTwo = Math.PI / 2

enum class ClockType {
    CLOCK_ONE, CLOCK_TWO
}