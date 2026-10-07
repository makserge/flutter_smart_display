package com.smsoft.smartdisplay.ui.composable.clock.nightdream

import android.graphics.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.utils.getColor
import com.smsoft.smartdisplay.ui.screen.clock.ClockViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
fun NightdreamAnalogClock(
    modifier: Modifier = Modifier,
    hour: Int,
    minute: Int,
    second: Int,
    viewModel: ClockViewModel,
    primaryColor: Color,
    secondaryColor: Color
) {
    val config = remember { AnalogClockConfig() }

    Init(
        viewModel = viewModel,
        config = config,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor
    )
    OnDraw(
        modifier = modifier,
        config = config,
        // The config is changed in place, so its values (and the colours) form the redraw key
        settingsKey = 31 * (31 * config.hashCode() + primaryColor.hashCode()) + secondaryColor.hashCode(),
        hour = hour,
        minute = minute,
        // Without a second hand the dial only has to be redrawn once a minute
        second = if (config.showSecondHand) second else 0
    )
}

@Composable
private fun OnDraw(
    modifier: Modifier,
    config: AnalogClockConfig,
    settingsKey: Int,
    hour: Int,
    minute: Int,
    second: Int
) {
    // A new key gives a new Canvas, so changed settings are drawn even while the time is unchanged
    key(settingsKey) {
        Canvas(
            modifier = modifier
                .fillMaxSize(),
        ) {
            drawIntoCanvas {
                val canvas = it.nativeCanvas

                val centerX = size.width / 2F
                val centerY = size.height / 2F
                // The shorter side decides, so the dial also fits in portrait. The margin is in dp;
                // the radius is chosen so that the outermost hand, tick or digit ends at the margin.
                val maxRadius = (min(size.width, size.height) / 2F - DIAL_MARGIN.toPx()).coerceAtLeast(1F)
                val radius = (maxRadius / dialExtent(maxRadius.toInt(), config)).toInt()
                paint.isAntiAlias = true
                paint.color = android.graphics.Color.WHITE

                val hourAngle = (hour.toDouble() / 6.0 * Math.PI - Math.PI / 2.0 + minute.toDouble() / 60.0 * Math.PI / 6.0)
                val minAngle = minute.toDouble() / 30.0 * Math.PI - Math.PI / 2.0
                val secAngle = second.toDouble() / 30.0 * Math.PI - Math.PI / 2.0
                paint.alpha = 255
                paint.color = android.graphics.Color.WHITE
                drawTicks(
                    canvas = canvas,
                    centerX = centerX,
                    centerY = centerY,
                    radius = radius,
                    config = config
                )
                drawHourDigits(
                    canvas = canvas,
                    centerX = centerX,
                    centerY = centerY,
                    radius = radius,
                    config = config
                )
                drawHands(
                    canvas = canvas,
                    centerX = centerX,
                    centerY = centerY,
                    radius = radius,
                    hourAngle = hourAngle,
                    minAngle = minAngle,
                    secAngle = secAngle,
                    outlineWidth = HUB_OUTLINE_WIDTH.toPx(),
                    config = config
                )
            }
        }
    }
}

/**
 * How far the hands, ticks and digits reach from the centre, in units of the radius.
 */
private fun dialExtent(
    radius: Int,
    config: AnalogClockConfig
): Float {
    // Nothing is drawn out to the full radius, so start from the hands instead of 1: with the
    // default ticks ending at 0.942 the dial stayed about 6 % smaller than the page allows. Never
    // below MIN_DIAL_EXTENT, or with ticks and digits off the hand length setting would zoom
    // the whole dial instead of changing the hands.
    var extent = maxOf(config.handLengthMinutes, config.handLengthHours, MIN_DIAL_EXTENT)
    if (config.tickStyleMinutes != AnalogClockConfig.TickStyle.NONE) {
        extent = max(extent, config.tickStartMinutes + config.tickLengthMinutes)
    }
    if (config.tickStyleHours != AnalogClockConfig.TickStyle.NONE) {
        // The triangle at 12 sticks out a little further than the circles
        extent = max(extent, config.tickStartHours + config.tickLengthHours * 1.12F)
    }
    val digitsExtent = drawHourDigits(
        canvas = null,
        centerX = 0F,
        centerY = 0F,
        radius = radius,
        config = config
    )
    return max(extent, digitsExtent / radius)
}

