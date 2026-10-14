package com.smsoft.smartdisplay.ui.composable.clock.rectangular

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import com.smsoft.smartdisplay.data.Font
import com.smsoft.smartdisplay.utils.getStateFromFlow
import com.smsoft.smartdisplay.ui.screen.clock.ClockViewModel
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
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
    // Kept for the caller; without a second hand whole seconds are enough.
    @Suppress("UNUSED_PARAMETER") millisecond: Int
) {
    // This function runs on every 100 ms tick, so it only stores the time. The face below reads
    // it in the draw phase: it is not recomposed by the tick and the hands redraw once a second.
    val secondOfDay = rememberUpdatedState(
        (hour * 60 + minute) * 60 + second
    )

    OnDraw(
        modifier = modifier,
        viewModel = viewModel,
        settingsScale = scale,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        secondOfDay = { secondOfDay.value }
    )
}

/**
 * A rectangular clock face that fills the page whatever its shape: eight marks on a rectangle
 * inset from the page edges, the numerals 12, 3, 6 and 9 between them, and two hands. The marks
 * and the hands are spread towards the long sides, as on a rectangular face. The face used to be
 * a 16:9 picture, which left a square page more than half empty and its hands tiny.
 */
@Composable
private fun OnDraw(
    modifier: Modifier,
    viewModel: ClockViewModel,
    settingsScale: Float,
    primaryColor: Color,
    secondaryColor: Color,
    secondOfDay: () -> Int
) {
    val fontRes = getStateFromFlow(
        flow = viewModel.fontResAR,
        defaultValue = Font.getDefault()
    ) as Int

    val fontSize = settingsScale * getStateFromFlow(
        flow = viewModel.fontSizeAR,
        defaultValue = DEFAULT_DIGIT_FONT_SIZE_AR
    ) as Float

    val minutesHandLength = settingsScale * getStateFromFlow(
        flow = viewModel.minutesHandLengthAR,
        defaultValue = DEFAULT_HAND_LEN_MINUTES_AR
    ) as Float

    val minutesHandWidth = settingsScale * getStateFromFlow(
        flow = viewModel.minutesHandWidthAR,
        defaultValue = DEFAULT_HAND_WIDTH_MINUTES_AR
    ) as Float

    val hoursHandLength = settingsScale * getStateFromFlow(
        flow = viewModel.hoursHandLengthAR,
        defaultValue = DEFAULT_HAND_LEN_HOURS_AR
    ) as Float

    val hoursHandWidth = settingsScale * getStateFromFlow(
        flow = viewModel.hoursHandWidthAR,
        defaultValue = DEFAULT_HAND_WIDTH_HOURS_AR
    ) as Float

    val context = LocalContext.current
    val typeface = remember(context, fontRes) {
        runCatching { ResourcesCompat.getFont(context, fontRes) }.getOrNull() ?: Typeface.DEFAULT
    }
    // The numerals are drawn with a platform Paint so their ink, not their line box, can be
    // centred on the hour position; line boxes differ a lot between the digit fonts.
    val numeralPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }
    val numeralBounds = remember {
        Rect()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Marks and numerals: redrawn only when the page size or a setting changes.
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val frame = Frame(size)
            for (hour in MARK_HOURS) {
                val direction = frame.direction(hour / 12.0 * 2 * PI)
                val outer = frame.edge(direction)
                drawLine(
                    start = outer,
                    end = outer - direction * (MARK_LENGTH * frame.scale),
                    color = secondaryColor,
                    strokeWidth = MARK_WIDTH * frame.scale,
                    cap = StrokeCap.Square
                )
            }

            numeralPaint.typeface = typeface
            numeralPaint.color = primaryColor.toArgb()
            numeralPaint.textSize = NUMERAL_SIZE * frame.scale * fontSize / DEFAULT_DIGIT_FONT_SIZE_AR
            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas
                drawNumeral(nativeCanvas, numeralPaint, numeralBounds, frame, "12", 0F, -frame.halfHeight)
                drawNumeral(nativeCanvas, numeralPaint, numeralBounds, frame, "3", frame.halfWidth, 0F)
                drawNumeral(nativeCanvas, numeralPaint, numeralBounds, frame, "6", 0F, frame.halfHeight)
                drawNumeral(nativeCanvas, numeralPaint, numeralBounds, frame, "9", -frame.halfWidth, 0F)
            }
        }
        // Hands: the only part that reads the time.
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val frame = Frame(size)
            val seconds = secondOfDay()
            val handBase = HAND_BASE * frame.scale
            drawHand(
                frame = frame,
                clockAngle = (seconds % 3_600) / 3_600.0 * 2 * PI,
                length = minutesHandLength * handBase,
                width = minutesHandWidth * frame.scale,
                color = secondaryColor
            )
            drawHand(
                frame = frame,
                clockAngle = (seconds % 43_200) / 43_200.0 * 2 * PI,
                length = hoursHandLength * handBase,
                width = hoursHandWidth * frame.scale,
                color = secondaryColor
            )
            // A hub over the hand roots: the square ends stuck out behind the pivot and formed
            // an open "<" where the two hands met. Its radius is the wider hand's width, so it
            // covers both square ends.
            drawCircle(
                color = secondaryColor,
                radius = max(minutesHandWidth, hoursHandWidth) * frame.scale,
                center = frame.center
            )
        }
    }
}

