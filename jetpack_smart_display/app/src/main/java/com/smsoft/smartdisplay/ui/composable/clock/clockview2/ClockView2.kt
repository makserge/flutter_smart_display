package com.smsoft.smartdisplay.ui.composable.clock.clockview2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.utils.getColor
import com.smsoft.smartdisplay.utils.getStateFromFlow
import com.smsoft.smartdisplay.ui.screen.clock.ClockViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ClockView2(
    modifier: Modifier = Modifier,
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int,
    viewModel: ClockViewModel,
    primaryColor: androidx.compose.ui.graphics.Color,
    secondaryColor: androidx.compose.ui.graphics.Color
) {
    val primaryArgb = remember(primaryColor) { getColor(primaryColor) }
    val secondaryArgb = remember(secondaryColor) { getColor(secondaryColor) }

    val context = LocalContext.current

    val font = getStateFromFlow(
        flow = viewModel.fontCV2,
        defaultValue = Font.getDefault().font
    ) as Int

    val digitStyle = getStateFromFlow(
        flow = viewModel.digitStyleCV2,
        defaultValue = DigitStyle.getDefault()
    ) as DigitStyle

    val digitTextSize = getStateFromFlow(
        flow = viewModel.digitTextSizeCV2,
        defaultValue = DEFAULT_DIGIT_TEXT_SIZE_CV2
    ) as Float

    val outerRimWidth = getStateFromFlow(
        flow = viewModel.outerRimWidthCV2,
        defaultValue = DEFAULT_OUTER_RIM_WIDTH_CV2
    ) as Float

    val innerRimWidth = getStateFromFlow(
        flow = viewModel.innerRimWidthCV2,
        defaultValue = DEFAULT_INNER_RIM_WIDTH_CV2
    ) as Float

    val thickMarkerWidth = getStateFromFlow(
        flow = viewModel.thickMarkerWidthCV2,
        defaultValue = DEFAULT_THICK_MARKER_WIDTH_CV2
    ) as Float

    val thinMarkerWidth = getStateFromFlow(
        flow = viewModel.thinMarkerWidthCV2,
        defaultValue = DEFAULT_THIN_MARKER_WIDTH_CV2
    ) as Float

    val hourHandWidth = getStateFromFlow(
        flow = viewModel.hourHandWidthCV2,
        defaultValue = DEFAULT_HOUR_HAND_WIDTH_CV2
    ) as Float

    val minuteHandWidth = getStateFromFlow(
        flow = viewModel.minuteHandWidthCV2,
        defaultValue = DEFAULT_MINUTE_HAND_WIDTH_CV2
    ) as Float

    val secondHandWidth = getStateFromFlow(
        flow = viewModel.secondHandWidthCV2,
        defaultValue = DEFAULT_SECOND_HAND_WIDTH_CV2
    ) as Float

    val centerCircleRadius = getStateFromFlow(
        flow = viewModel.centerCircleRadiusCV2,
        defaultValue = DEFAULT_CENTER_CIRCLE_RADIUS_CV2
    ) as Float

    // Loading the font is a resource lookup: do it when the setting changes, not 10 times a second.
    val digitFont = remember(context, font) {
        ResourcesCompat.getFont(context, font) ?: Typeface.DEFAULT
    }

    // The time changes every 100 ms. It reaches the Canvas through a State that only the draw
    // phase reads, so OnDraw skips recomposition and each tick just redraws the dial.
    val dayMillis = rememberUpdatedState(((hour * 60 + minute) * 60 + second) * 1000 + milliSecond)

    OnDraw(
        modifier = modifier,
        showThickMarkers = true,
        showThinMarkers = true,
        showNumbers = true,
        showSweepHand = true,
        primaryColor = primaryArgb,
        secondaryColor = secondaryArgb,
        digitFont = digitFont,
        digitStyle = digitStyle,
        digitTextSize = digitTextSize,
        outerRimWidth = outerRimWidth,
        innerRimWidth = innerRimWidth,
        thickMarkerWidth = thickMarkerWidth,
        thinMarkerWidth = thinMarkerWidth,
        hourHandWidth = hourHandWidth,
        minuteHandWidth = minuteHandWidth,
        secondHandWidth = secondHandWidth,
        centerCircleRadius = centerCircleRadius,
        dayMillis = { dayMillis.value }
    )
}

