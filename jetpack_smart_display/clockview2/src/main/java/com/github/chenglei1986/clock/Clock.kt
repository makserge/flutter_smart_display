package com.github.chenglei1986.clock

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.util.TypedValue
import android.view.View
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class Clock (context: Context) : View(context) {
    private val ROMAN_NUMBER_LIST = arrayOf("Ⅰ", "Ⅱ", "Ⅲ", "Ⅳ", "Ⅴ", "Ⅵ", "Ⅶ", "Ⅷ", "Ⅸ", "Ⅹ", "Ⅺ", "Ⅻ")

    private val clockFaceColor = DEFAULT_CLOCK_FACE_COLOR
    private val outerRimColor = DEFAULT_OUTER_RIM_COLOR
    private val innerRimColor = DEFAULT_INNER_RIM_COLOR
    private val thickMarkerColor = DEFAULT_THICK_MARKER_COLOR
    private val thinMarkerColor = DEFAULT_THIN_MARKER_COLOR
    private val numberTextColor = DEFAULT_NUMBER_TEXT_COLOR
    private val hourHandColor = DEFAULT_HOUR_HAND_COLOR
    private val minuteHandColor = DEFAULT_MINUTE_HAND_COLOR
    private val sweepHandColor = DEFAULT_SWEEP_HAND_COLOR
    private val centerCircleColor = DEFAULT_CENTER_CIRCLE_COLOR
    private val outerRimWidth = DEFAULT_OUTER_RIM_WIDTH
    private val innerRimWidth = DEFAULT_INNER_RIM_WIDTH
    private val thickMarkerWidth = DEFAULT_THICK_MARKER_WIDTH
    private val thinMarkerWidth = DEFAULT_THIN_MARKER_WIDTH
    private val numberTextSize = DEFAULT_NUMBER_TEXT_SIZE
    private val hourHandWidth = DEFAULT_HOUR_HAND_WIDTH
    private val minuteHandWidth = DEFAULT_MINUTE_HAND_WIDTH
    private val sweepHandWidth = DEFAULT_SWEEP_HAND_WIDTH
    private val centerCircleRadius = DEFAULT_CENTER_CIRCLE_RADIUS
    private val showThickMarkers = true
    private val showThinMarkers = true
    private val showNumbers = true
    private val showSweepHand = true
    private val numberType = NumberType.ARABIC
    private val minWidth = dipToPx(MIN_WIDTH_DP).toInt()
    private val minHeight = dipToPx(MIN_HEIGHT_DP).toInt()
    private val paintRect = Rect()
    private var refreshRectLeft = 0
    private var refreshRectTop = 0
    private var refreshRectRight = 0
    private var refreshRectBottom = 0
    private var hour = 0
    private var minute = 0
    private var second = 0
    private var milliSecond = 0
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

    private enum class NumberType {
        ARABIC, ROMAN
    }

    init {
        initPaint()
    }

    fun setTime(hour: Int, minute: Int, second: Int, milliSecond: Int) {
        this.hour = hour
        this.minute = minute
        this.second = second
        this.milliSecond = 1000 * second + milliSecond
        invalidate()
    }

    private fun initPaint() {
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
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width = if (widthMode == MeasureSpec.EXACTLY) widthSize else minWidth
        val height = if (heightMode == MeasureSpec.EXACTLY) heightSize else minHeight
        val centerX = width / 2
        val centerY = height / 2
        val rectSize = min(width, height)
        paintRect[centerX - rectSize / 2, centerY - rectSize / 2, centerX + rectSize / 2] = centerY + rectSize / 2
        setMeasuredDimension(width, height)
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {}

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(paintRect.centerX().toFloat(), paintRect.centerY().toFloat())
        drawClockFace(canvas)
        drawOuterRim(canvas)
        if (showThickMarkers) {
            drawThickMarkers(canvas)
        }
        if (showThinMarkers) {
            drawThinMarkers(canvas)
        }
        if (showNumbers) {
            drawNumbers(canvas)
        }
        drawInnerRim(canvas)
        drawHourHand(canvas, hour, minute, second)
        drawMinuteHand(canvas, minute, second)
        if (showSweepHand) {
            drawSweepHand(canvas, milliSecond)
        }
        canvas.drawCircle(0F, 0F, centerCircleRadius, centerCirclePaint)
        postInvalidate()
    }

    private fun drawClockFace(canvas: Canvas) {
        canvas.drawCircle(0f, 0f, paintRect.width() / 2f, clockFacePaint)
    }

    private fun drawThickMarkers(canvas: Canvas) {
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

    private fun drawThinMarkers(canvas: Canvas) {
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

    private fun drawOuterRim(canvas: Canvas) {
        val radius = paintRect.width() / 2
        canvas.drawCircle(0F, 0F, radius - DEFAULT_THIN_MARKER_LENGTH, outerRimPaint)
    }

    private fun drawNumbers(canvas: Canvas) {
        val radius = paintRect.width() / 2
        var number = 0
        val fm = numberPaint.fontMetrics
        val numberHeight = -fm.ascent + fm.descent
        var degree = -60
        while (degree < 300) {
            val radian = degree * Math.PI / 180
            val numberText = if (numberType == NumberType.ROMAN) {
                convertToRomanNumber(number++)
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

    private fun drawInnerRim(canvas: Canvas) {
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

    private fun drawHourHand(canvas: Canvas, hour: Int, minute: Int, second: Int) {
        val fm = numberPaint.fontMetrics
        val numberHeight = -fm.ascent + fm.descent
        val radius = (paintRect.width() / 2 - DEFAULT_THICK_MARKER_LENGTH - numberHeight - fm.bottom - dipToPx(5F)).toInt()
        val radian = (hour - 3) * Math.PI / 6 + minute * Math.PI / 360 + second * Math.PI / 21600
        val stopX = radius * cos(radian).toFloat()
        val stopY = radius * sin(radian).toFloat()
        canvas.drawLine(0F, 0F, stopX, stopY, hourHandPaint)
        setRefreshRectCoordinates(stopX.toInt(), stopY.toInt())
    }

    private fun drawMinuteHand(canvas: Canvas, minute: Int, second: Int) {
        val radius = (paintRect.width() / 2 - DEFAULT_THIN_MARKER_LENGTH - dipToPx(5F)).toInt()
        val radian = (minute - 15) * Math.PI / 30 + second * Math.PI / 1800
        val stopX = radius * cos(radian).toFloat()
        val stopY = radius * sin(radian).toFloat()
        canvas.drawLine(0F, 0F, stopX, stopY, minuteHandPaint)
        setRefreshRectCoordinates(stopX.toInt(), stopY.toInt())
    }

    private fun drawSweepHand(canvas: Canvas, milliSecond: Int) {
        val radius = paintRect.width() / 2
        val radian = (milliSecond - 15000) * Math.PI / 30000
        val stopX = radius * cos(radian).toFloat()
        val stopY = radius * sin(radian).toFloat()
        canvas.drawLine(0F, 0F, stopX, stopY, sweepHandPaint)
        setRefreshRectCoordinates(stopX.toInt(), stopY.toInt())
    }

    private fun setRefreshRectCoordinates(x: Int, y: Int) {
        refreshRectLeft = min(refreshRectLeft, x)
        refreshRectTop = min(refreshRectTop, y)
        refreshRectRight = max(refreshRectRight, x)
        refreshRectBottom = max(refreshRectBottom, y)
    }

    private fun convertToRomanNumber(number: Int): String {
        return ROMAN_NUMBER_LIST[number]
    }

    companion object {
        private const val MIN_WIDTH_DP = 50F
        private const val MIN_HEIGHT_DP = 50F
        private val DEFAULT_CLOCK_FACE_COLOR = Color.parseColor("#A9ADB0")
        private const val DEFAULT_OUTER_RIM_COLOR = Color.BLACK
        private const val DEFAULT_INNER_RIM_COLOR = Color.BLACK
        private const val DEFAULT_THICK_MARKER_COLOR = Color.BLACK
        private const val DEFAULT_THIN_MARKER_COLOR = Color.BLACK
        private const val DEFAULT_NUMBER_TEXT_COLOR = Color.BLACK
        private const val DEFAULT_HOUR_HAND_COLOR = Color.BLACK
        private const val DEFAULT_MINUTE_HAND_COLOR = Color.BLACK
        private const val DEFAULT_SWEEP_HAND_COLOR = Color.BLACK
        private const val DEFAULT_CENTER_CIRCLE_COLOR = Color.BLACK
        private val DEFAULT_OUTER_RIM_WIDTH = dipToPx(1F)
        private val DEFAULT_INNER_RIM_WIDTH = dipToPx(1F)
        private val DEFAULT_THICK_MARKER_WIDTH = dipToPx(3F)
        private val DEFAULT_THICK_MARKER_LENGTH = dipToPx(20F)
        private val DEFAULT_THIN_MARKER_WIDTH = dipToPx(1F)
        private val DEFAULT_THIN_MARKER_LENGTH = dipToPx(10F)
        private val DEFAULT_NUMBER_TEXT_SIZE = dipToPx(18F)
        private val DEFAULT_HOUR_HAND_WIDTH = dipToPx(5F)
        private val DEFAULT_MINUTE_HAND_WIDTH = dipToPx(3F)
        private val DEFAULT_SWEEP_HAND_WIDTH = dipToPx(1F)
        private val DEFAULT_CENTER_CIRCLE_RADIUS = dipToPx(5F)

        private fun dipToPx(dipValue: Float): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, Resources.getSystem().displayMetrics)
        }
    }
}