package com.smsoft.smartdisplay.ui.composable.clock.clockview

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ClockView(
    modifier: Modifier = Modifier
        .fillMaxSize(),
    viewModel: ClockViewModel,
    scale: Float,
    primaryColor: Color,
    secondaryColor: Color,
    hour: Int,
    minute: Int,
    second: Int
) {
    val primaryColor = getColor(primaryColor)
    val secondaryColor = getColor(secondaryColor)

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

    OnDraw(
        modifier = modifier,
        scale = scale,
        showHoursValues = showHoursValues,
        showMinutesValues = showMinutesValues,
        digitStyle = digitStyle,
        digitsColor = secondaryColor,
        digitsFont = ResourcesCompat.getFont(context, font)!!,
        showDegrees = showDegrees,
        digitDisposition = digitDisposition,
        digitStep = digitStep,
        minutesProgressColor = primaryColor,
        minutesValuesFactor = 0.3F,
        degreesColor = primaryColor,
        degreesType = degreesType,
        degreesStep = degreesStep,
        showCenter = showCenter,
        centerInnerColor = primaryColor,
        centerOuterColor = primaryColor,
        needleHoursColor = primaryColor,
        needleMinutesColor = primaryColor,
        needleSecondsColor = primaryColor,
        showSecondsHand = showSecondsHand,
        second = second,
        hour = hour,
        minute = minute
    )
}

@Composable
fun OnDraw(
    modifier: Modifier,
    scale: Float,
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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            Canvas(
                modifier = modifier
                    .fillMaxSize()
                    .absolutePadding(
                        left = if (scale == 1F) 220.dp else 0.dp, //60.dp for 480 x 480
                        top = 20.dp,
                        bottom = 20.dp
                    )
            ) {
                onPreDraw(
                    width = size.width.toInt(),
                    height = size.height.toInt()
                )
                drawIntoCanvas {
                    val canvas = it.nativeCanvas
                    if (showHoursValues) {
                        drawHoursValues(
                            canvas = canvas,
                            digitsColor = digitsColor,
                            digitsFont = digitsFont,
                            showDegrees = showDegrees,
                            digitStyle = digitStyle,
                            digitDisposition = digitDisposition,
                            digitStep = digitStep
                        )
                    }
                    if (showMinutesValues) {
                        drawMinutesValues(
                            canvas = canvas,
                            minutesProgressColor = minutesProgressColor,
                            digitsFont = digitsFont,
                            minutesValuesFactor = minutesValuesFactor,
                            digitStyle = digitStyle
                        )
                    }
                    if (showDegrees) {
                        drawDegrees(
                            canvas = canvas,
                            degreesColor = degreesColor,
                            degreesType = degreesType,
                            degreesStep = degreesStep
                        )
                    }
                    if (showCenter) {
                        drawCenter(
                            canvas = canvas,
                            centerInnerColor = centerInnerColor,
                            centerOuterColor = centerOuterColor
                        )
                    }
                    drawNeedles(
                        canvas = canvas,
                        needleHoursColor = needleHoursColor,
                        needleMinutesColor = needleMinutesColor,
                        needleSecondsColor = needleSecondsColor,
                        showSecondsHand = showSecondsHand,
                        second = second,
                        hour = hour,
                        minute = minute
                    )
                }
            }
    }
}

private fun onPreDraw(
    width: Int,
    height: Int
) {
    clockSize = min(height, width)
    centerX = clockSize / 2
    centerY = clockSize / 2
    radius = (clockSize * (1 - DEFAULT_BORDER_THICKNESS) / 2).toInt()
    defaultThickness = clockSize * DEFAULT_BORDER_THICKNESS
    defaultRectF = RectF(defaultThickness, defaultThickness, clockSize - defaultThickness, clockSize - defaultThickness)
}