@Composable
private fun OnDraw(
    modifier: Modifier,
    showThickMarkers: Boolean,
    showThinMarkers: Boolean,
    showNumbers: Boolean,
    showSweepHand: Boolean,
    primaryColor: Int,
    secondaryColor: Int,
    digitFont: Typeface,
    digitStyle: DigitStyle,
    digitTextSize: Float,
    outerRimWidth: Float,
    innerRimWidth: Float,
    thickMarkerWidth: Float,
    thinMarkerWidth: Float,
    hourHandWidth: Float,
    minuteHandWidth: Float,
    secondHandWidth: Float,
    centerCircleRadius: Float,
    dayMillis: () -> Int
) {
    // Kept for the life of the clock (they were process-wide globals changed during composition).
    val paints = remember { ClockView2Paints() }
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(all = 20.dp)
    ) {
        val radius = size.minDimension / 2F
        // Pixels per setting unit. The sizes were tuned in dp for a 520 dp dial (1024x600 and
        // 1280x800 panels). Scaling them with the dial keeps those proportions on every panel and
        // density instead of tiny text and long ticks on big dials, crowded ones on small dials.
        val unit = size.minDimension / REFERENCE_DIAL_SIZE
        paints.setUp(
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            digitFont = digitFont,
            // Not below MIN_DIGIT_TEXT_SIZE: on an 800x480 panel the numerals were 10 px tall
            digitTextSize = max(digitTextSize * unit, MIN_DIGIT_TEXT_SIZE.toPx()),
            outerRimWidth = lineWidth(outerRimWidth, unit),
            innerRimWidth = lineWidth(innerRimWidth, unit),
            thickMarkerWidth = lineWidth(thickMarkerWidth, unit),
            thinMarkerWidth = lineWidth(thinMarkerWidth, unit),
            hourHandWidth = lineWidth(hourHandWidth, unit),
            minuteHandWidth = lineWidth(minuteHandWidth, unit),
            secondHandWidth = lineWidth(secondHandWidth, unit)
        )
        val thinMarkerLength = THIN_MARKER_LENGTH * unit
        val thickMarkerLength = THICK_MARKER_LENGTH * unit
        val handInset = HAND_INSET * unit
        val fm = paints.fontMetrics
        paints.digit.getFontMetrics(fm)
        val numberHeight = -fm.ascent + fm.descent
        val innerRimRadius = radius - thickMarkerLength - numberHeight - fm.bottom
        val minuteHandLength = radius - thinMarkerLength - handInset
        // Clearly shorter than the minute hand: ending at the inner rim made it 0.85-0.9 of the
        // minute hand, so the two were hard to tell apart.
        val hourHandLength = min(innerRimRadius - handInset, HOUR_HAND_RATIO * minuteHandLength)

        // Draw-phase read: a new time invalidates only this drawing.
        val millis = dayMillis()
        val hour = millis / 3_600_000
        val minute = millis / 60_000 % 60
        val second = millis / 1000 % 60
        val milliSecond = millis % 1000

        drawIntoCanvas {
            val canvas = it.nativeCanvas
            canvas.save()
            canvas.translate(center.x, center.y)
            canvas.drawCircle(0F, 0F, radius, paints.clockFace)
            canvas.drawCircle(0F, 0F, radius - thinMarkerLength, paints.outerRim)
            if (showThickMarkers) {
                drawMarkers(
                    canvas = canvas,
                    radius = radius,
                    length = thickMarkerLength,
                    thick = true,
                    paint = paints.thickMarker
                )
            }
            if (showThinMarkers) {
                drawMarkers(
                    canvas = canvas,
                    radius = radius,
                    length = thinMarkerLength,
                    thick = false,
                    paint = paints.thinMarker
                )
            }
            if (showNumbers) {
                drawNumbers(
                    canvas = canvas,
                    outerRadius = radius - thickMarkerLength,
                    halfHeight = numberHeight / 2,
                    baselineShift = -(fm.ascent + fm.descent) / 2,
                    digitStyle = digitStyle,
                    paint = paints.digit
                )
            }
            canvas.drawCircle(0F, 0F, innerRimRadius, paints.innerRim)
            drawHand(
                canvas = canvas,
                radian = (hour - 3) * Math.PI / 6 + minute * Math.PI / 360 + second * Math.PI / 21600,
                length = hourHandLength,
                paint = paints.hourHand
            )
            drawHand(
                canvas = canvas,
                radian = (minute - 15) * Math.PI / 30 + second * Math.PI / 1800,
                length = minuteHandLength,
                paint = paints.minuteHand
            )
            if (showSweepHand) {
                drawHand(
                    canvas = canvas,
                    radian = (1000 * second + milliSecond - 15000) * Math.PI / 30000,
                    length = radius,
                    paint = paints.sweepHand
                )
            }
            canvas.drawCircle(0F, 0F, centerCircleRadius * unit, paints.centerCircle)
            canvas.restore()
        }
    }
}

