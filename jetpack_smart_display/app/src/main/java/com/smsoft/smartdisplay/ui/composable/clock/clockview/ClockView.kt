package com.smsoft.smartdisplay.ui.composable.clock.clockview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.utils.getColor
import com.smsoft.smartdisplay.utils.getStateFromFlow
import com.smsoft.smartdisplay.ui.screen.clock.ClockViewModel
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ClockView(
    modifier: Modifier = Modifier
        .fillMaxSize(),
    viewModel: ClockViewModel,
    // Not used any more: the dial always fits the page it is given. Kept for the callers.
    @Suppress("UNUSED_PARAMETER") scale: Float,
    primaryColor: Color,
    secondaryColor: Color,
    hour: Int,
    minute: Int,
    second: Int
) {
    val primaryArgb = remember(primaryColor) { getColor(primaryColor) }
    val secondaryArgb = remember(secondaryColor) { getColor(secondaryColor) }

    val context = LocalContext.current

    val font = getStateFromFlow(
        flow = viewModel.fontCV,
        defaultValue = Font.getDefault().font
    ) as Int

    val digitStyle = getStateFromFlow(
        flow = viewModel.digitStyleCV,
        defaultValue = DigitStyle.getDefault()
    ) as DigitStyle

    val showHoursValues = getStateFromFlow(
        flow = viewModel.showHoursValuesCV,
        defaultValue = DEFAULT_SHOW_HOURS_CV
    ) as Boolean

    val showMinutesValues = getStateFromFlow(
        flow = viewModel.showMinutesValuesCV,
        defaultValue = DEFAULT_SHOW_MINUTES_CV
    ) as Boolean

    val showDegrees = getStateFromFlow(
        flow = viewModel.showDegreesCV,
        defaultValue = DEFAULT_SHOW_DEGREES_CV
    ) as Boolean

    val digitDisposition = getStateFromFlow(
        flow = viewModel.digitDispositionCV,
        defaultValue = DigitDisposition.getDefault()
    ) as DigitDisposition

    val digitStep = getStateFromFlow(
        flow = viewModel.digitStepCV,
        defaultValue = DigitStep.getDefault()
    ) as DigitStep

    val degreesType = getStateFromFlow(
        flow = viewModel.degreesTypeCV,
        defaultValue = DegreeType.getDefault()
    ) as DegreeType

    val degreesStep = getStateFromFlow(
        flow = viewModel.degreesStepCV,
        defaultValue = DegreesStep.getDefault()
    ) as DegreesStep

    val showCenter = getStateFromFlow(
        flow = viewModel.showCenterCV,
        defaultValue = DEFAULT_SHOW_CENTER_CV
    ) as Boolean

    val showSecondsHand = getStateFromFlow(
        flow = viewModel.showSecondsHandCV,
        defaultValue = DEFAULT_SHOW_SECOND_HAND_CV
    ) as Boolean

    // Loading the font is a resource lookup: do it when the setting changes, not every second.
    val digitsFont = remember(context, font) {
        ResourcesCompat.getFont(context, font) ?: Typeface.DEFAULT
    }

    OnDraw(
        modifier = modifier,
        showHoursValues = showHoursValues,
        showMinutesValues = showMinutesValues,
        digitStyle = digitStyle,
        digitsColor = secondaryArgb,
        digitsFont = digitsFont,
        showDegrees = showDegrees,
        digitDisposition = digitDisposition,
        digitStep = digitStep,
        minutesProgressColor = primaryArgb,
        minutesValuesFactor = 0.3F,
        degreesColor = primaryArgb,
        degreesType = degreesType,
        degreesStep = degreesStep,
        showCenter = showCenter,
        centerInnerColor = primaryArgb,
        centerOuterColor = primaryArgb,
        needleHoursColor = primaryArgb,
        needleMinutesColor = primaryArgb,
        needleSecondsColor = primaryArgb,
        showSecondsHand = showSecondsHand,
        second = second,
        hour = hour,
        minute = minute
    )
}