private fun drawHoursValues(
    canvas: Canvas,
    digitsColor: Int,
    digitsFont: Typeface,
    showDegrees: Boolean,
    digitStyle: DigitStyle,
    digitDisposition: DigitDisposition,
    digitStep: DigitStep
) {
    val textPaint = TextPaint().apply{
        flags = Paint.ANTI_ALIAS_FLAG
        isAntiAlias = true
        color = digitsColor
        typeface = digitsFont
        textSize = clockSize * DEFAULT_HOURS_VALUES_TEXT_SIZE
    }
    val degreeSpace = if (showDegrees) DEFAULT_DEGREE_STROKE_WIDTH + 0.06F else 0F
    val text = (centerX - clockSize * DEFAULT_HOURS_VALUES_TEXT_SIZE - clockSize * degreeSpace).toInt()
    var i = FULL_ANGLE
    val rect = Rect()
    while (i > 0) {
        val value = i / 30
        val formatted = when (digitStyle) {
            DigitStyle.ROMAN -> toRoman(value)
            else -> value.toString()
        }
        if (digitDisposition == DigitDisposition.ALTERNATE) {
            if (i % REGULAR_ANGLE == 0) {
                textPaint.textSize = clockSize * DEFAULT_HOURS_VALUES_TEXT_SIZE
                textPaint.alpha = FULL_ALPHA
            } else {
                textPaint.textSize = clockSize * (DEFAULT_HOURS_VALUES_TEXT_SIZE - 0.03F)
                textPaint.alpha = CUSTOM_ALPHA
            }
        } else {
            textPaint.textSize = clockSize * DEFAULT_HOURS_VALUES_TEXT_SIZE
            textPaint.alpha = FULL_ALPHA
        }
        val textX = (centerX + text * cos(Math.toRadians((REGULAR_ANGLE - i).toDouble()))).toInt()
        val textY = (centerX - text * sin(Math.toRadians((REGULAR_ANGLE - i).toDouble()))).toInt()

        textPaint.getTextBounds(formatted, 0, formatted.length, rect)
        canvas.drawText(
            formatted,
            textX - rect.width().toFloat() / formatted.length,
            textY + rect.height().toFloat() / formatted.length,
            textPaint
        )
        i -= digitStep.value.toInt()
    }
}

private fun drawMinutesValues(
    canvas: Canvas,
    minutesProgressColor: Int,
    digitsFont: Typeface,
    minutesValuesFactor: Float,
    digitStyle: DigitStyle
) {
    val textPaint = TextPaint().apply{
        color = minutesProgressColor
        typeface = digitsFont
        textSize = clockSize * MINUTES_TEXT_SIZE
    }
    val text = (centerX - (1 - minutesValuesFactor - 2 * DEFAULT_BORDER_THICKNESS - MINUTES_TEXT_SIZE) * radius).toInt()
    var i = 0
    val rect = Rect()
    while (i < FULL_ANGLE) {
        val value = i / 6
        if (value > 0) {
            val formatted = when (digitStyle) {
                DigitStyle.ROMAN -> toRoman(value)
                else -> value.toString()
            }
            val textX = (centerX + text * cos(Math.toRadians((REGULAR_ANGLE - i).toDouble()))).toInt()
            val textY = (centerX - text * sin(Math.toRadians((REGULAR_ANGLE - i).toDouble()))).toInt()
            textPaint.getTextBounds(formatted, 0, formatted.length, rect)
            canvas.drawText(
                formatted,
                textX - rect.width() * 1f / formatted.length,
                textY + rect.height() * 1f / formatted.length,
                textPaint
            )
        }
        i += QUARTER_DEGREE_STEPS
    }
}

private fun drawDegrees(
    canvas: Canvas,
    degreesColor: Int,
    degreesType: DegreeType,
    degreesStep: DegreesStep
) {
    val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL_AND_STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = clockSize * DEFAULT_DEGREE_STROKE_WIDTH
        color = degreesColor
    }
    val rPadded = centerX - (clockSize * (DEFAULT_BORDER_THICKNESS + 0.03F)).toInt()
    val rEnd = centerX - (clockSize * (DEFAULT_BORDER_THICKNESS + 0.06F)).toInt()
    var i = 0
    while (i < FULL_ANGLE) {
        if (i % REGULAR_ANGLE != 0 && i % 15 != 0) paint.alpha = CUSTOM_ALPHA else {
            paint.alpha = FULL_ALPHA
        }
        val startX = (centerX + rPadded * cos(Math.toRadians(i.toDouble()))).toInt()
        val startY = (centerX - rPadded * sin(Math.toRadians(i.toDouble()))).toInt()
        val stopX = (centerX + rEnd * cos(Math.toRadians(i.toDouble()))).toInt()
        val stopY = (centerX - rEnd * sin(Math.toRadians(i.toDouble()))).toInt()
        when (degreesType) {
            DegreeType.CIRCLE -> canvas.drawCircle(
                stopX.toFloat(),
                stopY.toFloat(),
                clockSize * DEFAULT_DEGREE_STROKE_WIDTH,
                paint
            )
            DegreeType.SQUARE -> canvas.drawRect(
                startX.toFloat(),
                startY.toFloat(),
                startX + clockSize * DEFAULT_DEGREE_STROKE_WIDTH,
                startY + clockSize * DEFAULT_DEGREE_STROKE_WIDTH,
                paint
            )
            else -> canvas.drawLine(
                startX.toFloat(),
                startY.toFloat(),
                stopX.toFloat(),
                stopY.toFloat(),
                paint
            )
        }
        i += degreesStep.value.toInt()
    }
}

