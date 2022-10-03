package com.arbelkilani.clock

import android.graphics.*
import android.text.*
import android.text.style.RelativeSizeSpan
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ClockView(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background),
    color: ColorFilter = ColorFilter.tint(color = MaterialTheme.colors.primary),
    hour: Int,
    minute: Int,
    second: Int
) {
    val clockColor = Color.WHITE

    initRomanDigits()

    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    
    onPreDraw(
        width = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }.toInt(),
        height = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }.toInt()
    )

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        OnDraw(
            modifier = modifier
                .padding(
                    start = 200.dp
                ),
            clockType = ClockType.ANALOG,
            showBorder = true,
            borderColor = clockColor,
            showHoursValues = true,
            digitsColor = clockColor,
            digitsFont = ResourcesCompat.getFont(context, R.font.proxima_nova_thin)!!,
            showDegrees = true,
            digitType = DigitType.ARABIC,
            digitDisposition = DigitDisposition.ALTERNATE,
            digitStep = DigitStep.FULL,
            showMinutesValues = true,
            minutesProgressColor = clockColor,
            minutesValuesFactor = 0.2F,
            degreesColor = clockColor,
            degreesType = DegreeType.LINE,
            degreesStep = DegreesStep.FULL,
            showCenter = true,
            centerInnerColor = clockColor,
            centerOuterColor = clockColor,
            needleHoursColor = clockColor,
            needleMinutesColor = clockColor,
            needleSecondsColor = clockColor,
            showSecondsNeedle = true,
            numericShowSeconds = true,
            numericFormat = NumericFormat.HOUR_24,
            isAM = false,
            second = second,
            hour = hour,
            minute = minute
        )
    }
}

@Composable
fun OnDraw(
    modifier: Modifier,
    clockType: ClockType,
    showBorder: Boolean,
    borderColor: Int,
    showHoursValues: Boolean,
    digitsColor: Int,
    digitsFont: Typeface,
    showDegrees: Boolean,
    digitType: DigitType,
    digitDisposition: DigitDisposition,
    digitStep: DigitStep,
    showMinutesValues: Boolean,
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
    showSecondsNeedle: Boolean,
    numericShowSeconds: Boolean,
    numericFormat: NumericFormat,
    isAM: Boolean,
    second: Int,
    hour: Int,
    minute: Int
) {
    Canvas(
        modifier = modifier,
    ) {
        drawIntoCanvas {
            val canvas = it.nativeCanvas
            when (clockType) {
                ClockType.ANALOG -> drawAnalogClock(
                    canvas = canvas,
                    showBorder = showBorder,
                    borderColor = borderColor,
                    showHoursValues = showHoursValues,
                    digitsColor = digitsColor,
                    digitsFont = digitsFont,
                    showDegrees = showDegrees,
                    digitType = digitType,
                    digitDisposition = digitDisposition,
                    digitStep = digitStep,
                    showMinutesValues = showMinutesValues,
                    minutesProgressColor = minutesProgressColor,
                    minutesValuesFactor = minutesValuesFactor,
                    degreesColor = degreesColor,
                    degreesType = degreesType,
                    degreesStep = degreesStep,
                    showCenter = showCenter,
                    centerInnerColor = centerInnerColor,
                    centerOuterColor = centerOuterColor,
                    needleHoursColor = needleHoursColor,
                    needleMinutesColor = needleMinutesColor,
                    needleSecondsColor = needleSecondsColor,
                    showSecondsNeedle = showSecondsNeedle,
                    second = second,
                    hour = hour,
                    minute = minute
                )
                ClockType.DIGITAL -> drawDigitalClock(
                    canvas = canvas,
                    digitsFont = digitsFont,
                    digitsColor = digitsColor,
                    numericShowSeconds = numericShowSeconds,
                    numericFormat = numericFormat,
                    isAM = isAM,
                    hour = hour,
                    minute = minute,
                    second = second,
                )
            }
        }
    }
}

