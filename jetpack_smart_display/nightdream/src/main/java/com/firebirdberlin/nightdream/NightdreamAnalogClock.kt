package com.firebirdberlin.nightdream

import android.content.Context
import android.graphics.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NightdreamAnalogClock(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background),
    color: ColorFilter = ColorFilter.tint(color = MaterialTheme.colors.primary),
    hour: Int,
    minute: Int,
    second: Int
) {
    init(
        context = LocalContext.current,
        style = AnalogClockConfig.Style.DEFAULT,
        allowSecondHand = true
    )

    val configuration = LocalConfiguration.current

    val width = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }.toInt()
    val height = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }.toInt()

    Column(
        modifier = modifier
    ) {
        OnDraw(
            modifier = modifier,
            width = width,
            height = height,
            hour = hour,
            minute = minute,
            second = second
        )
    }
}

@Composable
fun OnDraw(
    modifier: Modifier,
    width: Int,
    height: Int,
    hour: Int,
    minute: Int,
    second: Int
) {
    Canvas(
        modifier = modifier,
    ) {
        drawIntoCanvas {
            val canvas = it.nativeCanvas

            centerX = width / 2F
            centerY = height / 2F
            radius = height / 2 - 20
            paint.isAntiAlias = true
            paint.color = Color.WHITE

            val hourAngle = (hour.toDouble() / 6.0 * Math.PI - Math.PI / 2.0 + minute.toDouble() / 60.0 * Math.PI / 6.0)
            val minAngle = minute.toDouble() / 30.0 * Math.PI - Math.PI / 2.0
            val secAngle = second.toDouble() / 30.0 * Math.PI - Math.PI / 2.0
            paint.alpha = 255
            paint.color = Color.WHITE
            drawOuterCircle(
                canvas = canvas
            )
            drawTicks(
                canvas = canvas,
                centerX = centerX,
                centerY = centerY,
                radius = radius
            )
            drawHourDigits(
                canvas = canvas,
                centerX = centerX,
                centerY = centerY,
                radius = radius
            )
            drawHands(
                canvas = canvas,
                centerX = centerX,
                centerY = centerY,
                radius = radius,
                hourAngle = hourAngle,
                minAngle = minAngle,
                secAngle = secAngle
            )
        }
    }
}

private fun drawTicks(
    canvas: Canvas,
    centerX: Float,
    centerY: Float,
    radius: Int
) {
    paint.apply {
        alpha = 255
        colorFilter = secondaryColorFilter
        style = Paint.Style.FILL //filled circle for every hour
    }
    for (minuteCounter in 0..59) {
        val isHourTick = minuteCounter % 5 == 0
        val tickStyle = if (isHourTick) config.tickStyleHours else config.tickStyleMinutes
        val tickStart = if (isHourTick) config.tickStartHours else config.tickStartMinutes
        val tickLength = if (isHourTick) config.tickLengthHours else config.tickLengthMinutes
        val width = (if (isHourTick) config.tickWidthHours * radius else config.tickWidthMinutes * radius).toInt()
        paint.strokeWidth = width.toFloat()
        val tickStartX = (centerX + tickStart * radius * MINUTE_ANGLES_COSINE[minuteCounter]).toFloat()
        val tickStartY = (centerY + tickStart * radius * MINUTE_ANGLES_SINE[minuteCounter]).toFloat()
        val tickEndX = (centerX + (tickStart + tickLength) * radius * MINUTE_ANGLES_COSINE[minuteCounter]).toFloat()
        val tickEndY = (centerY + (tickStart + tickLength) * radius * MINUTE_ANGLES_SINE[minuteCounter]).toFloat()
        when (tickStyle) {
            AnalogClockConfig.TickStyle.NONE -> {}
            AnalogClockConfig.TickStyle.CIRCLE ->
                if (isHourTick && config.emphasizeHour12 && minuteCounter == 45) {
                    val triangleHeight = tickLength * radius * 1.2F
                    val triangleWidth = triangleHeight * 1.2F
                    drawTriangle(
                        canvas = canvas,
                        paint = paint,
                        baseX = tickEndX,
                        baseY = tickEndY - triangleHeight * .1F,
                        width = triangleWidth,
                        height = triangleHeight
                    )
                } else {
                    val roundTickRadius = tickLength * .5F * radius
                    val roundTickCenterX = centerX + (tickStart + tickLength * .5F) * radius.toFloat() * MINUTE_ANGLES_COSINE[minuteCounter].toFloat()
                    val roundTickCenterY = centerY + (tickStart + tickLength * .5F) * radius.toFloat() * MINUTE_ANGLES_SINE[minuteCounter].toFloat()
                    canvas.drawCircle(
                        roundTickCenterX,
                        roundTickCenterY,
                        roundTickRadius,
                        paint)
                }
            else -> canvas.drawLine(
                tickStartX,
                tickStartY,
                tickEndX,
                tickEndY,
                paint
            )
        }
    }
}