private fun drawCenter(
    canvas: Canvas,
    centerInnerColor: Int,
    centerOuterColor: Int
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.ROUND
        color = centerInnerColor
    }
    canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), clockSize * 0.015F, paint) // center
    paint.apply {
        style = Paint.Style.STROKE
        color = centerOuterColor
        strokeWidth = clockSize * 0.008F
    }
    canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), clockSize * 0.02F, paint) // border
}

private fun drawNeedles(
    canvas: Canvas,
    needleHoursColor: Int,
    needleMinutesColor: Int,
    needleSecondsColor: Int,
    showSecondsHand: Boolean,
    second: Int,
    hour: Int,
    minute: Int
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
        strokeCap = Paint.Cap.ROUND
        strokeWidth = clockSize * DEFAULT_NEEDLE_STROKE_WIDTH
    }
    val needleStartSpace = DEFAULT_NEEDLE_START_SPACE
    val hoursTextSize = clockSize * DEFAULT_HOURS_VALUES_TEXT_SIZE
    val degreesSpace = clockSize * (DEFAULT_BORDER_THICKNESS + 0.06F)
    val needleMaxLength = radius * NEEDLE_LENGTH_FACTOR - 2 * (degreesSpace + hoursTextSize)

    // draw seconds needle
    val secondsDegree = (second * 6).toFloat()
    // draw hours needle
    val hoursDegree = (hour + minute / 60F) * 30 // 30 = 360 / 12
    val startHoursX = (centerX + radius * needleStartSpace * cos(Math.toRadians((-REGULAR_ANGLE + hoursDegree).toDouble()))).toFloat()
    val stopHoursX = (centerX + needleMaxLength * 0.6F * cos(Math.toRadians((-REGULAR_ANGLE + hoursDegree).toDouble()))).toFloat()
    val startHoursY = (centerY + radius * needleStartSpace * sin(Math.toRadians((-REGULAR_ANGLE + hoursDegree).toDouble()))).toFloat()
    val stopHoursY = (centerY + needleMaxLength * 0.6F * sin(Math.toRadians((-REGULAR_ANGLE + hoursDegree).toDouble()))).toFloat()

    // draw minutes needle
    val minutesDegree = (minute + second / 60F) * 6
    val startMinutesX = (centerX + radius * needleStartSpace * cos(Math.toRadians((-REGULAR_ANGLE + minutesDegree).toDouble()))).toFloat()
    val stopMinutesX = (centerX + needleMaxLength * 0.8F * cos(Math.toRadians((-REGULAR_ANGLE + minutesDegree).toDouble()))).toFloat()
    val startMinutesY = (centerY + (radius - clockSize * DEFAULT_BORDER_THICKNESS) * needleStartSpace * sin(Math.toRadians((-REGULAR_ANGLE + minutesDegree).toDouble()))).toFloat()
    val stopMinutesY = (centerY + needleMaxLength * 0.8F * sin(Math.toRadians((-REGULAR_ANGLE + minutesDegree).toDouble()))).toFloat()

    // hours needle
    paint.color = needleHoursColor
    canvas.drawLine(
        startHoursX,
        startHoursY,
        stopHoursX,
        stopHoursY,
        paint
    )

    // minutes needle
    paint.color = needleMinutesColor
    canvas.drawLine(
        startMinutesX,
        startMinutesY,
        stopMinutesX,
        stopMinutesY,
        paint
    )

    // seconds needle
    paint.strokeWidth = clockSize * 0.008F
    paint.color = needleSecondsColor

    if (showSecondsHand) {
        val startSecondsX = (centerX + radius * needleStartSpace * cos(Math.toRadians((-REGULAR_ANGLE + secondsDegree).toDouble()))).toFloat()
        val stopSecondsX = (centerX + needleMaxLength * cos(Math.toRadians((-REGULAR_ANGLE + secondsDegree).toDouble()))).toFloat()
        val startSecondsY = (centerY + radius * needleStartSpace * sin(Math.toRadians((-REGULAR_ANGLE + secondsDegree).toDouble()))).toFloat()
        val stopSecondsY = (centerY + needleMaxLength * sin(Math.toRadians((-REGULAR_ANGLE + secondsDegree).toDouble()))).toFloat()

        canvas.drawLine(
            startSecondsX,
            startSecondsY,
            stopSecondsX,
            stopSecondsY,
            paint
        )
    }
}

private fun toRoman(number: Int): String {
    return if (number < 11) {
        romanMap[number]!!
    } else romanMap[10] + toRoman(number - 10)
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

private var clockSize = 0
private var centerX = 0
private var centerY = 0
private var radius = 0
private var defaultThickness = 0F
private lateinit var defaultRectF: RectF
private val romanMap = sortedMapOf(
    10 to "X",
    9 to "IX",
    8 to "VIII",
    7 to "VII",
    6 to "VI",
    5 to "V",
    4 to "IV",
    3 to "III",
    2 to "II",
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