private fun drawDigitalClock(
    canvas: Canvas,
    digitsFont: Typeface,
    digitsColor: Int,
    numericShowSeconds: Boolean,
    numericFormat: NumericFormat,
    isAM: Boolean,
    hour: Int,
    minute: Int,
    second: Int,
) {
    val textPaint = TextPaint().apply{
        isAntiAlias = true
        typeface = digitsFont
        textSize = size * 0.22F
        color = digitsColor
    }
    val spannableString = SpannableStringBuilder()

    val hourStr = String.format(Locale.getDefault(), "%02d", hour)
    val minuteStr = String.format(Locale.getDefault(), "%02d", minute)

    if (numericShowSeconds) {
        val secondStr = String.format(Locale.getDefault(), "%02d", second)
        if (numericFormat == NumericFormat.HOUR_12) {
            spannableString.apply {
                append(hourStr)
                append(":")
                append(minuteStr)
                append(".")
                append(secondStr)
                append(if (isAM) "AM" else "PM")
                setSpan(RelativeSizeSpan(0.3F), toString().length - 2, toString().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // se superscript percent
            }
        } else {
            spannableString.apply {
                append(hourStr)
                append(":")
                append(minuteStr)
                append(".")
                append(secondStr)
            }
        }
    } else {
        if (numericFormat == NumericFormat.HOUR_12) {
            spannableString.apply {
                append(hourStr)
                append(":")
                append(minuteStr)
                append(if (isAM) "AM" else "PM")
                setSpan(RelativeSizeSpan(0.4F), toString().length - 2, toString().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // se superscript percent
            }
        } else {
            spannableString.apply {
                append(hourStr)
                append(":")
                append(minuteStr)
            }
        }
    }
    val layout = StaticLayout(
        spannableString,
        textPaint,
        canvas.width,
        Layout.Alignment.ALIGN_CENTER,
        1F,
        1F,
        true
    )
    canvas.translate(centerX - layout.width / 2F, centerY - layout.height / 2F)
    layout.draw(canvas)
}

private fun onPreDraw(
    width: Int,
    height: Int
) {
    size = min(height, width)
    centerX = size / 2
    centerY = size / 2
    radius = (size * (1 - DEFAULT_BORDER_THICKNESS) / 2).toInt()
    defaultThickness = size * DEFAULT_BORDER_THICKNESS
    defaultRectF = RectF(defaultThickness, defaultThickness, size - defaultThickness, size - defaultThickness)
}

private fun drawAnalogClock(
    canvas: NativeCanvas,
    showBorder: Boolean,
    borderColor: Int,
    showHoursValues: Boolean,
    digitsColor: Int,
    digitsFont: Typeface,
    showDegrees: Boolean,
    digitType: DigitType,
    digitDisposition: DigitDisposition,
    digitStep: DigitStep,
    showMinutesValues: Boolean,
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
    showSecondsNeedle: Boolean,
    second: Int,
    hour: Int,
    minute: Int
) {
    if (showBorder) {
        drawBorder(
            canvas = canvas,
            borderColor = borderColor
        )
    }
    if (showHoursValues) {
        drawHoursValues(
            canvas = canvas,
            digitsColor = digitsColor,
            digitsFont = digitsFont,
            showDegrees = showDegrees,
            digitType = digitType,
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
            digitType = digitType
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
        showBorder = showBorder,
        showHoursValues = showHoursValues,
        showDegrees = showDegrees,
        needleHoursColor = needleHoursColor,
        needleMinutesColor = needleMinutesColor,
        needleSecondsColor = needleSecondsColor,
        showSecondsNeedle = showSecondsNeedle,
        second = second,
        hour = hour,
        minute = minute
    )
}

private fun drawBorder(
    canvas: NativeCanvas,
    borderColor: Int
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
        isAntiAlias = true
        color = borderColor
        style = Paint.Style.STROKE
        strokeWidth = size * DEFAULT_BORDER_THICKNESS
    }
    canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), radius.toFloat(), paint)
}