private fun drawHourDigits(
    canvas: Canvas,
    centerX: Float,
    centerY: Float,
    radius: Int
) {
    if (config.digitStyle === AnalogClockConfig.DigitStyle.NONE) return

    paint.typeface = typeface
    val fontSizeBig = config.fontSize * radius
    val fontSizeSmall = 0.75F * config.fontSize * radius
    val textSizeBig = fontSizeForWidth(
        dummyText = "5",
        destWidth = fontSizeBig,
        paint = paint
    )
    val textSizeSmall = fontSizeForWidth(
        dummyText= "5",
        destWidth = fontSizeSmall,
        paint = paint
    )
    val minTickStart = config.tickStartHours - config.tickLengthHours * 0.5F
    val maxTickStart = config.tickStartHours + config.tickLengthHours * 1.5F
    val defaultDigitPosition = config.digitPosition * radius
    val maxDigitPosition = minTickStart * radius
    val minDigitPosition = maxTickStart * radius
    for (digitCounter in 0..11) {
        val currentHour = (digitCounter + 2) % 12 + 1
        paint.apply {
            if (config.highlightQuarterOfHour && currentHour % 3 == 0) {
                // 3,6,9,12
                colorFilter = customColorFilter
                textSize = textSizeBig
                typeface = boldTypeface
            } else {
                colorFilter = secondaryColorFilter
                textSize = textSizeSmall
                typeface = typeface
            }
        }
        val currentHourText = getHourTextOfDigitStyle(
            currentHour = currentHour
        )

        val bounds = Rect()
        paint.getTextBounds(
            currentHourText,
            0,
            currentHourText.length,
            bounds
        )
        val textWidth = paint.measureText(
            currentHourText,
            0,
            currentHourText.length
        )
        val textHeight = bounds.height().toFloat()

        val distanceDigitCenterToBorder = distanceHourTextBoundsCenterToBorder(
            currentHour = currentHour,
            textWidth = textWidth,
            textHeight = textHeight
        )
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
        var x = (centerX + correctedAbsoluteDigitPosition * HOUR_ANGLES_COSINE[digitCounter]).toFloat()
        var y = (centerY + correctedAbsoluteDigitPosition * HOUR_ANGLES_SINE[digitCounter]).toFloat()

        x -= (textWidth / 2.0).toFloat()
        y -= textHeight / 2F + 1F
        canvas.drawText(
            currentHourText,
            x,
            y + textHeight,
            paint
        )
    }
}

private fun drawHands(
    canvas: Canvas,
    centerX: Float,
    centerY: Float,
    radius: Int,
    hourAngle: Double,
    minAngle: Double,
    secAngle: Double
) {
    paint.style = Paint.Style.FILL
    paint.shader = null
    // minute hand
    canvas.save()
    if (config.handShape == AnalogClockConfig.HandShape.ARC) {
        paint.colorFilter = customColorFilter
    }
    canvas.rotate(
        radiansToDegrees(minAngle),
        centerX,
        centerY
    )
    drawHand(
        canvas = canvas,
        paint = paint,
        baseX = centerX,
        baseY = centerY,
        height = (config.handLengthMinutes * radius).toInt(),
        width = (config.handWidthMinutes * radius).toInt()
    )
    canvas.restore()

    // second hand
    if (config.showSecondHand) {
        canvas.save()
        if (config.handShape == AnalogClockConfig.HandShape.ARC) {
            paint.colorFilter = secondaryColorFilter
        }
        canvas.rotate(
            radiansToDegrees(secAngle),
            centerX,
            centerY
        )
        drawHand(
            canvas = canvas,
            paint = paint,
            baseX = centerX,
            baseY = centerY,
            height = (config.handLengthMinutes * radius).toInt(),
            width = (config.handWidthMinutes / 3 * radius).toInt()
        )
        canvas.restore()
    }
    // hour hand
    canvas.save()
    if (config.handShape == AnalogClockConfig.HandShape.ARC) {
        paint.colorFilter = secondaryColorFilter
    }
    canvas.rotate(
        radiansToDegrees(hourAngle),
        centerX,
        centerY
    )
    drawHand(
        canvas = canvas,
        paint = paint,
        baseX = centerX,
        baseY = centerY,
        height = (config.handLengthHours * radius).toInt(),
        width = (config.handWidthHours * radius).toInt()
    )
    canvas.restore()
    drawInnerCircle(
        canvas = canvas
    )
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
        AnalogClockConfig.HandShape.ARC -> drawHandArc(
            canvas = canvas,
            length = height,
            width = width
        )
        AnalogClockConfig.HandShape.BAR -> drawHandBar(
            canvas = canvas,
            paint = paint,
            centerX = baseX,
            centerY = baseY,
            length = height,
            width = width
        )
        AnalogClockConfig.HandShape.TRIANGLE -> drawHandTriangle(
            canvas = canvas,
            paint = paint,
            centerX = baseX,
            centerY = baseY,
            length = height,
            width = width
        )
    }
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
    canvas.drawLine(
        centerX,
        centerY,
        centerX + length,
        centerY,
        paint
    )
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
    canvas.drawPath(
        path,
        paint
    )
}

