package com.smsoft.smartdisplay.ui.composable.clock.jetalarm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.smsoft.smartdisplay.utils.getStateFromFlow
import com.smsoft.smartdisplay.ui.screen.clock.ClockViewModel
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun JetAlarm(
    modifier: Modifier = Modifier,
    viewModel: ClockViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int
) {
    // This function runs on every 100 ms tick, so it only stores the time. The face below
    // reads it in the draw phase, so a tick redraws the canvas without recomposing it.
    val millisOfDay = rememberUpdatedState(
        ((hour * 60 + minute) * 60 + second) * 1000 + milliSecond
    )

    OnDraw(
        modifier = modifier,
        viewModel = viewModel,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        millisOfDay = { millisOfDay.value }
    )
}

@Composable
private fun OnDraw(
    modifier: Modifier,
    viewModel: ClockViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    millisOfDay: () -> Int
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

    // Without a second hand the face only changes once a second; reading this value instead of
    // the raw time avoids nine identical redraws per second.
    val wholeSecondMillis = remember(millisOfDay) {
        derivedStateOf { millisOfDay() / 1000 * 1000 }
    }

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val time = if (showSecondHand) millisOfDay() else wholeSecondMillis.value

        // Every size is relative to the dial, so the face looks the same at any density and
        // resolution. The settings are pixels on a dial of REFERENCE_RADIUS_JA.
        val radius = size.minDimension / 2
        val unit = radius / REFERENCE_RADIUS_JA

        // Keep the whole ring stroke on the page, also with a border radius of 1.
        val ringWidth = borderThickness * unit
        drawCircle(
            color = primaryColor,
            radius = min(radius * borderRadius, radius - ringWidth / 2),
            style = Stroke(ringWidth)
        )

        val animatedSecond = (time % 60_000) / 1000.0
        val animatedMinute = (time % 3_600_000) / 60_000.0
        val animatedHour = (time % 43_200_000) / 720_000.0
        if (showSecondHand) {
            drawHand(
                radius = radius * minutesHandLength,
                tail = 0F,
                animatedValue = animatedMinute,
                color = primaryColor,
                strokeWidth = minutesHandWidth * unit
            )
            drawHand(
                radius = radius * hoursHandLength,
                tail = 0F,
                animatedValue = animatedHour,
                color = primaryColor,
                strokeWidth = hoursHandWidth * unit
            )
            drawHand(
                radius = radius * secondsHandLength,
                tail = 0F,
                animatedValue = animatedSecond,
                color = secondaryColor,
                strokeWidth = secondsHandWidth * unit
            )
        } else {
            drawHand(
                radius = radius * hoursHandLength,
                tail = TAIL_LENGTH * unit,
                animatedValue = animatedHour,
                color = primaryColor,
                strokeWidth = hoursHandWidth * unit
            )
            drawHand(
                radius = radius * minutesHandLength,
                tail = TAIL_LENGTH * unit,
                animatedValue = animatedMinute,
                color = primaryColor,
                strokeWidth = minutesHandWidth * unit
            )
        }

        // The hub goes last so it covers the roots of all hands.
        drawCircle(
            color = primaryColor,
            radius = HUB_RADIUS * unit
        )
    }
}

// animatedValue is in minute-dial units: 60 = one full turn.
private fun DrawScope.drawHand(
    radius: Float,
    tail: Float,
    animatedValue: Double,
    color: Color,
    strokeWidth: Float
) {
    val degree = animatedValue * (Math.PI / 30) - Math.PI / 2
    val dx = cos(degree).toFloat()
    val dy = sin(degree).toFloat()
    drawLine(
        start = Offset(
            x = center.x - dx * tail,
            y = center.y - dy * tail
        ),
        end = Offset(
            x = center.x + dx * radius,
            y = center.y + dy * radius
        ),
        color = color,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

// Dial radius in px at which the pixel settings keep their old size: a 600 px dial, about the
// 560 px tall page of a 1024x600 panel at 160 dpi, where they were tuned.
private const val REFERENCE_RADIUS_JA = 300F
private const val HUB_RADIUS = 15F
private const val TAIL_LENGTH = 30F

const val DEFAULT_BORDER_RADIUS_JA = 0.9F
const val DEFAULT_BORDER_THICKNESS_JA = 7F
const val DEFAULT_HAND_LEN_SECONDS_JA = 0.7F
const val DEFAULT_HAND_LEN_MINUTES_JA = 0.6F
const val DEFAULT_HAND_LEN_HOURS_JA = 0.45F
const val DEFAULT_HAND_WIDTH_SECONDS_JA = 4F
const val DEFAULT_HAND_WIDTH_MINUTES_JA = 8F
const val DEFAULT_HAND_WIDTH_HOURS_JA = 8F
const val DEFAULT_SHOW_SECOND_HAND_JA = true
