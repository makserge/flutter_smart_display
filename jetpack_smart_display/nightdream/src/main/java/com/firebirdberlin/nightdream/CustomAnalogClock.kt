package com.firebirdberlin.nightdream

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.firebirdberlin.nightdream.AnalogClockConfig.HandShape
import com.firebirdberlin.nightdream.AnalogClockConfig.TickStyle
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class CustomAnalogClock : View {
    private val ROMAN_DIGITS = arrayOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII")
    private var paint = Paint()
    private var customColor = Color.BLUE
    private var customSecondaryColor = Color.WHITE
    private var customColorFilter: ColorFilter? = null
    private var secondaryColorFilter: ColorFilter? = null
    private var typeface = Typeface.DEFAULT
    private var boldTypeface = Typeface.DEFAULT
    private var centerX = 0F
    private var centerY = 0F
    private var radius = 0
    private var hour = 0
    private var min = 0
    private var sec = 0
    private lateinit var config: AnalogClockConfig

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        customColorFilter = LightingColorFilter(Color.WHITE, 1)
        secondaryColorFilter = LightingColorFilter(Color.WHITE, 1)
    }

    fun setPrimaryColor(color: Int) {
        customColor = color
        customColorFilter = LightingColorFilter(color, 1)
        invalidate()
    }

    fun setSecondaryColor(color: Int) {
        customSecondaryColor = color
        secondaryColorFilter = LightingColorFilter(color, 1)
        invalidate()
    }

    fun setStyle(style: AnalogClockConfig.Style, allow_second_hand: Boolean) {
        config = AnalogClockConfig(context, style)
        config.showSecondHand = allow_second_hand && config.showSecondHand
        typeface = FontCache[context, config.fontUri]
        boldTypeface = Typeface.create(typeface, Typeface.BOLD)
    }

    fun setStyle(style: AnalogClockConfig.Style) {
        setStyle(style, true)
    }

    fun setTime(hour: Int, min: Int, sec: Int) {
        this.hour = hour
        this.min = min
        this.sec = sec
        invalidate()
    }

    public override fun onDraw(canvas: Canvas) {
        centerX = width / 2F
        centerY = height / 2F
        radius = width / 2 - 20
        paint.isAntiAlias = true
        paint.color = Color.WHITE

        val hourAngle = (hour.toDouble() / 6.0 * Math.PI - Math.PI / 2.0
                + min.toDouble() / 60.0 * Math.PI / 6.0)
        val minAngle = min.toDouble() / 30.0 * Math.PI - Math.PI / 2.0
        val secAngle = sec.toDouble() / 30.0 * Math.PI - Math.PI / 2.0
        paint.alpha = 255
        paint.color = Color.WHITE
        drawBackgroundArc(canvas, centerX, centerY, radius, minAngle)
        applyGoldShader(paint, centerX, centerY, radius)
        applyShader(paint, centerX, centerY, radius)
        drawOuterCircle(canvas)
        drawTicks(canvas, centerX, centerY, radius)
        drawHourDigits(canvas, centerX, centerY, radius)
        drawHands(canvas, centerX, centerY, radius, hourAngle, minAngle, secAngle)
    }

    private fun drawHands(
        canvas: Canvas, centerX: Float, centerY: Float, radius: Int,
        hourAngle: Double, min_angle: Double, sec_angle: Double
    ) {
        if (isTextureDecoration) {
            applyPureTexture(paint)
        }
        paint.style = Paint.Style.FILL
        if (!isTextureDecoration) {
            paint.shader = null
        }
        // minute hand
        canvas.save()
        if (!isTextureDecoration || config.handShape == HandShape.ARC) {
            paint.colorFilter = customColorFilter
        }
        canvas.rotate(radiansToDegrees(min_angle).toFloat(), centerX, centerY)
        drawHand(
            canvas,
            paint,
            centerX,
            centerY,
            (config.handLengthMinutes * radius).toInt(),
            (config.handWidthMinutes * radius).toInt()
        )
        canvas.restore()

        // second hand
        if (config.showSecondHand) {
            canvas.save()
            if (!isTextureDecoration || config.handShape == HandShape.ARC) {
                paint.colorFilter = secondaryColorFilter
            }
            canvas.rotate(radiansToDegrees(sec_angle).toFloat(), centerX, centerY)
            drawHand(
                canvas,
                paint,
                centerX,
                centerY,
                (config.handLengthMinutes * radius).toInt(),
                (config.handWidthMinutes / 3 * radius).toInt()
            )
            canvas.restore()
        }
        // hour hand
        canvas.save()
        if (!isTextureDecoration || config.handShape == HandShape.ARC) {
            paint.colorFilter = secondaryColorFilter
        }
        canvas.rotate(radiansToDegrees(hourAngle).toFloat(), centerX, centerY)
        drawHand(
            canvas,
            paint,
            centerX,
            centerY,
            (config.handLengthHours * radius).toInt(),
            (config.handWidthHours * radius).toInt()
        )
        canvas.restore()
        drawInnerCircle(canvas)
    }

    private fun drawHand(
        canvas: Canvas,
        paint: Paint,
        baseX: Float,
        baseY: Float,
        height: Int,
        width: Int
    ) {
        when (config.handShape) {
            HandShape.ARC -> drawHandArc(canvas, height, width)
            HandShape.BAR -> drawHandBar(canvas, paint, baseX, baseY, height, width)
            HandShape.TRIANGLE -> drawHandTriangle(canvas, paint, baseX, baseY, height, width)
        }
    }

    private fun drawHandTriangle(
        canvas: Canvas,
        paint: Paint,
        centerX: Float,
        centerY: Float,
        length: Int,
        width: Int
    ) {
        val halfWidth = width / 2
        val path = Path().apply{
            moveTo(centerX, centerY - halfWidth)
            lineTo(centerX + length, centerY)
            lineTo(centerX, centerY + halfWidth)
            lineTo(centerX, centerY - halfWidth)
            close()
        }
        canvas.drawPath(path, paint)
    }

    private fun drawHandBar(
        canvas: Canvas,
        paint: Paint,
        centerX: Float,
        centerY: Float,
        length: Int,
        width: Int
    ) {
        paint.strokeWidth = width.toFloat()
        canvas.drawLine(centerX, centerY, centerX + length, centerY, paint)
    }

    private fun drawHandArc(canvas: Canvas, length: Int, width: Int) {
        canvas.save()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = width.toFloat()
        val colors = intArrayOf(Color.TRANSPARENT, Color.WHITE)
        val positions = floatArrayOf(0.2F, 1F)
        val gradient: Shader = SweepGradient(centerX, centerY, colors, positions)
        paint.shader = gradient
        canvas.drawCircle(centerX, centerY, length.toFloat(), paint)
        paint.shader = null
        canvas.restore()
    }

    private fun drawInnerCircle(canvas: Canvas) {
        if (config.handShape === HandShape.ARC) return
        paint.apply {
            colorFilter = secondaryColorFilter
            alpha = 255
            canvas.drawCircle(centerX, centerY, config.innerCircleRadius * radius, this)
            colorFilter = null
            color = Color.BLACK
            strokeWidth = 2F
            canvas.drawPoint(centerX, centerY, this)
            style = Paint.Style.STROKE
            color = Color.WHITE
        }
        canvas.drawCircle(centerX, centerY, config.innerCircleRadius * radius, paint)

    }

    private fun drawTriangle(
        canvas: Canvas,
        paint: Paint,
        baseX: Float,
        baseY: Float,
        width: Float,
        height: Float
    ) {
        val halfWidth = width / 2
        val path = Path().apply {
            moveTo(baseX - halfWidth, baseY)
            lineTo(baseX + halfWidth, baseY)
            lineTo(baseX, baseY + height)
            close()
        }
        canvas.drawPath(path, paint)
    }

    private fun drawBackgroundArc(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        radius: Int,
        angle: Double
    ) {
        if (config.decoration !== AnalogClockConfig.Decoration.MINUTE_HAND) return
        canvas.save()
        paint.alpha = 70
        paint.colorFilter = customColorFilter
        val colors = intArrayOf(Color.TRANSPARENT, Color.WHITE)
        val positions = floatArrayOf(0.5F, 1F)
        val gradient = SweepGradient(centerX, centerY, colors, positions)
        val rotate = radiansToDegrees(angle).toFloat()
        val gradientMatrix = Matrix()
        gradientMatrix.preRotate(rotate, centerX, centerY)
        gradient.setLocalMatrix(gradientMatrix)
        paint.shader = gradient
        paint.style = Paint.Style.FILL_AND_STROKE
        canvas.drawCircle(centerX, centerY, config.handLengthMinutes * radius, paint)
        paint.shader = null
        canvas.restore()
    }

    private fun applyShader(paint: Paint, centerX: Float, centerY: Float, radius: Int) {
        if (config.decoration !== AnalogClockConfig.Decoration.LABELS) return
        val x1 = centerX - radius
        val y1 = centerY - radius
        val hsv = FloatArray(3)
        Color.colorToHSV(customColor, hsv)
        hsv[2] *= 1.2F
        hsv[1] *= .90f
        val accent = Color.HSVToColor(hsv)
        val colors = intArrayOf(Color.WHITE, accent)
        val positions = floatArrayOf(0.4F, 1F)
        paint.shader = LinearGradient(x1, y1, centerX, centerY, colors, positions, Shader.TileMode.MIRROR)
    }

    private val isTextureDecoration: Boolean
        get() = config.decoration === AnalogClockConfig.Decoration.GOLD || config.decoration === AnalogClockConfig.Decoration.COPPER || config.decoration === AnalogClockConfig.Decoration.RUST

    private fun applyGoldShader(paint: Paint, centerX: Float, centerY: Float, radius: Int) {
        if (!isTextureDecoration) return
        var resID = R.drawable.gold
        when (config.decoration) {
            AnalogClockConfig.Decoration.GOLD -> resID = R.drawable.gold
            AnalogClockConfig.Decoration.COPPER -> resID = R.drawable.copper
            AnalogClockConfig.Decoration.RUST -> resID = R.drawable.rust
            else -> {}
        }
        val bitmap = BitmapFactory.decodeResource(resources, resID)
        val shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        val light = Color.parseColor("#FFFFFF")
        val dark = Color.parseColor("#BBBBBB")
        val colors = intArrayOf(light, dark, light, dark, light)
        val positions = floatArrayOf(0.15F, 0.25F, 0.55F, 0.65F, 0.9F)
        val x1 = centerX - radius
        val y1 = centerY - radius
        val x2 = centerX + radius
        val y2 = centerY + radius
        val shader2 = LinearGradient(x1, y1, x2, y2, colors, positions, Shader.TileMode.MIRROR)
        paint.shader = ComposeShader(shader, shader2, PorterDuff.Mode.MULTIPLY)
    }

    private fun applyPureTexture(paint: Paint) {
        if (!isTextureDecoration) return
        var resID = R.drawable.gold
        when (config.decoration) {
            AnalogClockConfig.Decoration.GOLD -> resID = R.drawable.gold
            AnalogClockConfig.Decoration.COPPER -> resID = R.drawable.copper
            AnalogClockConfig.Decoration.RUST -> resID = R.drawable.rust
            else -> {}
        }
        val bitmap = BitmapFactory.decodeResource(resources, resID)
        val shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        paint.shader = shader
    }

    private fun drawOuterCircle(canvas: Canvas) {
        if (config.outerCircleWidth == 0.00F) return
        paint.apply {
            alpha = 255
            color = Color.WHITE
            colorFilter = secondaryColorFilter
            style = Paint.Style.STROKE
            strokeWidth = config.outerCircleWidth * radius
        }
        canvas.drawCircle(centerX, centerY, config.outerCircleRadius * radius, paint)
    }

    private fun drawTicks(canvas: Canvas, centerX: Float, centerY: Float, radius: Int) {
        // ticks
        paint.alpha = 255
        paint.colorFilter = secondaryColorFilter
        paint.style = Paint.Style.FILL //filled circle for every hour
        for (minuteCounter in 0..59) {
            val isHourTick = minuteCounter % 5 == 0
            val tickStyle = if (isHourTick) config.tickStyleHours else config.tickStyleMinutes
            val tickStart = if (isHourTick) config.tickStartHours else config.tickStartMinutes
            val tickLength =
                if (isHourTick) config.tickLengthHours else config.tickLengthMinutes
            val width =
                (if (isHourTick) config.tickWidthHours * radius else config.tickWidthMinutes * radius).toInt()
            paint.strokeWidth = width.toFloat()
            val tickStartX =
                (centerX + tickStart * radius * MINUTE_ANGLES_COSINE[minuteCounter]).toFloat()
            val tickStartY =
                (centerY + tickStart * radius * MINUTE_ANGLES_SINE[minuteCounter]).toFloat()
            val tickEndX =
                (centerX + (tickStart + tickLength) * radius * MINUTE_ANGLES_COSINE[minuteCounter]).toFloat()
            val tickEndY =
                (centerY + (tickStart + tickLength) * radius * MINUTE_ANGLES_SINE[minuteCounter]).toFloat()
            when (tickStyle) {
                TickStyle.NONE -> {}
                TickStyle.CIRCLE -> if (isHourTick && config.emphasizeHour12 && minuteCounter == 45) {
                    // for "12" digit draw a special marker
                    val triangleHeight = tickLength * radius * 1.2F
                    val triangleWidth = triangleHeight * 1.2F
                    drawTriangle(
                        canvas,
                        paint,
                        tickEndX,
                        tickEndY - triangleHeight * .1F,
                        triangleWidth,
                        triangleHeight
                    )
                } else {
                    val roundTickRadius = tickLength * .5F * radius
                    val roundTickCenterX =
                        centerX + (tickStart + tickLength * .5F) * radius.toFloat() * MINUTE_ANGLES_COSINE[minuteCounter].toFloat()
                    val roundTickCenterY =
                        centerY + (tickStart + tickLength * .5F) * radius.toFloat() * MINUTE_ANGLES_SINE[minuteCounter].toFloat()
                    canvas.drawCircle(roundTickCenterX, roundTickCenterY, roundTickRadius, paint)
                }
                else -> canvas.drawLine(tickStartX, tickStartY, tickEndX, tickEndY, paint)
            }
        }
    }

    private fun drawHourDigits(canvas: Canvas, centerX: Float, centerY: Float, radius: Int) {
        if (config.digitStyle === AnalogClockConfig.DigitStyle.NONE) return
        paint.typeface = typeface
        val fontSizeBig = config.fontSize * radius
        val fontSizeSmall = 0.75F * config.fontSize * radius
        val textSizeBig = fontSizeForWidth("5", fontSizeBig, paint)
        val textSizeSmall = fontSizeForWidth("5", fontSizeSmall, paint)
        val minTickStart = config.tickStartHours - config.tickLengthHours * 0.5F
        val maxTickStart = config.tickStartHours + config.tickLengthHours * 1.5F
        val defaultDigitPosition = config.digitPosition * radius
        val maxDigitPosition = minTickStart * radius
        val minDigitPosition = maxTickStart * radius
        for (digitCounter in 0..11) {
            val currentHour = (digitCounter + 2) % 12 + 1
            if (config.highlightQuarterOfHour && currentHour % 3 == 0) {
                // 3,6,9,12
                paint.colorFilter = customColorFilter
                paint.textSize = textSizeBig
                paint.typeface = boldTypeface
            } else {
                paint.colorFilter = secondaryColorFilter
                paint.textSize = textSizeSmall
                paint.typeface = typeface
            }
            val currentHourText = getHourTextOfDigitStyle(currentHour)

            val bounds = Rect()
            paint.getTextBounds(currentHourText, 0, currentHourText.length, bounds)
            val textWidth = paint.measureText(currentHourText, 0, currentHourText.length)
            val textHeight = bounds.height().toFloat()

            // find a position for the digits which does not interfere with the ticks
            val distanceDigitCenterToBorder =
                distanceHourTextBoundsCenterToBorder(currentHour, textWidth, textHeight)
            var correctedAbsoluteDigitPosition = defaultDigitPosition
            if (config.digitPosition < config.tickStartHours) {
                if (defaultDigitPosition + distanceDigitCenterToBorder > maxDigitPosition) {
                    correctedAbsoluteDigitPosition = maxDigitPosition - distanceDigitCenterToBorder
                }
            } else if (config.digitPosition >= config.tickStartHours) {
                if (defaultDigitPosition - distanceDigitCenterToBorder < minDigitPosition) {
                    correctedAbsoluteDigitPosition = minDigitPosition + distanceDigitCenterToBorder
                }
            }
            var x =
                (centerX + correctedAbsoluteDigitPosition * HOUR_ANGLES_COSINE[digitCounter]).toFloat()
            var y =
                (centerY + correctedAbsoluteDigitPosition * HOUR_ANGLES_SINE[digitCounter]).toFloat()

            // move center of text bounding box to x/y
            x -= (textWidth / 2.0).toFloat()
            y -= textHeight / 2F + 1F
            canvas.drawText(currentHourText, x, y + textHeight, paint)
        }
    }

    private fun distanceHourTextBoundsCenterToBorder(
        currentHour: Int,
        textWidth: Float,
        textHeight: Float
    ): Float {
        return when (currentHour) {
            6, 12 -> textHeight / 2F
            3, 9 -> textWidth / 2F
            2, 4, 8, 10 -> abs(textWidth / 2F / COSINE_OF_30_DEGREE)
            else -> abs(textHeight / 2F / COSINE_OF_30_DEGREE)
        }
    }

    private fun getHourTextOfDigitStyle(currentHour: Int): String {
        return if (config.digitStyle === AnalogClockConfig.DigitStyle.ARABIC) currentHour.toString() else ROMAN_DIGITS[currentHour - 1]
    }

    private fun radiansToDegrees(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

    companion object {
        /**
         * precalculated sine/cosine values
         */
        private val COSINE_OF_30_DEGREE = cos(Math.PI / 6.0).toFloat()
        private val MINUTE_ANGLES_SINE = DoubleArray(60)
        private val MINUTE_ANGLES_COSINE = DoubleArray(60)
        private val HOUR_ANGLES_SINE = DoubleArray(12)
        private val HOUR_ANGLES_COSINE = DoubleArray(12)

        init {
            // calculate angles and its sine/cosine for all 60 minute hands
            for (minuteCounter in 0..59) {
                val angle = minuteCounter.toDouble() * (Math.PI / 30.0)
                MINUTE_ANGLES_SINE[minuteCounter] = sin(angle)
                MINUTE_ANGLES_COSINE[minuteCounter] = cos(angle)
            }

            // calculate angles and its sine/cosine for all 12 hour hands
            for (hourCounter in 0..11) {
                val angle = hourCounter.toDouble() * (Math.PI / 6.0)
                HOUR_ANGLES_SINE[hourCounter] = sin(angle)
                HOUR_ANGLES_COSINE[hourCounter] = cos(angle)
            }
        }

        private fun fontSizeForWidth(dummyText: String, destWidth: Float, paint: Paint): Float {
            val dummyFontSize = 48F
            paint.textSize = dummyFontSize
            val bounds = Rect()
            paint.getTextBounds(dummyText, 0, dummyText.length, bounds)
            return dummyFontSize * destWidth / bounds.width()
        }
    }
}