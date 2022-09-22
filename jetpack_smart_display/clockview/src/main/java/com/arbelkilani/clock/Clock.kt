package com.arbelkilani.clock

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.*
import android.text.style.RelativeSizeSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.arbelkilani.clock.enumeration.BorderStyle
import com.arbelkilani.clock.enumeration.ClockType
import com.arbelkilani.clock.enumeration.analog.*
import com.arbelkilani.clock.enumeration.numeric.NumericFormat
import com.arbelkilani.clock.model.theme.AnalogTheme
import com.arbelkilani.clock.model.theme.NumericTheme
import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class Clock(context: Context) : View(context) {
    private var isAM = false
    private var hour = 1
    private var minute = 0
    private var second = 0

    private var clockType= ClockType.ANALOG
    private var clockBackground: Drawable? = null
    private var showCenter = DEFAULT_STATE
    private var centerInnerColor = DEFAULT_PRIMARY_COLOR
    private var centerOuterColor = DEFAULT_SECONDARY_COLOR
    private var showBorder = DEFAULT_STATE
    private var borderColor = DEFAULT_PRIMARY_COLOR
    private var borderStyle = BorderStyle.RECTANGLE
    private var borderRadiusRx = DEFAULT_BORDER_RADIUS
    private var borderRadiusRy = DEFAULT_BORDER_RADIUS
    private var showSecondsNeedle = DEFAULT_STATE
    private var needleHoursColor = DEFAULT_PRIMARY_COLOR
    private var needleMinutesColor = DEFAULT_PRIMARY_COLOR
    private var needleSecondsColor = DEFAULT_SECONDARY_COLOR
    private var minutesProgressColor = DEFAULT_PRIMARY_COLOR
    private var showDegrees = DEFAULT_STATE
    private var degreesColor = DEFAULT_PRIMARY_COLOR
    private var degreesType = DegreeType.LINE
    private var degreesStep = DegreesStep.FULL
    private var valuesFont = ResourcesCompat.getFont(context, R.font.proxima_nova_thin)
    private var valuesColor = DEFAULT_PRIMARY_COLOR
    private var showHoursValues = DEFAULT_STATE
    private var showMinutesValues = DEFAULT_STATE
    private var minutesValuesFactor = DEFAULT_MINUTES_BORDER_FACTOR
    private var valueStep = ValueStep.FULL
    private var valueType = ValueType.NONE
    private var valueDisposition = ValueDisposition.REGULAR
    private var numericFormat = NumericFormat.HOUR_24
    private var numericShowSeconds = DEFAULT_STATE

    // Attributes
    private var size = 0
    private var centerX = 0
    private var centerY = 0
    private var radius = 0
    private var defaultThickness = 0F
    private var defaultRectF: RectF? = null

    fun setTime(isAM: Boolean, hour: Int, minute: Int, second: Int) {
        this.isAM = isAM
        this.hour = hour
        this.minute = minute
        this.second = second
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = min(measuredWidth - paddingLeft - paddingRight, measuredHeight - paddingTop - paddingBottom)
        setMeasuredDimension(size + paddingLeft + paddingRight, size + paddingTop + paddingBottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        onPreDraw()
        when (clockType) {
            ClockType.ANALOG -> drawAnalogClock(canvas)
            ClockType.NUMERIC -> drawNumericClock(canvas)
        }
    }

    private fun drawAnalogClock(canvas: Canvas) {
        drawBackground(canvas)
        drawBorder(canvas)
        drawHoursValues(canvas)
        drawMinutesValues(canvas)
        drawDegrees(canvas)
        drawNeedles(canvas)
        drawCenter(canvas)
    }

    private fun onPreDraw() {
        size = min(height, width)
        centerX = size / 2
        centerY = size / 2
        radius = (size * (1 - DEFAULT_BORDER_THICKNESS) / 2).toInt()
        defaultThickness = size * DEFAULT_BORDER_THICKNESS
        defaultRectF = RectF(defaultThickness, defaultThickness, size - defaultThickness, size - defaultThickness)
    }

    private fun drawMinutesValues(canvas: Canvas) {
        if (!showMinutesValues) return
        val rect = Rect()
        val textPaint = TextPaint().apply{
            color = minutesProgressColor
            typeface = ResourcesCompat.getFont(context, R.font.proxima_nova_thin)
            textSize = size * MINUTES_TEXT_SIZE
        }
        val rText = (centerX - (1 - minutesValuesFactor - 2 * DEFAULT_BORDER_THICKNESS - MINUTES_TEXT_SIZE) * radius).toInt()
        var i = 0
        while (i < FULL_ANGLE) {
            val value = i / 6
            val formatted = when (valueType) {
                ValueType.ROMAN -> ClockUtils.toRoman(value)!!
                else -> value.toString()
            }
            val textX = (centerX + rText * cos(Math.toRadians((REGULAR_ANGLE - i).toDouble()))).toInt()
            val textY = (centerX - rText * sin(Math.toRadians((REGULAR_ANGLE - i).toDouble()))).toInt()
            textPaint.getTextBounds(formatted, 0, formatted.length, rect)
            canvas.drawText(
                formatted,
                textX - rect.width() * 1f / formatted.length,
                textY + rect.height() * 1f / formatted.length,
                textPaint
            )
            i += QUARTER_DEGREE_STEPS
        }
    }

    private fun drawDegrees(canvas: Canvas) {
        if (!showDegrees) return
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = size * DEFAULT_DEGREE_STROKE_WIDTH
            color = degreesColor
        }
        val rPadded = centerX - (size * (DEFAULT_BORDER_THICKNESS + 0.03f)).toInt()
        val rEnd = centerX - (size * (DEFAULT_BORDER_THICKNESS + 0.06f)).toInt()
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

    private fun drawNumericClock(canvas: Canvas) {
        if (showBorder) {
            drawCustomBorder(canvas)
        }
        if (clockBackground != null) {
            val paint = Paint()
            paint.isAntiAlias = true
            val bitmap = (clockBackground as BitmapDrawable).bitmap
            val rectF = RectF(
                (centerX - radius).toFloat(),
                (centerY - radius).toFloat(),
                (centerX + radius).toFloat(),
                (centerY + radius).toFloat()
            )
            val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val tCanvas = Canvas(output)
            when (borderStyle) {
                BorderStyle.RECTANGLE -> tCanvas.drawRect(
                    defaultRectF!!, paint
                )
                BorderStyle.CIRCLE -> tCanvas.drawCircle(
                    centerX.toFloat(),
                    centerY.toFloat(),
                    radius.toFloat(),
                    paint
                )
                BorderStyle.ROUNDED_RECTANGLE -> {
                    val rx = radius - radius * (100 - borderRadiusRx) / 100F
                    val ry = radius - radius * (100 - borderRadiusRy) / 100F
                    tCanvas.drawRoundRect(defaultRectF!!, rx, ry, paint)
                }
            }
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            tCanvas.drawBitmap(bitmap, null, rectF, paint)
            canvas.drawBitmap(output, null, rectF, Paint())
        }
        val textPaint = TextPaint().apply{
            isAntiAlias = true
            typeface = valuesFont
            textSize = size * 0.22F
            color = valuesColor
        }
        val spannableString = SpannableStringBuilder()
        val minute = String.format(Locale.getDefault(), "%02d", minute)
        val second = String.format(Locale.getDefault(), "%02d", second)
        if (numericShowSeconds) {
            if (numericFormat == NumericFormat.HOUR_12) {
                spannableString.apply {
                    append(String.format(Locale.getDefault(), "%02d", hour))
                    append(":")
                    append(minute)
                    append(".")
                    append(second)
                    append(if (isAM) "AM" else "PM")
                    setSpan(RelativeSizeSpan(0.3F), toString().length - 2, toString().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // se superscript percent
                }
            } else {
                spannableString.apply {
                    append(String.format(Locale.getDefault(), "%02d", hour))
                    append(":")
                    append(minute)
                    append(".")
                    append(second)
                }
            }
        } else {
            if (numericFormat == NumericFormat.HOUR_12) {
                spannableString.apply {
                    append(String.format(Locale.getDefault(), "%02d", hour))
                    append(":")
                    append(minute)
                    append(if (isAM) "AM" else "PM")
                    setSpan(RelativeSizeSpan(0.4F), toString().length - 2, toString().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // se superscript percent
                }
            } else {
                spannableString.apply {
                    append(String.format(Locale.getDefault(), "%02d", hour))
                    append(":")
                    append(minute)
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

    private fun drawCustomBorder(canvas: Canvas) {
        val paint = Paint().apply{
            isAntiAlias = true
            style = Paint.Style.FILL
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = defaultThickness
        }
        when (borderStyle) {
            BorderStyle.RECTANGLE -> canvas.drawRect(
                defaultRectF!!, paint
            )
            BorderStyle.CIRCLE -> canvas.drawCircle(
                centerX.toFloat(),
                centerY.toFloat(),
                radius.toFloat(),
                paint
            )
            BorderStyle.ROUNDED_RECTANGLE -> {
                val rx = radius - radius * (100 - borderRadiusRx) / 100F
                val ry = radius - radius * (100 - borderRadiusRy) / 100F
                canvas.drawRoundRect(defaultRectF!!, rx, ry, paint)
            }
        }
    }

    private fun drawHoursValues(canvas: Canvas) {
        if (!showHoursValues) return
        val rect = Rect()
        val textPaint = TextPaint().apply{
            flags = Paint.ANTI_ALIAS_FLAG
            isAntiAlias = true
            color = valuesColor
            typeface = valuesFont
            textSize = size * DEFAULT_HOURS_VALUES_TEXT_SIZE
        }
        var degreeSpace = 0F
        if (showDegrees) degreeSpace = DEFAULT_DEGREE_STROKE_WIDTH + 0.06F
        val rText = (centerX - size * DEFAULT_HOURS_VALUES_TEXT_SIZE - size * degreeSpace).toInt()
        var i = FULL_ANGLE
        while (i > 0) {
            val value = i / 30
            val formatted = when (valueType) {
                ValueType.ROMAN -> ClockUtils.toRoman(value)!!
                else -> value.toString()
            }
            if (valueDisposition.id == 0) {
                if (i % REGULAR_ANGLE == 0) {
                    textPaint.textSize = size * DEFAULT_HOURS_VALUES_TEXT_SIZE
                    textPaint.alpha = FULL_ALPHA
                } else {
                    textPaint.textSize =
                        size * (DEFAULT_HOURS_VALUES_TEXT_SIZE - 0.03F)
                    textPaint.alpha = CUSTOM_ALPHA
                }
            } else {
                textPaint.textSize = size * DEFAULT_HOURS_VALUES_TEXT_SIZE
                textPaint.alpha = FULL_ALPHA
            }
            val textX =
                (centerX + rText * cos(Math.toRadians((REGULAR_ANGLE - i).toDouble()))).toInt()
            val textY =
                (centerX - rText * sin(Math.toRadians((REGULAR_ANGLE - i).toDouble()))).toInt()
            textPaint.getTextBounds(formatted, 0, formatted.length, rect)
            canvas.drawText(
                formatted,
                textX - rect.width().toFloat() / formatted.length,
                textY + rect.height().toFloat() / formatted.length,
                textPaint
            )
            i -= valueStep.id
        }
    }

    private fun drawNeedles(canvas: Canvas) {
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
        val needleMaxLength =
            radius * NEEDLE_LENGTH_FACTOR - 2 * (degreesSpace + borderThickness + hoursTextSize)

        // draw seconds needle
        val secondsDegree = (second * 6).toFloat()
        // draw hours needle
        val hoursDegree = (hour + minute / 60F) * 30 // 30 = 360 / 12
        val startHoursX =
            (centerX + radius * needleStartSpace * cos(Math.toRadians((-REGULAR_ANGLE + hoursDegree).toDouble()))).toFloat()
        val stopHoursX =
            (centerX + needleMaxLength * 0.6F * cos(Math.toRadians((-REGULAR_ANGLE + hoursDegree).toDouble()))).toFloat()
        val startHoursY =
            (centerY + radius * needleStartSpace * sin(Math.toRadians((-REGULAR_ANGLE + hoursDegree).toDouble()))).toFloat()
        val stopHoursY =
            (centerY + needleMaxLength * 0.6F * sin(Math.toRadians((-REGULAR_ANGLE + hoursDegree).toDouble()))).toFloat()

        // draw minutes needle
        val minutesDegree = (minute + second / 60F) * 6
        val startMinutesX =
            (centerX + radius * needleStartSpace * cos(Math.toRadians((-REGULAR_ANGLE + minutesDegree).toDouble()))).toFloat()
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
            val startSecondsX =
            (centerX + radius * needleStartSpace * cos(Math.toRadians((-REGULAR_ANGLE + secondsDegree).toDouble()))).toFloat()
            val stopSecondsX =
            (centerX + needleMaxLength * cos(Math.toRadians((-REGULAR_ANGLE + secondsDegree).toDouble()))).toFloat()
            val startSecondsY =
            (centerY + radius * needleStartSpace * sin(Math.toRadians((-REGULAR_ANGLE + secondsDegree).toDouble()))).toFloat()
            val stopSecondsY =
            (centerY + needleMaxLength * sin(Math.toRadians((-REGULAR_ANGLE + secondsDegree).toDouble()))).toFloat()

            canvas.drawLine(startSecondsX, startSecondsY, stopSecondsX, stopSecondsY, paint)
        }
    }

    private fun drawBorder(canvas: Canvas) {
        if (!showBorder) return
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply{
            isAntiAlias = true
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = size * DEFAULT_BORDER_THICKNESS
        }
        canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), radius.toFloat(), paint)
    }

    private fun drawBackground(canvas: Canvas) {
        if (clockBackground == null) return
        val bitmap = (clockBackground as BitmapDrawable).bitmap
        val rectF = RectF(
            (centerX - radius).toFloat(),
            (centerY - radius).toFloat(),
            (centerX + radius).toFloat(),
            (centerY + radius).toFloat()
        )
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val tCanvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        tCanvas.drawCircle(centerX.toFloat(), centerY.toFloat(), radius.toFloat(), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        tCanvas.drawBitmap(bitmap, null, rectF, paint)
        canvas.drawBitmap(output, null, rectF, Paint())
    }

    private fun drawCenter(canvas: Canvas) {
        if (!showCenter) return
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

    fun setClockType(clockType: ClockType) {
        this.clockType = clockType
        invalidate()
    }

    fun setClockBackground(clockBackground: Int) {
        this.clockBackground = ResourcesCompat.getDrawable(context.resources, clockBackground, null)
    }

    fun showCenter(showCenter: Boolean) {
        this.showCenter = showCenter
    }

    fun setCenterInnerColor(centerInnerColor: Int) {
        this.centerInnerColor = ResourcesCompat.getColor(context.resources, centerInnerColor, null)
    }

    fun setCenterOuterColor(centerOuterColor: Int) {
        this.centerOuterColor = ResourcesCompat.getColor(context.resources, centerOuterColor, null)
    }

    fun setShowBorder(showBorder: Boolean) {
        this.showBorder = showBorder
    }

    fun setBorderColor(borderColor: Int) {
        this.borderColor = ResourcesCompat.getColor(context.resources, borderColor, null)
    }

    fun setBorderStyle(borderStyle: BorderStyle?) {
        this.borderStyle = borderStyle!!
    }

    fun setBorderRadius(borderRadiusRx: Int, borderRadiusRy: Int) {
        this.borderRadiusRx = borderRadiusRx
        this.borderRadiusRy = borderRadiusRy
    }

    fun setShowSecondsNeedle(showSecondsNeedle: Boolean) {
        this.showSecondsNeedle = showSecondsNeedle
    }

    fun setHoursNeedleColor(needleHoursColor: Int) {
        this.needleHoursColor = ResourcesCompat.getColor(context.resources, needleHoursColor, null)
    }

    fun setMinutesNeedleColor(needleMinutesColor: Int) {
        this.needleMinutesColor = ResourcesCompat.getColor(context.resources, needleMinutesColor, null)
    }

    fun setSecondsNeedleColor(needleSecondsColor: Int) {
        this.needleSecondsColor = ResourcesCompat.getColor(context.resources, needleSecondsColor, null)
    }

    fun setProgressColor(progressColor: Int) {
        centerOuterColor = ResourcesCompat.getColor(context.resources, progressColor, null)
    }

    fun setMinutesProgressColor(minutesProgressColor: Int) {
        try {
            this.minutesProgressColor = ContextCompat.getColor(context, minutesProgressColor)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setShowDegrees(showDegrees: Boolean) {
        this.showDegrees = showDegrees
    }

    fun setDegreesColor(degreesColor: Int) {
        this.degreesColor = degreesColor
    }

    fun setDegreesType(clockDegreesType: DegreeType?) {
        degreesType = clockDegreesType!!
    }

    fun setDegreesStep(degreesStep: DegreesStep?) {
        this.degreesStep = degreesStep!!
    }

    fun setValuesFont(valuesFont: Int) {
        this.valuesFont = ResourcesCompat.getFont(context, valuesFont)
    }

    fun setValuesColor(valuesColor: Int) {
        this.valuesColor = ResourcesCompat.getColor(context.resources, valuesColor, null)
    }

    fun setShowHoursValues(showHoursValues: Boolean) {
        this.showHoursValues = showHoursValues
    }

    fun setShowMinutesValues(showMinutesValues: Boolean) {
        this.showMinutesValues = showMinutesValues
    }

    fun setMinutesValuesFactor(minutesValuesFactor: Float) {
        this.minutesValuesFactor = minutesValuesFactor
    }

    fun setValueStep(valueStep: ValueStep?) {
        this.valueStep = valueStep!!
    }

    fun setValueType(valueType: ValueType?) {
        this.valueType = valueType!!
    }

    fun setValueDisposition(valueDisposition: ValueDisposition?) {
        this.valueDisposition = valueDisposition!!
    }

    fun setNumericFormat(numericFormat: NumericFormat?) {
        this.numericFormat = numericFormat!!
    }

    fun setNumericShowSeconds(numericShowSeconds: Boolean) {
        this.numericShowSeconds = numericShowSeconds
    }

    // Themes
    fun setAnalogicalTheme(analogicalTheme: AnalogTheme) {
        clockType = analogicalTheme.clockType
        clockBackground = ResourcesCompat.getDrawable(context.resources, analogicalTheme.clockBackground, null)
        showCenter = analogicalTheme.isShowCenter
        centerInnerColor = analogicalTheme.centerInnerColor
        centerOuterColor = analogicalTheme.centerOuterColor
        showBorder = analogicalTheme.isShowBorder
        borderColor = analogicalTheme.borderColor
        showSecondsNeedle = analogicalTheme.isShowSecondsNeedle
        needleHoursColor = analogicalTheme.needleHoursColor
        needleMinutesColor = analogicalTheme.needleMinutesColor
        needleSecondsColor = analogicalTheme.needleSecondsColor
        minutesProgressColor = analogicalTheme.minutesProgressColor
        showDegrees = analogicalTheme.isShowDegrees
        degreesColor = ResourcesCompat.getColor(context.resources, analogicalTheme.degreesColor, null)
        degreesType = analogicalTheme.degreesType
        degreesStep = analogicalTheme.degreesStep
        valuesFont = ResourcesCompat.getFont(context, analogicalTheme.valuesFont)
        valuesColor = ResourcesCompat.getColor(context.resources, analogicalTheme.valuesColor, null)
        showHoursValues = analogicalTheme.isShowHoursValues
        showMinutesValues = analogicalTheme.isShowMinutesValues
        minutesValuesFactor = analogicalTheme.minutesValuesFactor
        valueStep = analogicalTheme.valueStep
        valueType = analogicalTheme.valueType
        valueDisposition = analogicalTheme.valueDisposition
    }

    fun setNumericTheme(numericTheme: NumericTheme) {
        clockType = numericTheme.clockType
        clockBackground = ResourcesCompat.getDrawable(context.resources, numericTheme.clockBackground, null)
        valuesFont = ResourcesCompat.getFont(context, numericTheme.valuesFont)
        valuesColor = ResourcesCompat.getColor(context.resources, numericTheme.valuesColor, null)
        showBorder = numericTheme.isShowBorder
        borderColor = numericTheme.borderColor
        borderRadiusRx = numericTheme.borderRadiusRx
        borderRadiusRy = numericTheme.borderRadiusRy
        numericFormat = numericTheme.numericFormat!!
    }

    companion object {
        private const val DEFAULT_PRIMARY_COLOR = Color.BLACK
        private const val DEFAULT_SECONDARY_COLOR = Color.LTGRAY
        private const val DEFAULT_STATE = false
        private const val DEFAULT_BORDER_THICKNESS = 0.015F
        private const val DEFAULT_BORDER_RADIUS = 20
        private const val FULL_ANGLE = 360
        private const val REGULAR_ANGLE = 90
        private const val CUSTOM_ALPHA = 140
        private const val FULL_ALPHA = 255
        private const val DEFAULT_MINUTES_BORDER_FACTOR = 0.4F
        private const val DEFAULT_DEGREE_STROKE_WIDTH = 0.010F
        private const val DEFAULT_NEEDLE_STROKE_WIDTH = 0.015F
        private const val NEEDLE_LENGTH_FACTOR = 0.9F
        private const val DEFAULT_NEEDLE_START_SPACE = 0.05F
        private const val DEFAULT_HOURS_VALUES_TEXT_SIZE = 0.08F
        private const val QUARTER_DEGREE_STEPS = 90
        private const val MINUTES_TEXT_SIZE = 0.050F
    }
}