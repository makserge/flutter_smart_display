package cn.tecotaku.cn.digital_clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun DigitalClock2(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    color: Color = MaterialTheme.colors.primary,
    hour: Int,
    minute: Int,
    second: Int,
    millisecond: Int,
) {
    val configuration = LocalConfiguration.current

    val width = with(LocalDensity.current) {
        configuration.screenWidthDp.dp.toPx()
    }.toInt()
    val height = with(LocalDensity.current) {
        configuration.screenHeightDp.dp.toPx()
    }.toInt()

    setTime(
        color = color,
        width = width,
        height = height,
        isShowSeconds = true,
        hour = hour,
        minute = minute,
        second = second,
    )

    Column (
        modifier = modifier
            .fillMaxSize(),
    ) {
        OnDraw(
            modifier = Modifier,
            millisecond = millisecond
        )
    }
}

@Composable
fun OnDraw(
    modifier: Modifier,
    millisecond: Int,
) {
    Canvas(
        modifier = modifier,

    ) {
        millisecond
        for (i in nums.indices) {
            nums[i].onDraw(this)
        }
    }
}

private var lastSecond = 0
private var nums = ArrayList<Number>()
private const val shadowRadius = 14
var numbers = arrayOf(
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
    color: Color,
    width: Int,
    height: Int,
    isShowSeconds: Boolean,
    hour: Int,
    minute: Int,
    second: Int
) {

    if (nums.isEmpty()) {
        val length = if (isShowSeconds) (width / 9) else (width / 6)  // Digits height
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
                    initialColor = android.graphics.Color.parseColor("#66CCFF"),
                    shadowRadius = shadowRadius,
                    animationDuration = 800
                )
            )
            x += length + textMargin
        }
    }

    if (lastSecond != second) {
        updateNumber(0, hour / 10)
        updateNumber(1, hour % 10)
        updateNumber(2, minute / 10)
        updateNumber(3, minute % 10)
        if (isShowSeconds) {
            updateNumber(4, second / 10)
            updateNumber(5, second % 10)
        }
        lastSecond = second
    }
}

private fun updateNumber(numIndex: Int, newNumber: Int) {
    nums[numIndex].updateNumber(newNumber)
}