/**
 * The face on the page, in screen px: the rectangle of the marks, inset from the page edges by
 * [MARK_INSET], and the [scale] of everything drawn, per px of the short side of the former
 * artwork, so the face keeps its proportions at every resolution and density.
 */
private class Frame(size: Size) {
    val center = Offset(size.width / 2F, size.height / 2F)
    val halfWidth = size.width / 2F * MARK_INSET
    val halfHeight = size.height / 2F * MARK_INSET
    val left = center.x - halfWidth
    val right = center.x + halfWidth
    val top = center.y - halfHeight
    val bottom = center.y + halfHeight
    val scale = min(size.width, size.height) / ART_SHORT_SIDE

    // The marks do not sit at true angles: they are spread towards the long sides of the
    // rectangle, tan(mark angle) = stretch * tan(true angle). The stretch follows the shape of
    // the rectangle, damped: 1 on a square, 1.5 to 1.65 on a 16:9 to 2:1 page, which puts the
    // 1 o'clock mark at 42 to 44 degrees as on the former artwork (43 degrees).
    private val stretch = (halfWidth / halfHeight).toDouble().pow(STRETCH_POWER)

    /** The unit direction on screen of [clockAngle] (clockwise from 12 o'clock, in radians). */
    fun direction(clockAngle: Double): Offset {
        val angle = atan2(stretch * sin(clockAngle), cos(clockAngle))
        return Offset(sin(angle).toFloat(), -cos(angle).toFloat())
    }

    /** Where the ray from the centre in [direction] meets the rectangle of the marks. */
    fun edge(direction: Offset): Offset {
        val toSide = if (direction.x == 0F) Float.MAX_VALUE else halfWidth / abs(direction.x)
        val toEnd = if (direction.y == 0F) Float.MAX_VALUE else halfHeight / abs(direction.y)
        return center + direction * min(toSide, toEnd)
    }
}

// Centres the numeral's ink on the anchor (px from the centre) and keeps it in the frame.
private fun drawNumeral(
    canvas: android.graphics.Canvas,
    paint: Paint,
    bounds: Rect,
    frame: Frame,
    text: String,
    dx: Float,
    dy: Float
) {
    paint.getTextBounds(text, 0, text.length, bounds)
    val halfWidth = bounds.width() / 2F
    val halfHeight = bounds.height() / 2F
    val x = clamp(frame.center.x + dx, frame.left + halfWidth, frame.right - halfWidth)
    val y = clamp(frame.center.y + dy, frame.top + halfHeight, frame.bottom - halfHeight)
    canvas.drawText(text, x - bounds.exactCenterX(), y - bounds.exactCenterY(), paint)
}

private fun clamp(value: Float, low: Float, high: Float) =
    if (low > high) (low + high) / 2F else value.coerceIn(low, high)

// clockAngle is clockwise from 12 o'clock in radians; the hand points at the mark of its time.
private fun DrawScope.drawHand(
    frame: Frame,
    clockAngle: Double,
    length: Float,
    width: Float,
    color: Color
) {
    drawLine(
        start = frame.center,
        end = frame.center + frame.direction(clockAngle) * length,
        color = color,
        strokeWidth = width,
        cap = StrokeCap.Square
    )
}

// Short side of the former 16:9 face artwork (1280 by 720 px): the sizes below are in its px.
private const val ART_SHORT_SIDE = 720F
// The rectangle of the marks, as a share of the half extents of the page.
private const val MARK_INSET = 0.88F
// Damping of the mark spread, see Frame.stretch.
private const val STRETCH_POWER = 0.75
// The hours with a mark; 12, 3, 6 and 9 have numerals instead.
private val MARK_HOURS = listOf(1, 2, 4, 5, 7, 8, 10, 11)
private const val MARK_LENGTH = 46F
private const val MARK_WIDTH = 12F
// Text size of the numerals at the default font size setting (glyphs about as tall as the marks).
private const val NUMERAL_SIZE = 100F
// Hand length 1.0 stays inside the page (half its short side is 360 px) and reaches a little past
// the mark rectangle (317 px). The defaults below are as long as the other analog clocks' hands
// (NightDream: 0.9 and 0.7 of the radius); shorter ones looked stubby beside them.
private const val HAND_BASE = 320F

const val DEFAULT_DIGIT_FONT_SIZE_AR = 80F
const val DEFAULT_HAND_LEN_MINUTES_AR = 0.9F
const val DEFAULT_HAND_LEN_HOURS_AR = 0.65F
const val DEFAULT_HAND_WIDTH_MINUTES_AR = 16F
const val DEFAULT_HAND_WIDTH_HOURS_AR = 8F