private fun drawTicks(
    canvas: Canvas,
    centerX: Float,
    centerY: Float,
    radius: Int,
    config: AnalogClockConfig
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
        val tickStartX = (centerX + tickStart * radius * MINUTE_ANGLES_COS[minuteCounter]).toFloat()
        val tickStartY = (centerY + tickStart * radius * MINUTE_ANGLES_SIN[minuteCounter]).toFloat()
        val tickEndX = (centerX + (tickStart + tickLength) * radius * MINUTE_ANGLES_COS[minuteCounter]).toFloat()
        val tickEndY = (centerY + (tickStart + tickLength) * radius * MINUTE_ANGLES_SIN[minuteCounter]).toFloat()
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
                    val roundTickCenterX = centerX + (tickStart + tickLength * .5F) * radius.toFloat() * MINUTE_ANGLES_COS[minuteCounter].toFloat()
                    val roundTickCenterY = centerY + (tickStart + tickLength * .5F) * radius.toFloat() * MINUTE_ANGLES_SIN[minuteCounter].toFloat()
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

/**
 * Draws the hour digits, or only measures them when [canvas] is null.
 * Returns how far the digits reach from the centre, in px.
 */
private fun drawHourDigits(
    canvas: Canvas?,
    centerX: Float,
    centerY: Float,
    radius: Int,
    config: AnalogClockConfig
): Float {
    if (config.digitStyle === AnalogClockConfig.DigitStyle.NONE) return 0F

    paint.typeface = regularTypeface
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
    val bounds = Rect()
    var extent = 0F
    for (digitCounter in 0..11) {
        val currentHour = (digitCounter + 2) % 12 + 1
        paint.apply {
            // The global typefaces are named apart from Paint.typeface: inside apply a plain
            // "typeface = typeface" assigned the paint's own (often bold) typeface back to itself
            if (config.highlightQuarterOfHour && currentHour % 3 == 0) {
                // 3,6,9,12
                colorFilter = customColorFilter
                textSize = textSizeBig
                typeface = boldTypeface
            } else {
                colorFilter = secondaryColorFilter
                textSize = textSizeSmall
                typeface = regularTypeface
            }
        }
        val currentHourText = getHourTextOfDigitStyle(
            currentHour = currentHour,
            digitStyle = config.digitStyle
        )

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
        extent = max(extent, correctedAbsoluteDigitPosition + distanceDigitCenterToBorder)
        if (canvas == null) continue

        var x = (centerX + correctedAbsoluteDigitPosition * HOUR_ANGLES_COS[digitCounter]).toFloat()
        var y = (centerY + correctedAbsoluteDigitPosition * HOUR_ANGLES_SIN[digitCounter]).toFloat()

        x -= (textWidth / 2.0).toFloat()
        y -= textHeight / 2F + 1F
        canvas.drawText(
            currentHourText,
            x,
            y + textHeight,
            paint
        )
    }
    return extent
}

private fun drawHands(
    canvas: Canvas,
    centerX: Float,
    centerY: Float,
    radius: Int,
    hourAngle: Double,
    minAngle: Double,
    secAngle: Double,
    outlineWidth: Float,
    config: AnalogClockConfig
) {
    paint.style = Paint.Style.FILL
    paint.shader = null
    // Hands are in the secondary colour; set it here instead of relying on what was drawn last
    paint.colorFilter = secondaryColorFilter
    paint.alpha = 255
    // minute hand
    canvas.save()
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
        width = (config.handWidthMinutes * radius).toInt(),
        handStyle = config.handStyle
    )
    canvas.restore()

    // hour hand
    canvas.save()
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
        width = (config.handWidthHours * radius).toInt(),
        handStyle = config.handStyle
    )
    canvas.restore()
    // second hand, last and in the primary colour like the emphasized numerals: in the hands'
    // colour, and drawn before the hour hand, it disappeared wherever it crossed the hour hand
    if (config.showSecondHand) {
        paint.colorFilter = customColorFilter
        canvas.save()
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
            width = (config.handWidthMinutes / 3 * radius).toInt(),
            handStyle = config.handStyle
        )
        canvas.restore()
    }
    drawInnerCircle(
        canvas = canvas,
        centerX = centerX,
        centerY = centerY,
        // The setting is a share of the dial radius; it used to be drawn as px (0.045 px: invisible)
        innerCircleRadius = config.innerCircleRadius * radius,
        outlineWidth = outlineWidth
    )
}