// Thin lines stay at least one pixel wide; below that they fade out on small dials.
private fun lineWidth(
    value: Float,
    unit: Float
) = max(1F, value * unit)

private class ClockView2Paints {
    val clockFace = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = android.graphics.Color.BLACK
    }
    val outerRim = strokePaint()
    val innerRim = strokePaint()
    val thickMarker = strokePaint()
    val thinMarker = strokePaint()
    val digit = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    val hourHand = strokePaint()
    val minuteHand = strokePaint()
    val sweepHand = strokePaint()
    val centerCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    val fontMetrics = Paint.FontMetrics()

    // Widths and text size in px for the current dial.
    fun setUp(
        primaryColor: Int,
        secondaryColor: Int,
        digitFont: Typeface,
        digitTextSize: Float,
        outerRimWidth: Float,
        innerRimWidth: Float,
        thickMarkerWidth: Float,
        thinMarkerWidth: Float,
        hourHandWidth: Float,
        minuteHandWidth: Float,
        secondHandWidth: Float
    ) {
        outerRim.setStroke(primaryColor, outerRimWidth)
        innerRim.setStroke(primaryColor, innerRimWidth)
        thickMarker.setStroke(primaryColor, thickMarkerWidth)
        thinMarker.setStroke(primaryColor, thinMarkerWidth)
        hourHand.setStroke(primaryColor, hourHandWidth)
        minuteHand.setStroke(primaryColor, minuteHandWidth)
        sweepHand.setStroke(primaryColor, secondHandWidth)
        centerCircle.color = primaryColor
        digit.color = secondaryColor
        digit.typeface = digitFont
        digit.textSize = digitTextSize
    }

    private fun strokePaint() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private fun Paint.setStroke(
        color: Int,
        width: Float
    ) {
        this.color = color
        strokeWidth = width
    }
}

// Ticks from the dial edge inwards: every 30 degrees (thick) or the other 6 degree steps (thin).
private fun drawMarkers(
    canvas: Canvas,
    radius: Float,
    length: Float,
    thick: Boolean,
    paint: Paint
) {
    var degree = 0
    while (degree < 360) {
        if ((degree % 30 == 0) == thick) {
            val radian = degree * Math.PI / 180
            val cosA = cos(radian).toFloat()
            val sinA = sin(radian).toFloat()
            canvas.drawLine(
                radius * cosA,
                radius * sinA,
                (radius - length) * cosA,
                (radius - length) * sinA,
                paint
            )
        }
        degree += 6
    }
}

/**
 * Draws the hour numerals inside [outerRadius] (the inner end of the hour ticks). Each label is
 * moved in by its own extent along its direction, so wide labels such as "10" or "XI" do not
 * touch their tick; at 12 and 6 this is [halfHeight], as before.
 */
