package com.github.chenglei1986.clock

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.util.TypedValue
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ClockView2(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background),
    color: ColorFilter = ColorFilter.tint(color = MaterialTheme.colors.primary),
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int
) {
    val configuration = LocalConfiguration.current

    val width = with(LocalDensity.current) {
        configuration.screenWidthDp.dp.toPx()
    }.toInt()
    val height = with(LocalDensity.current) {
        configuration.screenHeightDp.dp.toPx()
    }.toInt()

    init(
        width = width,
        height = height,
        clockFaceColor = Color.BLACK,
        outerRimColor = Color.WHITE,
        outerRimWidth = dipToPx(1F),
        innerRimColor = Color.WHITE,
        innerRimWidth = dipToPx(1F),
        thickMarkerColor = Color.WHITE,
        thickMarkerWidth = dipToPx(3F),
        thinMarkerColor = Color.WHITE,
        thinMarkerWidth = dipToPx(1F),
        numberTextColor = Color.WHITE,
        numberTextSize = dipToPx(18F),
        hourHandColor = Color.WHITE,
        hourHandWidth = dipToPx(5F),
        minuteHandColor = Color.WHITE,
        minuteHandWidth = dipToPx(3F),
        sweepHandColor = Color.WHITE,
        sweepHandWidth = dipToPx(1F),
        centerCircleColor = Color.WHITE
    )

    Column(
        modifier = modifier
    ) {
        OnDraw(
            modifier = modifier,
            showThickMarkers = true,
            showThinMarkers = true,
            showNumbers = true,
            showSweepHand = true,
            centerCircleRadius = dipToPx(5F),
            numberType = NumberType.ARABIC,
            hour = hour,
            minute = minute,
            second = second,
            milliSecond = milliSecond
        )
    }
}

@Composable
fun OnDraw(
    modifier: Modifier,
    showThickMarkers: Boolean,
    showThinMarkers: Boolean,
    showNumbers: Boolean,
    showSweepHand: Boolean,
    centerCircleRadius: Float,
    numberType: NumberType,
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int,
) {
    Canvas(
        modifier = modifier,
    ) {
        drawIntoCanvas {
            val canvas = it.nativeCanvas

            canvas.translate(paintRect.centerX().toFloat(), paintRect.centerY().toFloat())
            drawClockFace(
                canvas = canvas
            )
            drawOuterRim(
                canvas = canvas
            )
            if (showThickMarkers) {
                drawThickMarkers(
                    canvas = canvas
                )
            }
            if (showThinMarkers) {
                drawThinMarkers(
                    canvas = canvas
                )
            }
            if (showNumbers) {
                drawNumbers(
                    canvas = canvas,
                    numberType = numberType
                )
            }
            drawInnerRim(
                canvas = canvas
            )
            drawHourHand(
                canvas = canvas,
                hour = hour,
                minute = minute,
                second = second
            )
            drawMinuteHand(
                canvas = canvas,
                minute = minute,
                second = second
            )
            if (showSweepHand) {
                drawSweepHand(
                    canvas = canvas,
                    milliSecond = 1000 * second + milliSecond
                )
            }
            canvas.drawCircle(0F, 0F, centerCircleRadius, centerCirclePaint)
        }
    }
}

private fun init(
    width: Int,
    height: Int,
    clockFaceColor: Int,
    outerRimColor: Int,
    outerRimWidth: Float,
    innerRimColor: Int,
    innerRimWidth: Float,
    thickMarkerColor: Int,
    thickMarkerWidth: Float,
    thinMarkerColor: Int,
    thinMarkerWidth: Float,
    numberTextColor: Int,
    numberTextSize: Float,
    hourHandColor: Int,
    hourHandWidth: Float,
    minuteHandColor: Int,
    minuteHandWidth: Float,
    sweepHandColor: Int,
    sweepHandWidth: Float,
    centerCircleColor: Int
) {
    clockFacePaint.apply {
        isAntiAlias = true
        color = clockFaceColor
        style = Paint.Style.FILL
    }
    outerRimPaint.apply {
        isAntiAlias = true
        color = outerRimColor
        style = Paint.Style.STROKE
        strokeWidth = outerRimWidth
    }
    innerRimPaint.apply {
        isAntiAlias = true
        color = innerRimColor
        style = Paint.Style.STROKE
        strokeWidth = innerRimWidth
    }
    thickMarkerPaint.apply {
        isAntiAlias = true
        color = thickMarkerColor
        style = Paint.Style.STROKE
        strokeWidth = thickMarkerWidth
    }
    thinMarkerPaint.apply {
        isAntiAlias = true
        color = thinMarkerColor
        style = Paint.Style.STROKE
        strokeWidth = thinMarkerWidth
    }
    numberPaint.apply {
        isAntiAlias = true
        color = numberTextColor
        textSize = numberTextSize
        textAlign = Paint.Align.CENTER
    }
    hourHandPaint.apply {
        isAntiAlias = true
        color = hourHandColor
        style = Paint.Style.STROKE
        strokeWidth = hourHandWidth
    }
    minuteHandPaint.apply {
        isAntiAlias = true
        color = minuteHandColor
        style = Paint.Style.STROKE
        strokeWidth = minuteHandWidth
    }
    sweepHandPaint.apply {
        isAntiAlias = true
        color = sweepHandColor
        style = Paint.Style.STROKE
        strokeWidth = sweepHandWidth
    }
    centerCirclePaint.apply {
        isAntiAlias = true
        color = centerCircleColor
        style = Paint.Style.FILL
    }

    val centerX = width / 2
    val centerY = height / 2
    val rectSize = min(width, height)
    paintRect[centerX - rectSize / 2, centerY - rectSize / 2, centerX + rectSize / 2] = centerY + rectSize / 2
}

