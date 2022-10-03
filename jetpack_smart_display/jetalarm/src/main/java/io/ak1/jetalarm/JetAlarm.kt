package io.ak1.jetalarm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

@Composable
fun JetAlarm(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    color: Color = MaterialTheme.colors.primary,
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int
) {
    OnDraw(
        modifier = modifier,
        color = color,
        clockType = ClockType.WITHOUT_SECONDS,
        hour = hour,
        minute = minute,
        second = second,
        milliSecond = milliSecond
    )
}

@Composable
fun OnDraw(
    modifier: Modifier,
    color: Color,
    clockType: ClockType,
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int
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
            clockType = clockType,
            hour = hour,
            minute = minute,
            second = second,
            milliSecond = milliSecond
        )
    }
}

@Composable
fun StaticUi(
    modifier: Modifier,
    color: Color
) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        drawCircle(
            color = color,
            radius = 15F
        )
        drawCircle(
            color = color,
            radius = size.getRadius(0.8F),
            style = Stroke(7F)
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
    milliSecond: Int
) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val progression = ((milliSecond % 1000) / 1000.0)

        val centerX = (size.width / 2)
        val centerY = (size.height / 2)

        val hourInt = if (hour > 12) hour - 12 else hour

        val animatedSecond = second + progression
        val animatedMinute = minute + animatedSecond / 60
        val animatedHour = (hourInt + (animatedMinute / 60)) * 5F
        if (clockType == ClockType.WITH_SECONDS) {
            drawHand(
                scope = this,
                centerX = centerX,
                centerY = centerY,
                clockRadius = size.getRadius(0.6F),
                animatedValue = animatedMinute,
                color = color,
                strokeWidth = 8F
            )
            drawHand(
                scope = this,
                centerX = centerX,
                centerY = centerY,
                clockRadius = size.getRadius(0.45F),
                animatedValue = animatedHour,
                color = color,
                strokeWidth = 8F
            )
            drawHand(
                scope = this,
                centerX = centerX,
                centerY = centerY,
                clockRadius = size.getRadius(0.7F),
                animatedValue = animatedSecond,
                color = color,
                strokeWidth = 4F
            )
        } else {
            drawHand2(
                scope = this,
                centerX = centerX,
                centerY = centerY,
                clockRadius = size.getRadius(0.45F),
                animatedValue =animatedHour,
                color = color,
                strokeWidth = 8F
            )
            drawHand2(
                scope = this,
                centerX = centerX,
                centerY = centerY,
                clockRadius = size.getRadius(0.6F),
                animatedValue = animatedMinute,
                color = color,
                strokeWidth = 8F
            )
        }
    }
}

private fun drawHand(
    scope: DrawScope,
    centerX: Float,
    centerY: Float,
    clockRadius: Float,
    animatedValue: Double,
    color: Color,
    strokeWidth: Float
) {
    val degree = animatedValue * (Math.PI / 30) - Math.PI / 2
    val x = (centerX + cos(degree) * clockRadius).toFloat()
    val y = (centerY + sin(degree) * clockRadius).toFloat()
    scope.drawLine(
        start = Offset(centerX, centerY),
        end = Offset(x, y),
        color = color,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

private fun drawHand2(
    scope: DrawScope,
    centerX: Float,
    centerY: Float,
    clockRadius: Float,
    animatedValue: Double,
    color: Color,
    strokeWidth: Float
) {
    val degree = animatedValue * (Math.PI / 30) - Math.PI / 2
    val x = (centerX + cos(degree) * clockRadius).toFloat()
    val y = (centerY + sin(degree) * clockRadius).toFloat()
    val minusX = (centerX + cos(degree) * -30).toFloat()
    val minusY = (centerY + sin(degree) * -30).toFloat()
    scope.drawLine(
        start = Offset(minusX, minusY),
        end = Offset(x, y),
        color = color,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

private fun Size.getRadius(expo: Float = 1F) = expo * min(Dp(width / 2), Dp(height / 2)).value

enum class ClockType {
    WITH_SECONDS, WITHOUT_SECONDS
}