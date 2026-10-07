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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.res.ResourcesCompat
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.Font
import com.smsoft.smartdisplay.utils.getStateFromFlow
import com.smsoft.smartdisplay.ui.screen.clock.ClockViewModel
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
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
    val background = painterResource(R.drawable.ic_background_rectangular)
    val backgroundTint = remember(secondaryColor) {
        ColorFilter.tint(
            color = secondaryColor
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Frame and numerals: redrawn only when the page size or a setting changes.
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val frame = Frame(size)
            withTransform({
                translate(left = frame.center.x, top = frame.center.y)
                if (frame.portrait) {
                    rotate(degrees = 90F, pivot = Offset.Zero)
                }
                scale(scaleX = frame.scale, scaleY = frame.scale, pivot = Offset.Zero)
                translate(left = -PIVOT_X, top = -PIVOT_Y)
            }) {
                with(background) {
                    draw(
                        size = Size(ART_WIDTH, ART_HEIGHT),
                        colorFilter = backgroundTint
                    )
                }
            }

            numeralPaint.typeface = typeface
            numeralPaint.color = primaryColor.toArgb()
            numeralPaint.textSize = NUMERAL_SIZE * frame.scale * fontSize / DEFAULT_DIGIT_FONT_SIZE_AR
            // 12 and 6 sit on the hour-mark band along the short side of the artwork, 3 and 9
            // along the long side; in portrait the artwork is turned, so the two swap.
            val vertical = if (frame.portrait) NUMERAL_ALONG_LONG else NUMERAL_ALONG_SHORT
            val horizontal = if (frame.portrait) NUMERAL_ALONG_SHORT else NUMERAL_ALONG_LONG
            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas
                drawNumeral(nativeCanvas, numeralPaint, numeralBounds, frame, "12", 0F, -vertical)
                drawNumeral(nativeCanvas, numeralPaint, numeralBounds, frame, "3", horizontal, 0F)
                drawNumeral(nativeCanvas, numeralPaint, numeralBounds, frame, "6", 0F, vertical)
                drawNumeral(nativeCanvas, numeralPaint, numeralBounds, frame, "9", -horizontal, 0F)
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
 * Where the 16:9 artwork lands on the page, in screen px. Everything else (numerals, hands,
 * stroke widths) is placed in artwork px times [scale], so the face keeps its proportions at
 * every resolution and density. On an upright page the artwork is turned by 90 degrees, so it
 * fills the page instead of being a thin band across the middle.
 */
private class Frame(size: Size) {
    val portrait = size.height > size.width
    val center = Offset(size.width / 2F, size.height / 2F)

    // The hour marks are centred 5 px above the middle of the artwork; that point is the pivot,
    // so the fit uses the larger of the two half-heights.
    val scale = min(
        (if (portrait) size.height else size.width) / 2F / PIVOT_X,
        (if (portrait) size.width else size.height) / 2F / (ART_HEIGHT - PIVOT_Y)
    )

    // The artwork's box on screen (turning maps artwork (x, y) to screen (-y, x)).
    val left = center.x - scale * (if (portrait) ART_HEIGHT - PIVOT_Y else PIVOT_X)
    val right = center.x + scale * (if (portrait) PIVOT_Y else ART_WIDTH - PIVOT_X)
    val top = center.y - scale * (if (portrait) PIVOT_X else PIVOT_Y)
    val bottom = center.y + scale * (if (portrait) ART_WIDTH - PIVOT_X else ART_HEIGHT - PIVOT_Y)
}

// Centres the numeral's ink on the anchor (artwork px from the pivot) and keeps it in the frame.
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
    val x = clamp(frame.center.x + dx * frame.scale, frame.left + halfWidth, frame.right - halfWidth)
    val y = clamp(frame.center.y + dy * frame.scale, frame.top + halfHeight, frame.bottom - halfHeight)
    canvas.drawText(text, x - bounds.exactCenterX(), y - bounds.exactCenterY(), paint)
}

private fun clamp(value: Float, low: Float, high: Float) =
    if (low > high) (low + high) / 2F else value.coerceIn(low, high)

// clockAngle is clockwise from 12 o'clock in radians.
private fun DrawScope.drawHand(
    frame: Frame,
    clockAngle: Double,
    length: Float,
    width: Float,
    color: Color
) {
    // The artwork does not put its hour marks at true angles: they are spread along the long
    // side of the frame (1 o'clock at about 43 degrees). Stretch the hand angle the same way,
    // so the hands point at the marks.
    val sinA = sin(clockAngle)
    val cosA = cos(clockAngle)
    val angle = if (frame.portrait) {
        atan2(sinA, MARKER_STRETCH * cosA)
    } else {
        atan2(MARKER_STRETCH * sinA, cosA)
    }
    drawLine(
        start = frame.center,
        end = Offset(
            x = frame.center.x + (sin(angle) * length).toFloat(),
            y = frame.center.y - (cos(angle) * length).toFloat()
        ),
        color = color,
        strokeWidth = width,
        cap = StrokeCap.Square
    )
}

// Geometry of res/drawable/ic_background_rectangular.png, in its own px (measured from the file).
private const val ART_WIDTH = 1280F
private const val ART_HEIGHT = 720F
private const val PIVOT_X = 640F
private const val PIVOT_Y = 355F
// tan(mark angle) = MARKER_STRETCH * tan(true angle); fits all eight marks within about 1 degree.
private const val MARKER_STRETCH = 1.62
// Distance of the numeral centres from the pivot: on the mark band, short and long axis.
private const val NUMERAL_ALONG_SHORT = 286F
private const val NUMERAL_ALONG_LONG = 575F
// Text size of the numerals at the default font size setting (glyphs about as tall as the marks).
private const val NUMERAL_SIZE = 100F
// Hand length 1.0 stays inside the frame (half its short side is 355 px); the default minute
// hand stays clear of the 12.
private const val HAND_BASE = 320F

const val DEFAULT_DIGIT_FONT_SIZE_AR = 80F
const val DEFAULT_HAND_LEN_MINUTES_AR = 0.7F
const val DEFAULT_HAND_LEN_HOURS_AR = 0.4F
const val DEFAULT_HAND_WIDTH_MINUTES_AR = 16F
const val DEFAULT_HAND_WIDTH_HOURS_AR = 8F