private fun drawHoursValues(
    canvas: Canvas,
    digitsColor: Int,
    digitsFont: Typeface,
    showDegrees: Boolean,
    digitType: DigitType,
    digitDisposition: DigitDisposition,
    digitStep: DigitStep
) {
    val textPaint = TextPaint().apply{
        flags = Paint.ANTI_ALIAS_FLAG
        isAntiAlias = true
        color = digitsColor
        typeface = digitsFont
        textSize = size * DEFAULT_HOURS_VALUES_TEXT_SIZE
    }
    var degreeSpace = 0F
    if (showDegrees) degreeSpace = DEFAULT_DEGREE_STROKE_WIDTH + 0.06F
    val text = (centerX - size * DEFAULT_HOURS_VALUES_TEXT_SIZE - size * degreeSpace).toInt()
    var i = FULL_ANGLE
    val rect = Rect()
    while (i > 0) {
        val value = i / 30
        val formatted = when (digitType) {
            DigitType.ROMAN -> toRoman(value)
            else -> value.toString()
        }
        if (digitDisposition == DigitDisposition.ALTERNATE) {
            if (i % REGULAR_ANGLE == 0) {
                textPaint.textSize = size * DEFAULT_HOURS_VALUES_TEXT_SIZE
                textPaint.alpha = FULL_ALPHA
            } else {
                textPaint.textSize = size * (DEFAULT_HOURS_VALUES_TEXT_SIZE - 0.03F)
                textPaint.alpha = CUSTOM_ALPHA
            }
        } else {
            textPaint.textSize = size * DEFAULT_HOURS_VALUES_TEXT_SIZE
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
        i -= digitStep.id
    }
}

private fun drawMinutesValues(
    canvas: Canvas,
    minutesProgressColor: Int,
    digitsFont: Typeface,
    minutesValuesFactor: Float,
    digitType: DigitType
) {
    val textPaint = TextPaint().apply{
        color = minutesProgressColor
        typeface = digitsFont
        textSize = size * MINUTES_TEXT_SIZE
    }
    val text = (centerX - (1 - minutesValuesFactor - 2 * DEFAULT_BORDER_THICKNESS - MINUTES_TEXT_SIZE) * radius).toInt()
    var i = 0
    val rect = Rect()
    while (i < FULL_ANGLE) {
        val value = i / 6
        if (value > 0) {
            val formatted = when (digitType) {
                DigitType.ROMAN -> toRoman(value)
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
        strokeWidth = size * DEFAULT_DEGREE_STROKE_WIDTH
        color = degreesColor
    }
    val rPadded = centerX - (size * (DEFAULT_BORDER_THICKNESS + 0.03F)).toInt()
    val rEnd = centerX - (size * (DEFAULT_BORDER_THICKNESS + 0.06F)).toInt()
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
                size * DEFAULT_DEGREE_STROKE_WIDTH,
                paint
            )
            DegreeType.SQUARE -> canvas.drawRect(
                startX.toFloat(),
                startY.toFloat(),
                startX + size * DEFAULT_DEGREE_STROKE_WIDTH,
                startY + size * DEFAULT_DEGREE_STROKE_WIDTH,
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
        i += degreesStep.id
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
    canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), size * 0.015F, paint) // center
    paint.apply {
        style = Paint.Style.STROKE
        color = centerOuterColor
        strokeWidth = size * 0.008F
    }
    canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), size * 0.02F, paint) // border
}

