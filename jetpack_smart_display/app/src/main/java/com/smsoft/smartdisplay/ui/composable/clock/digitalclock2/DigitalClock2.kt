package com.smsoft.smartdisplay.ui.composable.clock.digitalclock2

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.smsoft.smartdisplay.ui.screen.clock.ClockViewModel
import com.smsoft.smartdisplay.ui.screen.clock.displayHour
import com.smsoft.smartdisplay.utils.getStateFromFlow
import kotlin.math.max
import kotlin.math.min

@Composable
fun DigitalClock2(
    modifier: Modifier = Modifier,
    viewModel: ClockViewModel,
    scale: Float,
    primaryColor: Color,
    hour: Int,
    minute: Int,
    second: Int,
    is24Hour: Boolean
) {
    val isShowSeconds = getStateFromFlow(
        flow = viewModel.isShowSecondsDC2,
        defaultValue = DEFAULT_SHOW_SECONDS_DC2
    ) as Boolean

    val fontSize = scale * getStateFromFlow(
        flow = viewModel.fontSizeDC2,
        defaultValue = DEFAULT_FONT_SIZE_DC2
    ) as Float

    val shadowRadius = getStateFromFlow(
        flow = viewModel.shadowRadiusDC2,
        defaultValue = DEFAULT_SHADOW_RADIUS_DC2
    ) as Float

    val animationDuration = getStateFromFlow(
        flow = viewModel.animationDurationDC2,
        defaultValue = DEFAULT_ANIMATION_DURATION_DC2
    ) as Float

    val shownHour = displayHour(hour, is24Hour)
    // 12-hour format has no leading zero: the tens digit stays dark (all segments unlit) below
    // 10. The number of digits stays the same, so the layout does not move.
    val hourTens = if (!is24Hour && (shownHour < 10)) BLANK_DIGIT else shownHour / 10
    val values = if (isShowSeconds) {
        intArrayOf(hourTens, shownHour % 10, minute / 10, minute % 10, second / 10, second % 10)
    } else {
        intArrayOf(hourTens, shownHour % 10, minute / 10, minute % 10)
    }

    // The digits belong to this composition (no process-wide cache), start on the current time
    // and are laid out from the Canvas size on every draw, so rotation, a size change or a new
    // setting always takes effect.
    val digits = remember(isShowSeconds) {
        List(values.size) {
            Number(values[it])
        }
    }
    digits.forEachIndexed { index, digit ->
        val value = values[index]
        LaunchedEffect(digit, value, animationDuration) {
            digit.morphTo(
                value = value,
                durationMillis = animationDuration.toInt()
            )
        }
    }

    OnDraw(
        modifier = modifier,
        digits = digits,
        color = primaryColor,
        fontSize = fontSize,
        shadowRadius = shadowRadius
    )
}

/**
 * Draws the digits centred in the page, with a wider gap between HH, MM and SS. At font size 1
 * the clock uses [PAGE_FILL] of the page; tall pages stack the groups when that gives bigger
 * digits.
 */
@Composable
fun OnDraw(
    modifier: Modifier,
    digits: List<Number>,
    color: Color,
    fontSize: Float,
    shadowRadius: Float
) {
    val paint = remember {
        Paint().apply {
            isAntiAlias = true
        }
    }
    val argb = color.toArgb()
    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Sizes in digit widths: a digit is 1 wide and 2 tall
        val groups = digits.size / 2
        val groupWidth = 2F + DIGIT_GAP
        val rowWidth = groups * groupWidth + (groups - 1) * GROUP_GAP
        val stackHeight = groups * 2F + (groups - 1) * ROW_GAP
        val maxWidth = size.width * PAGE_FILL
        val maxHeight = size.height * PAGE_FILL
        val rowLength = min(maxWidth / rowWidth, maxHeight / 2F)
        val stackLength = min(maxWidth / groupWidth, maxHeight / stackHeight)
        val isStacked = stackLength > rowLength
        val length = max(rowLength, stackLength) * fontSize
        val left = (size.width - length * (if (isStacked) groupWidth else rowWidth)) / 2F
        val top = (size.height - length * (if (isStacked) stackHeight else 2F)) / 2F

        paint.color = argb
        paint.strokeWidth = length / 25F
        // Glow in the digit colour, set per segment by Number.draw; the setting is in tenths of
        // the segment width
        val glow = paint.strokeWidth * shadowRadius / 10F

        drawIntoCanvas {
            val canvas = it.nativeCanvas
            digits.forEachIndexed { index, digit ->
                val group = index / 2
                val inGroup = (index % 2) * (1F + DIGIT_GAP) * length
                val x = if (isStacked) {
                    left + inGroup
                } else {
                    left + group * (groupWidth + GROUP_GAP) * length + inGroup
                }
                val y = if (isStacked) top + group * (2F + ROW_GAP) * length else top
                digit.draw(
                    canvas = canvas,
                    paint = paint,
                    x = x,
                    y = y,
                    length = length,
                    argb = argb,
                    glow = glow
                )
            }
        }
    }
}

// Gaps in digit widths: between the two digits of a group, between HH, MM and SS in a row,
// and between stacked groups
private const val DIGIT_GAP = 0.2F
private const val GROUP_GAP = 0.7F
private const val ROW_GAP = 0.5F

// Share of the page width and height the clock may use at font size 1; the rest keeps the
// strokes and the glow off the page edges.
private const val PAGE_FILL = 0.9F

const val DEFAULT_SHOW_SECONDS_DC2 = false
const val DEFAULT_FONT_SIZE_DC2 = 1F
const val DEFAULT_SHADOW_RADIUS_DC2 = 14F
const val DEFAULT_ANIMATION_DURATION_DC2 = 800F