@Composable
private fun OnDraw(
    modifier: Modifier,
    showHoursValues: Boolean,
    showMinutesValues: Boolean,
    digitStyle: DigitStyle,
    digitsColor: Int,
    digitsFont: Typeface,
    showDegrees: Boolean,
    digitDisposition: DigitDisposition,
    digitStep: DigitStep,
    minutesProgressColor: Int,
    minutesValuesFactor: Float,
    degreesColor: Int,
    degreesType: DegreeType,
    degreesStep: DegreesStep,
    showCenter: Boolean,
    centerInnerColor: Int,
    centerOuterColor: Int,
    needleHoursColor: Int,
    needleMinutesColor: Int,
    needleSecondsColor: Int,
    showSecondsHand: Boolean,
    second: Int,
    hour: Int,
    minute: Int
) {
    // Kept for the life of the clock instead of allocating new paints on every draw.
    val paints = remember { ClockViewPaints() }
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(all = 20.dp)
    ) {
        // The dial is the largest square that fits the page, centred in both orientations.
        // All sizes below are fractions of clockSize, so they follow the dial on every panel.
        val clockSize = size.minDimension
        drawIntoCanvas {
            val canvas = it.nativeCanvas
            canvas.save()
            canvas.translate(center.x, center.y)
            paints.labelBoxes.clear()
            if (showHoursValues) {
                drawHoursValues(
                    canvas = canvas,
                    clockSize = clockSize,
                    paints = paints,
                    digitsColor = digitsColor,
                    digitsFont = digitsFont,
                    showDegrees = showDegrees,
                    digitStyle = digitStyle,
                    digitDisposition = digitDisposition,
                    digitStep = digitStep
                )
            }
            val minutesValuesInner = if (showMinutesValues) {
                drawMinutesValues(
                    canvas = canvas,
                    clockSize = clockSize,
                    paints = paints,
                    minutesProgressColor = minutesProgressColor,
                    digitsFont = digitsFont,
                    minutesValuesFactor = minutesValuesFactor,
                    digitStyle = digitStyle
                )
            } else {
                null
            }
            if (showDegrees) {
                drawDegrees(
                    canvas = canvas,
                    clockSize = clockSize,
                    paint = paints.degrees,
                    degreesColor = degreesColor,
                    degreesType = degreesType,
                    degreesStep = degreesStep
                )
            }
            if (showCenter) {
                drawCenter(
                    canvas = canvas,
                    clockSize = clockSize,
                    paint = paints.center,
                    centerInnerColor = centerInnerColor,
                    centerOuterColor = centerOuterColor
                )
            }
            drawNeedles(
                canvas = canvas,
                clockSize = clockSize,
                paint = paints.needle,
                needleHoursColor = needleHoursColor,
                needleMinutesColor = needleMinutesColor,
                needleSecondsColor = needleSecondsColor,
                showSecondsHand = showSecondsHand,
                second = second,
                hour = hour,
                minute = minute,
                labelBoxes = paints.labelBoxes,
                minutesValuesInner = minutesValuesInner
            )
            canvas.restore()
        }
    }
}

private class ClockViewPaints {
    val hoursText = TextPaint(Paint.ANTI_ALIAS_FLAG)
    val minutesText = TextPaint(Paint.ANTI_ALIAS_FLAG)
    val degrees = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        strokeCap = Paint.Cap.ROUND
    }
    val center = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
    }
    val needle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
    }
    val textBounds = Rect()
    // Where this frame's numerals were drawn; the seconds hand passes under them.
    val labelBoxes = LabelBoxes()
}

/** The numeral boxes of one frame. The RectFs are reused, so drawing allocates nothing. */
private class LabelBoxes : Iterable<RectF> {
    private val boxes = ArrayList<RectF>()
    private var count = 0

    fun clear() {
        count = 0
    }