private fun drawNeedles(
    canvas: Canvas,
    showBorder: Boolean,
    showHoursValues: Boolean,
    showDegrees: Boolean,
    needleHoursColor: Int,
    needleMinutesColor: Int,
    needleSecondsColor: Int,
    showSecondsNeedle: Boolean,
    second: Int,
    hour: Int,
    minute: Int
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
        strokeCap = Paint.Cap.ROUND
        strokeWidth = size * DEFAULT_NEEDLE_STROKE_WIDTH
    }
    val needleStartSpace = DEFAULT_NEEDLE_START_SPACE
    var borderThickness = 0F
    var hoursTextSize = 0F
    var degreesSpace = 0F
    if (showBorder) borderThickness = size * DEFAULT_BORDER_THICKNESS
    if (showHoursValues) hoursTextSize = size * DEFAULT_HOURS_VALUES_TEXT_SIZE
    if (showDegrees) degreesSpace = size * (DEFAULT_BORDER_THICKNESS + 0.06F)
    val needleMaxLength = radius * NEEDLE_LENGTH_FACTOR - 2 * (degreesSpace + borderThickness + hoursTextSize)

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
    val stopMinutesX =
        (centerX + needleMaxLength * 0.8F * cos(Math.toRadians((-REGULAR_ANGLE + minutesDegree).toDouble()))).toFloat()
    val startMinutesY =
        (centerY + (radius - size * DEFAULT_BORDER_THICKNESS) * needleStartSpace * sin(
            Math.toRadians((-REGULAR_ANGLE + minutesDegree).toDouble())
        )).toFloat()
    val stopMinutesY =
        (centerY + needleMaxLength * 0.8F * sin(Math.toRadians((-REGULAR_ANGLE + minutesDegree).toDouble()))).toFloat()

    // hours needle
    paint.color = needleHoursColor
    canvas.drawLine(startHoursX, startHoursY, stopHoursX, stopHoursY, paint)

    // minutes needle
    paint.color = needleMinutesColor
    canvas.drawLine(startMinutesX, startMinutesY, stopMinutesX, stopMinutesY, paint)

    // seconds needle
    paint.strokeWidth = size * 0.008F
    paint.color = needleSecondsColor

    if (showSecondsNeedle) {
        val startSecondsX = (centerX + radius * needleStartSpace * cos(Math.toRadians((-REGULAR_ANGLE + secondsDegree).toDouble()))).toFloat()
        val stopSecondsX = (centerX + needleMaxLength * cos(Math.toRadians((-REGULAR_ANGLE + secondsDegree).toDouble()))).toFloat()
        val startSecondsY = (centerY + radius * needleStartSpace * sin(Math.toRadians((-REGULAR_ANGLE + secondsDegree).toDouble()))).toFloat()
        val stopSecondsY = (centerY + needleMaxLength * sin(Math.toRadians((-REGULAR_ANGLE + secondsDegree).toDouble()))).toFloat()

        canvas.drawLine(startSecondsX, startSecondsY, stopSecondsX, stopSecondsY, paint)
    }
}

private fun toRoman(number: Int): String {
    val l = romanMap.floorKey(number)
    return if (number == l) {
        romanMap[number]!!
    } else romanMap[l as Int] + toRoman(number - l)
}

private fun initRomanDigits() {
    romanMap[12] = "XII"
    romanMap[11] = "XI"
    romanMap[10] = "X"
    romanMap[9] = "IX"
    romanMap[8] = "VIII"
    romanMap[7] = "VII"
    romanMap[6] = "VI"
    romanMap[5] = "V"
    romanMap[4] = "IV"
    romanMap[3] = "III"
    romanMap[2] = "II"
    romanMap[1] = "I"
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
private const val NEEDLE_LENGTH_FACTOR = 0.9F

private var size = 0
private var centerX = 0
private var centerY = 0
private var radius = 0
private var defaultThickness = 0F
private lateinit var defaultRectF: RectF
private val romanMap = TreeMap<Int, String>()

enum class DegreesStep(val id: Int) {
    QUARTER(90), FULL(6), TWELVE(30);
}

enum class DegreeType(val id: Int) {
    LINE(0), CIRCLE(1), SQUARE(2);
}

enum class DigitDisposition(val id: Int) {
    REGULAR(-1), ALTERNATE(0);
}

enum class DigitStep(val id: Int) {
    QUARTER(90), FULL(30);
}

enum class DigitType(val id: Int) {
    ARABIC(-1), ROMAN(0);
}

enum class NumericFormat(val id: Int) {
    HOUR_12(0), HOUR_24(1);
}

enum class ClockType(val id: Int) {
    ANALOG(0), DIGITAL(1);
}