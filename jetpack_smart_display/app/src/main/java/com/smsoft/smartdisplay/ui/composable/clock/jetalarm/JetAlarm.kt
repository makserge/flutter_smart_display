package com.smsoft.smartdisplay.ui.composable.clock.jetalarm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
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
import com.smsoft.smartdisplay.utils.getStateFromFlow
import com.smsoft.smartdisplay.ui.screen.clock.ClockViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JetAlarm(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    viewModel: ClockViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int
) {
    val borderRadius = getStateFromFlow(
        flow = viewModel.borderRadiusJA,
        defaultValue = DEFAULT_BORDER_RADIUS_JA
    ) as Float

    val borderThickness = getStateFromFlow(
        flow = viewModel.borderThicknessJA,
        defaultValue = DEFAULT_BORDER_THICKNESS_JA
    ) as Float

    val secondsHandLength = getStateFromFlow(
        flow = viewModel.secondsHandLengthJA,
        defaultValue = DEFAULT_HAND_LEN_SECONDS_JA
    ) as Float

    val minutesHandLength = getStateFromFlow(
        flow = viewModel.minutesHandLengthJA,
        defaultValue = DEFAULT_HAND_LEN_MINUTES_JA
    ) as Float

    val secondsHandWidth = getStateFromFlow(
        flow = viewModel.secondsHandWidthJA,
        defaultValue = DEFAULT_HAND_WIDTH_SECONDS_JA
    ) as Float

    val minutesHandWidth = getStateFromFlow(
        flow = viewModel.minutesHandWidthJA,
        defaultValue = DEFAULT_HAND_WIDTH_MINUTES_JA
    ) as Float

    val hoursHandLength = getStateFromFlow(
        flow = viewModel.hoursHandLengthJA,
        defaultValue = DEFAULT_HAND_LEN_HOURS_JA
    ) as Float

    val hoursHandWidth = getStateFromFlow(
        flow = viewModel.hoursHandWidthJA,
        defaultValue = DEFAULT_HAND_WIDTH_HOURS_JA
    ) as Float

    val showSecondHand = getStateFromFlow(
        flow = viewModel.showSecondHandJA,
        defaultValue = DEFAULT_SHOW_SECOND_HAND_JA
    ) as Boolean

    OnDraw(
        modifier = modifier,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        borderRadius = borderRadius,
        borderThickness = borderThickness,
        showSecondHand = showSecondHand,
        secondsHandLength = secondsHandLength,
        secondsHandWidth = secondsHandWidth,
        minutesHandLength = minutesHandLength,
        minutesHandWidth = minutesHandWidth,
        hoursHandLength = hoursHandLength,
        hoursHandWidth = hoursHandWidth,
        hour = hour,
        minute = minute,
        second = second,
        milliSecond = milliSecond
    )
}

@Composable
fun OnDraw(
    modifier: Modifier,
    primaryColor: Color,
    secondaryColor: Color,
    borderRadius: Float,
    borderThickness: Float,
    showSecondHand: Boolean,
    secondsHandLength: Float,
    secondsHandWidth: Float,
    minutesHandLength: Float,
    minutesHandWidth: Float,
    hoursHandLength: Float,
    hoursHandWidth: Float,
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int
) {
    Box(
        modifier = modifier
    ) {
        DrawStaticUi(
            modifier = modifier,
            borderRadius = borderRadius,
            borderThickness = borderThickness,
            color = primaryColor
        )
        DrawHands(
            modifier = modifier,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            showSecondHand = showSecondHand,
            secondsHandLength = secondsHandLength,
            secondsHandWidth = secondsHandWidth,
            minutesHandLength = minutesHandLength,
            minutesHandWidth = minutesHandWidth,
            hoursHandLength = hoursHandLength,
            hoursHandWidth = hoursHandWidth,
            hour = hour,
            minute = minute,
            second = second,
            milliSecond = milliSecond
        )
    }
}

@Composable
fun DrawStaticUi(
    modifier: Modifier,
    borderRadius: Float,
    borderThickness: Float,
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
            radius = size.getRadius(borderRadius),
            style = Stroke(borderThickness)
        )
    }
}

@Composable
fun DrawHands(
    modifier: Modifier,
    primaryColor: Color,
    secondaryColor: Color,
    showSecondHand: Boolean,
    secondsHandLength: Float,
    secondsHandWidth: Float,
    minutesHandLength: Float,
    minutesHandWidth: Float,
    hoursHandLength: Float,
    hoursHandWidth: Float,
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
        if (showSecondHand) {
            drawHand(
                scope = this,
                centerX = centerX,
                centerY = centerY,
                clockRadius = size.getRadius(minutesHandLength),
                animatedValue = animatedMinute,
                color = primaryColor,
                strokeWidth = minutesHandWidth
            )
            drawHand(
                scope = this,
                centerX = centerX,
                centerY = centerY,
                clockRadius = size.getRadius(hoursHandLength),
                animatedValue = animatedHour,
                color = primaryColor,
                strokeWidth = hoursHandWidth
            )
            drawHand(
                scope = this,
                centerX = centerX,
                centerY = centerY,
                clockRadius = size.getRadius(secondsHandLength),
                animatedValue = animatedSecond,
                color = secondaryColor,
                strokeWidth = secondsHandWidth
            )
        } else {
            drawHand2(
                scope = this,
                centerX = centerX,
                centerY = centerY,
                clockRadius = size.getRadius(hoursHandLength),
                animatedValue = animatedHour,
                color = primaryColor,
                strokeWidth = hoursHandWidth
            )
            drawHand2(
                scope = this,
                centerX = centerX,
                centerY = centerY,
                clockRadius = size.getRadius(minutesHandLength),
                animatedValue = animatedMinute,
                color = primaryColor,
                strokeWidth = minutesHandWidth
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
    scope.drawLine(
        start = Offset(
            x = centerX,
            y = centerY),
        end = Offset(
            x = (centerX + cos(degree) * clockRadius).toFloat(),
            y = (centerY + sin(degree) * clockRadius).toFloat()
        ),
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
    scope.drawLine(
        start = Offset(
            x = (centerX + cos(degree) * -30).toFloat(),
            y = (centerY + sin(degree) * -30).toFloat()
        ),
        end = Offset(
            x = (centerX + cos(degree) * clockRadius).toFloat(),
            y = (centerY + sin(degree) * clockRadius).toFloat()
        ),
        color = color,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

private fun Size.getRadius(
    expo: Float = 1F
) = expo * min(Dp(width / 2), Dp(height / 2)).value

const val DEFAULT_BORDER_RADIUS_JA = 0.9F
const val DEFAULT_BORDER_THICKNESS_JA = 7F
const val DEFAULT_HAND_LEN_SECONDS_JA = 0.7F
const val DEFAULT_HAND_LEN_MINUTES_JA = 0.6F
const val DEFAULT_HAND_LEN_HOURS_JA = 0.45F
const val DEFAULT_HAND_WIDTH_SECONDS_JA = 4F
const val DEFAULT_HAND_WIDTH_MINUTES_JA = 8F
const val DEFAULT_HAND_WIDTH_HOURS_JA = 8F
const val DEFAULT_SHOW_SECOND_HAND_JA = true