    fun add(left: Float, top: Float, right: Float, bottom: Float): RectF {
        if (count == boxes.size) {
            boxes += RectF()
        }
        return boxes[count++].apply { set(left, top, right, bottom) }
    }

    override fun iterator(): Iterator<RectF> = boxes.subList(0, count).iterator()
}

// The canvas origin is the dial centre; y grows downwards.
private fun dialRadius(clockSize: Float) = clockSize * (1 - DEFAULT_BORDER_THICKNESS) / 2

private fun drawHoursValues(
    canvas: Canvas,
    clockSize: Float,
    paints: ClockViewPaints,
    digitsColor: Int,
    digitsFont: Typeface,
    showDegrees: Boolean,
    digitStyle: DigitStyle,
    digitDisposition: DigitDisposition,
    digitStep: DigitStep
) {
    val textPaint = paints.hoursText
    textPaint.typeface = digitsFont
    val degreeSpace = if (showDegrees) DEFAULT_DEGREE_STROKE_WIDTH + 0.06F else 0F
    val text = clockSize / 2 - clockSize * DEFAULT_HOURS_VALUES_TEXT_SIZE - clockSize * degreeSpace
    var i = FULL_ANGLE
    while (i > 0) {
        val value = i / 30
        val formatted = when (digitStyle) {
            DigitStyle.ROMAN -> toRoman(value)
            else -> value.toString()
        }
        // Setting the colour resets the alpha left over from the previous numeral.
        textPaint.color = digitsColor
        if (digitDisposition == DigitDisposition.ALTERNATE && i % REGULAR_ANGLE != 0) {
            textPaint.textSize = clockSize * (DEFAULT_HOURS_VALUES_TEXT_SIZE - 0.03F)
            textPaint.alpha = CUSTOM_ALPHA
        } else {
            textPaint.textSize = clockSize * DEFAULT_HOURS_VALUES_TEXT_SIZE
            textPaint.alpha = FULL_ALPHA
        }
        val angle = Math.toRadians((REGULAR_ANGLE - i).toDouble())
        drawCenteredText(
            canvas = canvas,
            text = formatted,
            x = text * cos(angle).toFloat(),
            y = -text * sin(angle).toFloat(),
            paint = textPaint,
            bounds = paints.textBounds,
            boxes = paints.labelBoxes
        )
        i -= digitStep.value.toInt()
    }
}

/** Draws 15, 30 and 45 and returns how close to the centre they reach. */
private fun drawMinutesValues(
    canvas: Canvas,
    clockSize: Float,
    paints: ClockViewPaints,
    minutesProgressColor: Int,
    digitsFont: Typeface,
    minutesValuesFactor: Float,
    digitStyle: DigitStyle
): Float {
    var inner = Float.MAX_VALUE
    val textPaint = paints.minutesText.apply {
        color = minutesProgressColor
        typeface = digitsFont
        textSize = clockSize * MINUTES_TEXT_SIZE
    }
    val text = clockSize / 2 - (1 - minutesValuesFactor - 2 * DEFAULT_BORDER_THICKNESS - MINUTES_TEXT_SIZE) * dialRadius(clockSize)
    var i = 0
    while (i < FULL_ANGLE) {
        val value = i / 6
        if (value > 0) {
            val formatted = when (digitStyle) {
                DigitStyle.ROMAN -> toRoman(value)
                else -> value.toString()
            }
            val angle = Math.toRadians((REGULAR_ANGLE - i).toDouble())
            val box = drawCenteredText(
                canvas = canvas,
                text = formatted,
                x = text * cos(angle).toFloat(),
                y = -text * sin(angle).toFloat(),
                paint = textPaint,
                bounds = paints.textBounds,
                boxes = paints.labelBoxes
            )
            inner = min(inner, box.distanceToCentre())
        }
        i += QUARTER_DEGREE_STEPS
    }
    return inner
}