private fun drawNumbers(
    canvas: Canvas,
    outerRadius: Float,
    halfHeight: Float,
    baselineShift: Float,
    digitStyle: DigitStyle,
    paint: Paint
) {
    val numbers = if (digitStyle == DigitStyle.ROMAN) ROMAN_NUMBER_LIST else ARABIC_NUMBER_LIST
    var degree = -60
    for (numberText in numbers) {
        val radian = degree * Math.PI / 180
        val cosA = cos(radian).toFloat()
        val sinA = sin(radian).toFloat()
        val halfWidth = paint.measureText(numberText) / 2
        val numberRadius = outerRadius - (abs(cosA) * halfWidth + abs(sinA) * halfHeight)
        canvas.drawText(
            numberText,
            numberRadius * cosA,
            numberRadius * sinA + baselineShift,
            paint
        )
        degree += 30
    }
}

private fun drawHand(
    canvas: Canvas,
    radian: Double,
    length: Float,
    paint: Paint
) {
    canvas.drawLine(0F, 0F, length * cos(radian).toFloat(), length * sin(radian).toFloat(), paint)
}

enum class DigitStyle(val value: String, val titleId: Int) {
    ARABIC("arabic", R.string.digit_style_arabic),
    ROMAN("roman", R.string.digit_style_roman);

    companion object {
        fun toMap(context: Context): Map<String, String> {
            return values().associate {
                it.value to context.getString(it.titleId)
            }
        }

        fun getDefault(): DigitStyle {
            return ARABIC
        }

        fun getDefaultId(): String {
            return getDefault().value
        }

        fun getById(id: String): DigitStyle {
            val item = values().filter {
                it.value == id
            }
            return item[0]
        }
    }
}

enum class Font(val id: String, val font: Int, val titleId: Int) {
    ROBOTO_REGULAR("roboto_regular", R.font.roboto_regular, R.string.digit_font_roboto_regular),
    ROBOTO_LIGHT("roboto_light", R.font.roboto_light, R.string.digit_font_roboto_light),
    ROBOTO_THIN("roboto_thin", R.font.roboto_thin, R.string.digit_font_roboto_thin),
    SEVEN_SEGMENT_DIGITAL("seven_segment_digital", R.font.seven_segment_digital, R.string.digit_font_seven_segment_digital),
    DSEG14_CLASSIC("dseg14classic", R.font.dseg14classic, R.string.digit_font_dseg14_classic);

    companion object {
        fun toMap(context: Context): Map<String, String> {
            return values().associate {
                it.id to context.getString(it.titleId)
            }
        }

        fun getDefault(): Font {
            return ROBOTO_REGULAR
        }

        fun getDefaultId(): String {
            return getDefault().id
        }

        fun getById(id: String): Font {
            val item = values().filter {
                it.id == id
            }
            return item[0]
        }
    }
}

// The dial is this many setting units across: the dial size in dp on the 1024x600 and 1280x800
// panels the defaults were tuned on. Text size, marker and hand widths and lengths use this unit.
private const val REFERENCE_DIAL_SIZE = 520F
private const val THIN_MARKER_LENGTH = 10F
private const val THICK_MARKER_LENGTH = 20F
private const val HAND_INSET = 5F
private const val HOUR_HAND_RATIO = 0.7F
private val ROMAN_NUMBER_LIST = arrayOf("Ⅰ", "Ⅱ", "Ⅲ", "Ⅳ", "Ⅴ", "Ⅵ", "Ⅶ", "Ⅷ", "Ⅸ", "Ⅹ", "Ⅺ", "Ⅻ")
private val ARABIC_NUMBER_LIST = Array(12) { (it + 1).toString() }

const val DEFAULT_OUTER_RIM_WIDTH_CV2 = 1F
const val DEFAULT_SECOND_HAND_WIDTH_CV2 = 1F
const val DEFAULT_MINUTE_HAND_WIDTH_CV2 = 3F
const val DEFAULT_HOUR_HAND_WIDTH_CV2 = 5F
const val DEFAULT_DIGIT_TEXT_SIZE_CV2 = 18F
// Smallest numeral text size, whatever the dial size
private val MIN_DIGIT_TEXT_SIZE = 16.dp
const val DEFAULT_THIN_MARKER_WIDTH_CV2 = 1F
const val DEFAULT_THICK_MARKER_WIDTH_CV2 = 3F
const val DEFAULT_INNER_RIM_WIDTH_CV2 = 1F
const val DEFAULT_CENTER_CIRCLE_RADIUS_CV2 = 5F