private fun drawClockFace(
    canvas: NativeCanvas
) {
    canvas.drawCircle(0F, 0F, paintRect.width() / 2F, clockFacePaint)
}

private fun drawOuterRim(
    canvas: Canvas
) {
    val radius = paintRect.width() / 2
    canvas.drawCircle(0F, 0F, radius - DEFAULT_THIN_MARKER_LENGTH, outerRimPaint)
}

private fun drawThickMarkers(
    canvas: Canvas
) {
    val radius = paintRect.width() / 2
    var degree = 0
    while (degree < 360) {
        val radian = degree * Math.PI / 180
        canvas.drawLine(
            radius * cos(radian).toFloat(),
            radius * sin(radian).toFloat(),
            (radius - DEFAULT_THICK_MARKER_LENGTH) * cos(radian).toFloat(),
            (radius - DEFAULT_THICK_MARKER_LENGTH) * sin(radian).toFloat(),
            thickMarkerPaint
        )
        degree += 30
    }
}

private fun drawThinMarkers(
    canvas: Canvas
) {
    val radius = paintRect.width() / 2
    var degree = 0
    while (degree < 360) {
        if (degree % 30 == 0) {
            degree += 6
            continue
        }
        val radian = degree * Math.PI / 180
        canvas.drawLine(
            radius * cos(radian).toFloat(),
            radius * sin(radian).toFloat(),
            (radius - DEFAULT_THIN_MARKER_LENGTH) * cos(radian).toFloat(),
            (radius - DEFAULT_THIN_MARKER_LENGTH) * sin(radian).toFloat(),
            thinMarkerPaint
        )
        degree += 6
    }
}

private fun drawNumbers(
    canvas: Canvas,
    numberType: NumberType
) {
    val radius = paintRect.width() / 2
    var number = 0
    val fm = numberPaint.fontMetrics
    val numberHeight = -fm.ascent + fm.descent
    var degree = -60
    while (degree < 300) {
        val radian = degree * Math.PI / 180
        val numberText = if (numberType == NumberType.ROMAN) {
            ROMAN_NUMBER_LIST[number++]
        } else {
            number++
            number.toString()
        }
        canvas.drawText(
            numberText,
            (radius - DEFAULT_THICK_MARKER_LENGTH - numberHeight / 2) * cos(radian)
                .toFloat(),
            (radius - DEFAULT_THICK_MARKER_LENGTH - numberHeight / 2) * sin(radian)
                .toFloat() - (fm.ascent + fm.descent) / 2,
            numberPaint
        )
        degree += 30
    }
}

private fun drawInnerRim(
    canvas: Canvas
) {
    val radius = paintRect.width() / 2
    val fm = numberPaint.fontMetrics
    val numberHeight = -fm.ascent + fm.descent
    canvas.drawCircle(
        0F,
        0F,
        radius - DEFAULT_THICK_MARKER_LENGTH - numberHeight - fm.bottom,
        innerRimPaint
    )
}

private fun drawHourHand(
    canvas: Canvas,
    hour: Int,
    minute: Int,
    second: Int
) {
    val fm = numberPaint.fontMetrics
    val numberHeight = -fm.ascent + fm.descent
    val radius = (paintRect.width() / 2 - DEFAULT_THICK_MARKER_LENGTH - numberHeight - fm.bottom - dipToPx(5F)).toInt()
    val radian = (hour - 3) * Math.PI / 6 + minute * Math.PI / 360 + second * Math.PI / 21600
    val stopX = radius * cos(radian).toFloat()
    val stopY = radius * sin(radian).toFloat()
    canvas.drawLine(0F, 0F, stopX, stopY, hourHandPaint)
}

private fun drawMinuteHand(
    canvas: Canvas,
    minute: Int,
    second: Int
) {
    val radius = (paintRect.width() / 2 - DEFAULT_THIN_MARKER_LENGTH - dipToPx(5F)).toInt()
    val radian = (minute - 15) * Math.PI / 30 + second * Math.PI / 1800
    val stopX = radius * cos(radian).toFloat()
    val stopY = radius * sin(radian).toFloat()
    canvas.drawLine(0F, 0F, stopX, stopY, minuteHandPaint)
}

private fun drawSweepHand(
    canvas: Canvas,
    milliSecond: Int
) {
    val radius = paintRect.width() / 2
    val radian = (milliSecond - 15000) * Math.PI / 30000
    val stopX = radius * cos(radian).toFloat()
    val stopY = radius * sin(radian).toFloat()
    canvas.drawLine(0F, 0F, stopX, stopY, sweepHandPaint)
}

private fun dipToPx(
    value: Float
): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, Resources.getSystem().displayMetrics)
}

enum class NumberType {
    ARABIC, ROMAN
}

private val DEFAULT_THIN_MARKER_LENGTH = dipToPx(10F)
private val DEFAULT_THICK_MARKER_LENGTH = dipToPx(20F)
private val ROMAN_NUMBER_LIST = arrayOf("Ⅰ", "Ⅱ", "Ⅲ", "Ⅳ", "Ⅴ", "Ⅵ", "Ⅶ", "Ⅷ", "Ⅸ", "Ⅹ", "Ⅺ", "Ⅻ")

private val clockFacePaint = Paint()
private val outerRimPaint = Paint()
private val innerRimPaint = Paint()
private val thickMarkerPaint = Paint()
private val thinMarkerPaint = Paint()
private val numberPaint = TextPaint()
private val hourHandPaint = Paint()
private val minuteHandPaint = Paint()
private val sweepHandPaint = Paint()
private val centerCirclePaint = Paint()
private val paintRect = Rect()