// Centres the glyphs on (x, y). The old offset of width/length and height/length put
// one-character numerals (3, 6, 9, V...) half a glyph left of and below their anchor.
private fun drawCenteredText(
    canvas: Canvas,
    text: String,
    x: Float,
    y: Float,
    paint: Paint,
    bounds: Rect,
    boxes: LabelBoxes
): RectF {
    paint.getTextBounds(text, 0, text.length, bounds)
    canvas.drawText(text, x - bounds.exactCenterX(), y - bounds.exactCenterY(), paint)
    val halfWidth = bounds.width() / 2F
    val halfHeight = bounds.height() / 2F
    return boxes.add(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight)
}

// Distance from the dial centre (the origin) to the nearest point of the box.
private fun RectF.distanceToCentre() = hypot(0F.coerceIn(left, right), 0F.coerceIn(top, bottom))

private fun drawDegrees(
    canvas: Canvas,
    clockSize: Float,
    paint: Paint,
    degreesColor: Int,
    degreesType: DegreeType,
    degreesStep: DegreesStep
) {
    paint.strokeWidth = clockSize * DEFAULT_DEGREE_STROKE_WIDTH
    val markSize = clockSize * DEFAULT_DEGREE_STROKE_WIDTH
    val rPadded = clockSize / 2 - clockSize * (DEFAULT_BORDER_THICKNESS + 0.03F)
    val rEnd = clockSize / 2 - clockSize * (DEFAULT_BORDER_THICKNESS + 0.06F)
    var i = 0
    while (i < FULL_ANGLE) {
        paint.color = degreesColor
        paint.alpha = if (i % REGULAR_ANGLE != 0 && i % 15 != 0) CUSTOM_ALPHA else FULL_ALPHA
        val angle = Math.toRadians(i.toDouble())
        val startX = rPadded * cos(angle).toFloat()
        val startY = -rPadded * sin(angle).toFloat()
        val stopX = rEnd * cos(angle).toFloat()
        val stopY = -rEnd * sin(angle).toFloat()
        when (degreesType) {
            DegreeType.CIRCLE -> canvas.drawCircle(
                stopX,
                stopY,
                markSize,
                paint
            )
            // Centred on its point; it used to hang down and right of it.
            DegreeType.SQUARE -> canvas.drawRect(
                startX - markSize / 2,
                startY - markSize / 2,
                startX + markSize / 2,
                startY + markSize / 2,
                paint
            )
            else -> canvas.drawLine(
                startX,
                startY,
                stopX,
                stopY,
                paint
            )
        }
        i += degreesStep.value.toInt()
    }
}

private fun drawCenter(
    canvas: Canvas,
    clockSize: Float,
    paint: Paint,
    centerInnerColor: Int,
    centerOuterColor: Int
) {
    paint.apply {
        style = Paint.Style.FILL
        color = centerInnerColor
    }
    canvas.drawCircle(0F, 0F, clockSize * 0.015F, paint) // center
    paint.apply {
        style = Paint.Style.STROKE
        color = centerOuterColor
        strokeWidth = clockSize * 0.008F
    }
    canvas.drawCircle(0F, 0F, clockSize * 0.02F, paint) // border
}