private fun drawHand(
    canvas: Canvas,
    paint: Paint,
    baseX: Float,
    baseY: Float,
    height: Int,
    width: Int,
    handStyle: AnalogClockConfig.HandStyle
) {
    when (handStyle) {
        AnalogClockConfig.HandStyle.BAR -> drawHandBar(
            canvas = canvas,
            paint = paint,
            centerX = baseX,
            centerY = baseY,
            length = height,
            width = width
        )
        AnalogClockConfig.HandStyle.TRIANGLE -> drawHandTriangle(
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
    val path = Path().apply {
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
    canvas: Canvas,
    centerX: Float,
    centerY: Float,
    innerCircleRadius: Float,
    outlineWidth: Float
) {
    paint.apply {
        colorFilter = secondaryColorFilter
        alpha = 255
        style = Paint.Style.FILL
        canvas.drawCircle(
            centerX,
            centerY,
            innerCircleRadius,
            this
        )
        colorFilter = null
        color = android.graphics.Color.BLACK
        strokeWidth = 2 * outlineWidth
        canvas.drawPoint(
            centerX,
            centerY,
            this
        )
        style = Paint.Style.STROKE
        color = android.graphics.Color.WHITE
        strokeWidth = outlineWidth
    }
    canvas.drawCircle(
        centerX,
        centerY,
        innerCircleRadius,
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

private fun radiansToDegrees(rad: Double): Float {
    return (rad * 180.0 / Math.PI).toFloat()
}

@Suppress("SameParameterValue")
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
    currentHour: Int,
    digitStyle: AnalogClockConfig.DigitStyle
): String {
    return if (digitStyle === AnalogClockConfig.DigitStyle.ARABIC) {
        currentHour.toString()
    } else {
        ROMAN_DIGITS[currentHour - 1]
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
        2, 4, 8, 10 -> abs(textWidth / 2F / COS_OF_30_DEGREE)
        else -> abs(textHeight / 2F / COS_OF_30_DEGREE)
    }
}

@Composable
private fun Init(
    viewModel: ClockViewModel,
    config: AnalogClockConfig,
    primaryColor: Color,
    secondaryColor: Color
) {
    config.InitDataStore(
        viewModel = viewModel
    )

    // Built only when the font or a colour changes, not on every tick
    val context = LocalContext.current
    val fontUri = config.fontUri
    val typefaces = remember(fontUri) {
        val regular = FontCache[context, fontUri] ?: Typeface.DEFAULT
        Pair(regular, Typeface.create(regular, Typeface.BOLD))
    }
    regularTypeface = typefaces.first
    boldTypeface = typefaces.second
    customColorFilter = remember(primaryColor) {
        LightingColorFilter(getColor(primaryColor), 1)
    }
    secondaryColorFilter = remember(secondaryColor) {
        LightingColorFilter(getColor(secondaryColor), 1)
    }
}

// Gap between the dial and the shorter side of the page
private val DIAL_MARGIN = 12.dp
// Where the default minute ticks end, in units of the radius; the dial extent never goes below.
private const val MIN_DIAL_EXTENT = 0.94F
// Outline of the centre hub
private val HUB_OUTLINE_WIDTH = 1.dp

private val ROMAN_DIGITS = arrayOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII")
private val COS_OF_30_DEGREE = cos(Math.PI / 6.0).toFloat()
// Filled when the class loads, so the very first frame is already drawn at the right angles
private val MINUTE_ANGLES_SIN = DoubleArray(60) { sin(it * Math.PI / 30.0) }
private val MINUTE_ANGLES_COS = DoubleArray(60) { cos(it * Math.PI / 30.0) }
private val HOUR_ANGLES_SIN = DoubleArray(12) { sin(it * Math.PI / 6.0) }
private val HOUR_ANGLES_COS = DoubleArray(12) { cos(it * Math.PI / 6.0) }

private var paint = Paint()
private var customColorFilter = LightingColorFilter(android.graphics.Color.WHITE, 1)
private var secondaryColorFilter = LightingColorFilter(android.graphics.Color.WHITE, 1)
private var regularTypeface: Typeface = Typeface.DEFAULT
private var boldTypeface: Typeface = Typeface.DEFAULT