private fun drawInnerCircle(
    canvas: Canvas
) {
    if (config.handShape === AnalogClockConfig.HandShape.ARC) return

    paint.apply {
        colorFilter = secondaryColorFilter
        alpha = 255
        canvas.drawCircle(
            centerX,
            centerY,
            config.innerCircleRadius * radius,
            this
        )
        colorFilter = null
        color = Color.BLACK
        strokeWidth = 2F
        canvas.drawPoint(
            centerX,
            centerY,
            this
        )
        style = Paint.Style.STROKE
        color = Color.WHITE
    }
    canvas.drawCircle(
        centerX,
        centerY,
        config.innerCircleRadius * radius,
        paint
    )
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
    canvas.drawPath(
        path,
        paint
    )
}

private fun drawHandArc(
    canvas: Canvas,
    length: Int,
    width: Int
) {
    canvas.save()
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = width.toFloat()
    val colors = intArrayOf(Color.TRANSPARENT, Color.WHITE)
    val positions = floatArrayOf(0.2F, 1F)
    val gradient = SweepGradient(
        centerX,
        centerY,
        colors,
        positions
    )
    paint.shader = gradient
    canvas.drawCircle(
        centerX,
        centerY,
        length.toFloat(),
        paint
    )
    paint.shader = null
    canvas.restore()
}

private fun radiansToDegrees(rad: Double): Float {
    return (rad * 180.0 / Math.PI).toFloat()
}

private fun fontSizeForWidth(
    dummyText: String,
    destWidth: Float,
    paint: Paint
): Float {
    val dummyFontSize = 48F
    paint.textSize = dummyFontSize
    val bounds = Rect()
    paint.getTextBounds(
        dummyText,
        0,
        dummyText.length,
        bounds
    )
    return dummyFontSize * destWidth / bounds.width()
}

private fun getHourTextOfDigitStyle(
    currentHour: Int
): String {
    return if (config.digitStyle === AnalogClockConfig.DigitStyle.ARABIC) currentHour.toString() else ROMAN_DIGITS[currentHour - 1]
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

private fun drawOuterCircle(
    canvas: Canvas
) {
    if (config.outerCircleWidth == 0.00F) return
    paint.apply {
        alpha = 255
        color = Color.WHITE
        colorFilter = secondaryColorFilter
        style = Paint.Style.STROKE
        strokeWidth = config.outerCircleWidth * radius
    }
    canvas.drawCircle(
        centerX,
        centerY,
        config.outerCircleRadius * radius,
        paint
    )
}

private fun init(
    context: Context,
    style: AnalogClockConfig.Style,
    allowSecondHand: Boolean
) {
    for (minuteCounter in 0..59) {
        val angle = minuteCounter.toDouble() * (Math.PI / 30.0)
        MINUTE_ANGLES_SINE[minuteCounter] = sin(angle)
        MINUTE_ANGLES_COSINE[minuteCounter] = cos(angle)
    }
    for (hourCounter in 0..11) {
        val angle = hourCounter.toDouble() * (Math.PI / 6.0)
        HOUR_ANGLES_SINE[hourCounter] = sin(angle)
        HOUR_ANGLES_COSINE[hourCounter] = cos(angle)
    }

    config = AnalogClockConfig(context, style)
    config.showSecondHand = allowSecondHand && config.showSecondHand
    typeface = FontCache[context, config.fontUri]
    boldTypeface = Typeface.create(typeface, Typeface.BOLD)
}

private val ROMAN_DIGITS = arrayOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII")
private val COSINE_OF_30_DEGREE = cos(Math.PI / 6.0).toFloat()
private val MINUTE_ANGLES_SINE = DoubleArray(60)
private val MINUTE_ANGLES_COSINE = DoubleArray(60)
private val HOUR_ANGLES_SINE = DoubleArray(12)
private val HOUR_ANGLES_COSINE = DoubleArray(12)

private var centerX = 0F
private var centerY = 0F
private var radius = 0
private var paint = Paint()
private lateinit var config: AnalogClockConfig
private var customColorFilter = LightingColorFilter(Color.WHITE, 1)
private var secondaryColorFilter = LightingColorFilter(Color.WHITE, 1)
private var typeface = Typeface.DEFAULT
private var boldTypeface = Typeface.DEFAULT