private fun drawNeedles(
    canvas: Canvas,
    clockSize: Float,
    paint: Paint,
    needleHoursColor: Int,
    needleMinutesColor: Int,
    needleSecondsColor: Int,
    showSecondsHand: Boolean,
    second: Int,
    hour: Int,
    minute: Int,
    labelBoxes: LabelBoxes,
    minutesValuesInner: Float?
) {
    val radius = dialRadius(clockSize)
    val needleStart = radius * DEFAULT_NEEDLE_START_SPACE
    val hoursTextSize = clockSize * DEFAULT_HOURS_VALUES_TEXT_SIZE
    val degreesSpace = clockSize * (DEFAULT_BORDER_THICKNESS + 0.06F)
    val needleMaxLength = radius * NEEDLE_LENGTH_FACTOR - 2 * (degreesSpace + hoursTextSize)

    paint.strokeWidth = clockSize * DEFAULT_NEEDLE_STROKE_WIDTH

    val margin = clockSize * LABEL_MARGIN

    // hours needle: it ended on the 15, 30 and 45, and now stops short of them
    val hoursDegree = (hour + minute / 60F) * 30 // 30 = 360 / 12
    paint.color = needleHoursColor
    val hoursLength = min(needleMaxLength * 0.6F, (minutesValuesInner ?: Float.MAX_VALUE) - 2 * margin)
    drawNeedle(canvas, hoursDegree, needleStart, hoursLength.coerceAtLeast(needleStart), paint)

    // minutes needle: drawn whole, so it covers 15, 30 or 45 only while it points at it. Cut
    // around the numeral it looked no longer than the hour hand, with a loose tip beyond.
    val minutesDegree = (minute + second / 60F) * 6
    paint.color = needleMinutesColor
    drawNeedle(canvas, minutesDegree, needleStart, needleMaxLength * 0.8F, paint)

    // seconds needle: passes under the numerals it crosses (it covered 15, 30, 45 and 6 every
    // minute), so they stay readable
    if (showSecondsHand) {
        val secondsDegree = (second * 6).toFloat()
        paint.strokeWidth = clockSize * 0.008F
        paint.color = needleSecondsColor
        drawNeedle(canvas, secondsDegree, needleStart, needleMaxLength, paint, labelBoxes, margin)
    }
}

// degree is clockwise from 12 o'clock.
private fun drawNeedle(
    canvas: Canvas,
    degree: Float,
    start: Float,
    stop: Float,
    paint: Paint,
    passUnder: Iterable<RectF> = emptyList(),
    margin: Float = 0F
) {
    val angle = Math.toRadians((-REGULAR_ANGLE + degree).toDouble())
    val cosA = cos(angle).toFloat()
    val sinA = sin(angle).toFloat()
    val x0 = start * cosA
    val y0 = start * sinA
    val x1 = stop * cosA
    val y1 = stop * sinA
    // Only the numerals the hand really crosses are cut out, with a margin around them. Cutting
    // every numeral's margin shaved the hand where it only passed beside one.
    var isClipped = false
    for (box in passUnder) {
        if (segmentCrosses(x0, y0, x1, y1, box, paint.strokeWidth / 2F)) {
            if (!isClipped) {
                canvas.save()
                isClipped = true
            }
            canvas.clipOutRect(box.left - margin, box.top - margin, box.right + margin, box.bottom + margin)
        }
    }
    canvas.drawLine(x0, y0, x1, y1, paint)
    if (isClipped) {
        canvas.restore()
    }
}

// Whether the line from (x0, y0) to (x1, y1), [grow] wide on each side, touches [box]
// (Liang-Barsky clipping against the grown box).
private fun segmentCrosses(x0: Float, y0: Float, x1: Float, y1: Float, box: RectF, grow: Float): Boolean {
    val dx = x1 - x0
    val dy = y1 - y0
    var tMin = 0F
    var tMax = 1F
    fun clip(p: Float, q: Float): Boolean {
        if (p == 0F) {
            return q >= 0F
        }
        val t = q / p
        if (p < 0F) {
            if (t > tMax) return false
            if (t > tMin) tMin = t
        } else {
            if (t < tMin) return false
            if (t < tMax) tMax = t
        }
        return true
    }
    return clip(-dx, x0 - (box.left - grow)) && clip(dx, box.right + grow - x0) &&
        clip(-dy, y0 - (box.top - grow)) && clip(dy, box.bottom + grow - y0)
}

// Also handles 40 and 50: the old version wrote 45 as "XXXXV".
private fun toRoman(number: Int): String {
    var rest = number
    val result = StringBuilder()
    for ((value, symbol) in romanNumerals) {
        while (rest >= value) {
            result.append(symbol)
            rest -= value
        }
    }
    return result.toString()
}

