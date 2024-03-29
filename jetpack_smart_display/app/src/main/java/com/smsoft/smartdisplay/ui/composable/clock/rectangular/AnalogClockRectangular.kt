package com.smsoft.smartdisplay.ui.composable.clock.rectangular

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.utils.getStateFromFlow
import com.smsoft.smartdisplay.ui.screen.clock.ClockViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogClockRectangular(
    modifier: Modifier = Modifier,
    viewModel: ClockViewModel,
    scale: Float,
    primaryColor: Color,
    secondaryColor: Color,
    hour: Int,
    minute: Int,
    second: Int,
    millisecond: Int
) {
    val fontRes = getStateFromFlow(
        flow = viewModel.fontResAR,
        defaultValue = com.smsoft.smartdisplay.data.Font.getDefault()
    ) as Int

    val fontSize = scale * getStateFromFlow(
        flow = viewModel.fontSizeAR,
        defaultValue = DEFAULT_DIGIT_FONT_SIZE_AR
    ) as Float

    val font = FontFamily(
        Font(
            resId = fontRes,
            weight = FontWeight.Normal
        )
    )

    val minutesHandLength = scale * getStateFromFlow(
        flow = viewModel.minutesHandLengthAR,
        defaultValue = DEFAULT_HAND_LEN_MINUTES_AR
    ) as Float

    val minutesHandWidth = scale * getStateFromFlow(
        flow = viewModel.minutesHandWidthAR,
        defaultValue = DEFAULT_HAND_WIDTH_MINUTES_AR
    ) as Float

    val hoursHandLength = scale * getStateFromFlow(
        flow = viewModel.hoursHandLengthAR,
        defaultValue = DEFAULT_HAND_LEN_HOURS_AR
    ) as Float

    val hoursHandWidth = scale * getStateFromFlow(
        flow = viewModel.hoursHandWidthAR,
        defaultValue = DEFAULT_HAND_WIDTH_HOURS_AR
    ) as Float

    Box(
        modifier = Modifier
    ) {
        DrawStaticUi(
            modifier = Modifier,
            scale = scale,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            font = font,
            fontSize = fontSize.sp
        )
        DrawHands(
            modifier = Modifier,
            color = secondaryColor,
            minutesHandLength = minutesHandLength,
            minutesHandWidth = minutesHandWidth,
            hoursHandLength = hoursHandLength,
            hoursHandWidth = hoursHandWidth,
            hour = hour,
            minute = minute,
            second = second,
            millisecond = millisecond
        )
    }
}

@Composable
fun DrawStaticUi(
    modifier: Modifier,
    scale: Float,
    primaryColor: Color,
    secondaryColor: Color,
    font : FontFamily,
    fontSize: TextUnit
) {
    var parentSize by remember { mutableStateOf(Size.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                parentSize = it.parentLayoutCoordinates?.size?.toSize()?: Size.Zero
            }
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = scale * 9.dp
                ),
            painter = painterResource(R.drawable.ic_background_rectangular),
            contentDescription = null,
            colorFilter = ColorFilter.tint(
                color = secondaryColor
            )
        )
        Row(
            modifier = Modifier
                .width(parentSize.width.dp)
                .fillMaxHeight()
                .padding(
                    start = scale * 30.dp,
                    end = scale * 30.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier,
                text = "9",
                color = primaryColor,
                fontFamily = font,
                fontSize = fontSize
            )
            Spacer(
                modifier = Modifier
                    .weight(1F)
            )
            Text(
                modifier = Modifier,
                textAlign = TextAlign.Right,
                text = "3",
                color = primaryColor,
                fontFamily = font,
                fontSize = fontSize
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scale * parentSize.width.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier,
                    text = "12",
                    color = primaryColor,
                    fontFamily = font,
                    fontSize = fontSize
                )
                Spacer(
                    modifier = Modifier.weight(1F)
                )
                Text(
                    modifier = Modifier,
                    text = "6",
                    color = primaryColor,
                    fontFamily = font,
                    fontSize = fontSize
                )
            }
        }
    }
}

@Composable
fun DrawHands(
    modifier: Modifier,
    color: Color,
    minutesHandLength: Float,
    minutesHandWidth: Float,
    hoursHandLength: Float,
    hoursHandWidth: Float,
    hour: Int,
    minute: Int,
    second: Int,
    millisecond: Int
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val progression = ((millisecond % 1000) / 1000.0)

        val centerX = (size.width / 2)
        val centerY = (size.height / 2)

        val hourInt = if (hour > 12) hour - 12 else hour

        val animatedSecond = second + progression
        val animatedMinute = minute + animatedSecond / 60
        val animatedHour = (hourInt + (animatedMinute / 60)) * 5F
        minuteHand(
            centerX = centerX,
            centerY = centerY,
            clockRadius = size.getRadius(minutesHandLength),
            strokeWidth = minutesHandWidth,
            animatedMinute = animatedMinute,
            color = color
        )
        hourHand(
            centerX = centerX,
            centerY = centerY,
            clockRadius = size.getRadius(hoursHandLength),
            strokeWidth = hoursHandWidth,
            animatedHour = animatedHour,
            color = color
        )
    }
}

fun DrawScope.minuteHand(
    centerX: Float,
    centerY: Float,
    clockRadius: Float,
    strokeWidth: Float,
    animatedMinute: Double,
    color: Color
) {
    val degree = animatedMinute * (Math.PI / 30) - (Math.PI / 2)
    drawLine(
        start = Offset(
            x = centerX,
            y = centerY
        ),
        end = Offset(
            x = (centerX + cos(degree) * clockRadius).toFloat(),
            y = (centerY + sin(degree) * clockRadius).toFloat()
        ),
        color = color,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Square
    )
}

fun DrawScope.hourHand(
    centerX: Float,
    centerY: Float,
    clockRadius: Float,
    strokeWidth: Float,
    animatedHour: Double,
    color: Color
) {
    val degree = animatedHour * (Math.PI / 30) - (Math.PI / 2)
    drawLine(
        start = Offset(
            x = centerX,
            y = centerY
        ),
        end = Offset(
            x = (centerX + cos(degree) * clockRadius).toFloat(),
            y = (centerY + sin(degree) * clockRadius).toFloat()
        ),
        color = color,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Square
    )
}

private fun Size.getRadius(
    expo: Float
) : Float {
    return expo * min(Dp(width / 2), Dp(height / 2)).value
}

const val DEFAULT_DIGIT_FONT_SIZE_AR = 80F
const val DEFAULT_HAND_LEN_MINUTES_AR = 0.7F
const val DEFAULT_HAND_LEN_HOURS_AR = 0.4F
const val DEFAULT_HAND_WIDTH_MINUTES_AR = 16F
const val DEFAULT_HAND_WIDTH_HOURS_AR = 8F