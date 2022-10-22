package com.smsoft.smartdisplay.ui.composable.clock.digitalclock2

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.getColor
import com.smsoft.smartdisplay.getStateFromFlow
import com.smsoft.smartdisplay.ui.screen.clock.ClockViewModel

@Composable
fun DigitalClock2(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    viewModel: ClockViewModel,
    scale: Float,
    primaryColor: Color,
    hour: Int,
    minute: Int,
    second: Int
) {
    val isShowSeconds = getStateFromFlow(
        flow = viewModel.isShowSecondsDC2,
        defaultValue = DEFAULT_SHOW_SECONDS_DC2
    ) as Boolean

    val fontSize = getStateFromFlow(
        flow = viewModel.fontSizeDC2,
        defaultValue = DEFAULT_FONT_SIZE_DC2
    ) as Float

    val shadowRadius = scale * getStateFromFlow(
        flow = viewModel.shadowRadiusDC2,
        defaultValue = DEFAULT_SHADOW_RADIUS_DC2
    ) as Float

    val animationDuration = getStateFromFlow(
        flow = viewModel.animationDurationDC2,
        defaultValue = DEFAULT_ANIMATION_DURATION_DC2
    ) as Float

    val configuration = LocalConfiguration.current
    val width = with(LocalDensity.current) {
        configuration.screenWidthDp.dp.toPx()
    }.toInt()
    val height = with(LocalDensity.current) {
        configuration.screenHeightDp.dp.toPx()
    }.toInt()

    var scaleVal by remember { mutableStateOf(1F) }
    if (scaleVal != scale) {
        scaleVal = scale
        nums.clear()
    }

    var secondVal by remember { mutableStateOf(0) }
    if (secondVal != second) {
        secondVal = second

        initClock(
            color = primaryColor,
            width = (width * scale).toInt(),
            fontSize = fontSize,
            height = height,
            isShowSeconds = isShowSeconds,
            shadowRadius = shadowRadius,
            animationDuration = animationDuration
        )

        setTime(
            isShowSeconds = isShowSeconds,
            hour = hour,
            minute = minute,
            second = secondVal
        )
    }
    OnDraw(
        modifier = Modifier
    )
}

@Composable
fun OnDraw(
    modifier: Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = modifier
                .fillMaxSize(),
        ) {
            for (i in nums.indices) {
                nums[i].onDraw(this)
            }
        }
    }
}

private var nums = ArrayList<Number>()
val numbers = arrayOf(
    intArrayOf(1, 1, 1, 0, 1, 1, 1),
    intArrayOf(0, 0, 1, 0, 0, 1, 0),
    intArrayOf(1, 0, 1, 1, 1, 0, 1),
    intArrayOf(1, 0, 1, 1, 0, 1, 1),
    intArrayOf(0, 1, 1, 1, 0, 1, 0),
    intArrayOf(1, 1, 0, 1, 0, 1, 1),
    intArrayOf(1, 1, 0, 1, 1, 1, 1),
    intArrayOf(1, 0, 1, 0, 0, 1, 0),
    intArrayOf(1, 1, 1, 1, 1, 1, 1),
    intArrayOf(1, 1, 1, 1, 0, 1, 1)
)

private fun setTime(
    isShowSeconds: Boolean,
    hour: Int,
    minute: Int,
    second: Int
) {
    updateNumber(
        index = 0,
        value = hour / 10
    )
    updateNumber(
        index = 1,
        value = hour % 10
    )
    updateNumber(
        index = 2,
        value = minute / 10
    )
    updateNumber(
        index = 3,
        value = minute % 10
    )
    if (isShowSeconds) {
        updateNumber(
            index = 4,
            value = second / 10
        )
        updateNumber(
            index = 5,
            value = second % 10
        )
    }
}

private fun initClock(
    color: Color,
    width: Int,
    height: Int,
    fontSize: Float,
    isShowSeconds: Boolean,
    shadowRadius: Float,
    animationDuration: Float
) {
    val initialColor = getColor(color)
    if (nums.isEmpty()) {
        val length = (if (isShowSeconds) {
            width / 9
        } else {
            width / 6
        } * fontSize).toInt() // Digits height
        val textMargin = width / 35 //total 7 margin 0.2 width
        val marginTop = height / 2 - length
        var x = width / 10
        val digits = if (isShowSeconds) 5 else 3
        for (i in 0..digits) {
            nums.add(
                Number(
                    startX = x,
                    startY = marginTop,
                    lineLength = length,
                    initialColor = initialColor,
                    shadowRadius = shadowRadius.toInt(),
                    animationDuration = animationDuration.toInt()
                )
            )
            x += length + textMargin
        }
    }
}

private fun updateNumber(
    index: Int,
    value: Int
) {
    if (nums.isEmpty()) {
        return
    }
    nums[index].updateNumber(
        value = value
    )
}

const val DEFAULT_SHOW_SECONDS_DC2 = false
const val DEFAULT_FONT_SIZE_DC2 = 1F
const val DEFAULT_SHADOW_RADIUS_DC2 = 14F
const val DEFAULT_ANIMATION_DURATION_DC2 = 800F