private const val DEFAULT_BORDER_THICKNESS = 0.015F
private const val DEFAULT_HOURS_VALUES_TEXT_SIZE = 0.08F
private const val DEFAULT_DEGREE_STROKE_WIDTH = 0.010F
private const val FULL_ANGLE = 360
private const val REGULAR_ANGLE = 90
private const val FULL_ALPHA = 255
private const val CUSTOM_ALPHA = 140
private const val QUARTER_DEGREE_STEPS = 90
private const val MINUTES_TEXT_SIZE = 0.050F
private const val DEFAULT_NEEDLE_STROKE_WIDTH = 0.015F
private const val DEFAULT_NEEDLE_START_SPACE = 0.05F
private const val NEEDLE_LENGTH_FACTOR = 1.3F
// Space kept free around a numeral the seconds hand passes under, and between the hour hand
// and 15, 30, 45; in clock sizes.
private const val LABEL_MARGIN = 0.012F

private val romanNumerals = listOf(
    50 to "L",
    40 to "XL",
    10 to "X",
    9 to "IX",
    5 to "V",
    4 to "IV",
    1 to "I"
)

enum class DegreesStep(val value: String, val titleId: Int) {
    QUARTER("90", R.string.degree_step_quarter),
    FULL("6", R.string.degree_step_full),
    TWELVE("30", R.string.degree_step_twelve);

    companion object {
        fun toMap(context: Context): Map<String, String> {
            return values().associate {
                it.value to context.getString(it.titleId)
            }
        }

        fun getDefault(): DegreesStep {
            return FULL
        }

        fun getDefaultId(): String {
            return getDefault().value
        }

        fun getById(id: String): DegreesStep {
            val item = values().filter {
                it.value == id
            }
            return item[0]
        }
    }
}

enum class DegreeType(val value: String, val titleId: Int) {
    LINE("line", R.string.degree_type_line),
    CIRCLE("circle", R.string.degree_type_circle),
    SQUARE("square", R.string.degree_type_square);

    companion object {
        fun toMap(context: Context): Map<String, String> {
            return values().associate {
                it.value to context.getString(it.titleId)
            }
        }

        fun getDefault(): DegreeType {
            return LINE
        }

        fun getDefaultId(): String {
            return getDefault().value
        }

        fun getById(id: String): DegreeType {
            val item = values().filter {
                it.value == id
            }
            return item[0]
        }
    }
}

enum class DigitDisposition(val value: String, val titleId: Int) {
    REGULAR("regular", R.string.digit_disposition_regular),
    ALTERNATE("alternate", R.string.digit_disposition_alternate);

    companion object {
        fun toMap(context: Context): Map<String, String> {
            return values().associate {
                it.value to context.getString(it.titleId)
            }
        }

        fun getDefault(): DigitDisposition {
            return REGULAR
        }

        fun getDefaultId(): String {
            return getDefault().value
        }

        fun getById(id: String): DigitDisposition {
            val item = values().filter {
                it.value == id
            }
            return item[0]
        }
    }
}

enum class DigitStep(val value: String, val titleId: Int) {
    QUARTER("90", R.string.digit_step_quarter),
    FULL("30", R.string.digit_step_full);

    companion object {
        fun toMap(context: Context): Map<String, String> {
            return values().associate {
                it.value to context.getString(it.titleId)
            }
        }

        fun getDefault(): DigitStep {
            return QUARTER
        }

        fun getDefaultId(): String {
            return getDefault().value
        }

        fun getById(id: String): DigitStep {
            val item = values().filter {
                it.value == id
            }
            return item[0]
        }
    }
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

const val DEFAULT_SHOW_HOURS_CV = true
const val DEFAULT_SHOW_MINUTES_CV = true
const val DEFAULT_SHOW_DEGREES_CV = true
const val DEFAULT_SHOW_CENTER_CV = true
const val DEFAULT_SHOW_SECOND_HAND_CV = true