package com.smsoft.smartdisplay.ui.composable.clock.clockview2

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.text.TextPaint
import android.util.TypedValue
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.getParam
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ClockView2(
    modifier: Modifier = Modifier,
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int,
    dataStore: DataStore<Preferences>,
    primaryColor: androidx.compose.ui.graphics.Color,
    secondaryColor: androidx.compose.ui.graphics.Color
) {
    val primaryColor = getColor(primaryColor)
    val secondaryColor = getColor(secondaryColor)

    val context = LocalContext.current

    val font = getParam(
        dataStore = dataStore,
        defaultValue = Font.getDefault().font
    ) { preferences -> Font.getById((preferences[stringPreferencesKey(PreferenceKey.DIGIT_FONT_CLOCKVIEW2.key)] ?: Font.getDefaultId())).font }
    as Int

    val digitStyle = getParam(
        dataStore = dataStore,
        defaultValue = DigitStyle.getDefault()
    ) { preferences ->
        DigitStyle.getById(preferences[stringPreferencesKey(PreferenceKey.DIGIT_STYLE_CLOCKVIEW2.key)] ?: DigitStyle.getDefaultId())
    }
    as DigitStyle

    val digitTextSize = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_DIGIT_TEXT_SIZE
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.DIGIT_TEXT_SIZE_CLOCKVIEW2.key)] }
    as Float

    val outerRimWidth = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_OUTER_RIM_WIDTH
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.OUTER_RIM_WIDTH_CLOCKVIEW2.key)] }
    as Float

    val innerRimWidth = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_INNER_RIM_WIDTH
    ) { preferences ->
        preferences[stringPreferencesKey(PreferenceKey.INNER_RIM_WIDTH_CLOCKVIEW2.key)] }
    as Float

    val thickMarkerWidth = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_THICK_MARKER_WIDTH
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.THICK_MARKER_WIDTH_CLOCKVIEW2.key)] }
    as Float

    val thinMarkerWidth = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_THIN_MARKER_WIDTH
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.THIN_MARKER_WIDTH_CLOCKVIEW2.key)] }
    as Float

    val hourHandWidth = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_HOUR_HAND_WIDTH
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.HOUR_HAND_WIDTH_CLOCKVIEW2.key)] }
    as Float

    val minuteHandWidth = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_MINUTE_HAND_WIDTH
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.MINUTE_HAND_WIDTH_CLOCKVIEW2.key)] }
    as Float

    val secondHandWidth = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_SECOND_HAND_WIDTH
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.SECOND_HAND_WIDTH_CLOCKVIEW2.key)] }
    as Float

    val centerCircleRadius = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_CENTER_CIRCLE_RADIUS
    ) { preferences -> preferences[stringPreferencesKey(PreferenceKey.CENTER_CIRCLE_RADIUS_CLOCKVIEW2.key)] }
    as Float

    init(
        outerRimColor = primaryColor,
        outerRimWidth = dipToPx(outerRimWidth),
        innerRimColor = primaryColor,
        innerRimWidth = dipToPx(innerRimWidth),
        thickMarkerColor = primaryColor,
        thickMarkerWidth = dipToPx(thickMarkerWidth),
        thinMarkerColor = primaryColor,
        thinMarkerWidth = dipToPx(thinMarkerWidth),
        digitTextColor = secondaryColor,
        digitTextSize = dipToPx(digitTextSize),
        digitFont = ResourcesCompat.getFont(context, font)!!,
        hourHandColor = primaryColor,
        hourHandWidth = dipToPx(hourHandWidth),
        minuteHandColor = primaryColor,
        minuteHandWidth = dipToPx(minuteHandWidth),
        secondHandColor = primaryColor,
        secondHandWidth = dipToPx(secondHandWidth),
        centerCircleColor = primaryColor
    )

    Column(
        modifier = modifier
            .padding(
                all = 20.dp
            )
    ) {
        OnDraw(
            modifier = modifier,
            showThickMarkers = true,
            showThinMarkers = true,
            showNumbers = true,
            showSweepHand = true,
            centerCircleRadius = dipToPx(centerCircleRadius),
            digitStyle = digitStyle,
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
    digitStyle: DigitStyle,
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int,
) {
    Canvas(
        modifier = modifier
            .fillMaxSize(),
    ) {
        val width = size.width.toInt()
        val height = size.height.toInt()
        val centerX = width / 2
        val centerY = height / 2
        val rectSize = min(width, height)
        paintRect[(centerX - rectSize / 2), (centerY - rectSize / 2), (centerX + rectSize / 2)] = centerY + rectSize / 2

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
                    digitStyle = digitStyle
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
    outerRimColor: Int,
    outerRimWidth: Float,
    innerRimColor: Int,
    innerRimWidth: Float,
    thickMarkerColor: Int,
    thickMarkerWidth: Float,
    thinMarkerColor: Int,
    thinMarkerWidth: Float,
    digitTextColor: Int,
    digitTextSize: Float,
    digitFont: Typeface,
    hourHandColor: Int,
    hourHandWidth: Float,
    minuteHandColor: Int,
    minuteHandWidth: Float,
    secondHandColor: Int,
    secondHandWidth: Float,
    centerCircleColor: Int
) {
    clockFacePaint.apply {
        isAntiAlias = true
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
    digitPaint.apply {
        isAntiAlias = true
        color = digitTextColor
        textSize = digitTextSize
        textAlign = Paint.Align.CENTER
        typeface = digitFont
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
        color = secondHandColor
        style = Paint.Style.STROKE
        strokeWidth = secondHandWidth
    }
    centerCirclePaint.apply {
        isAntiAlias = true
        color = centerCircleColor
        style = Paint.Style.FILL
    }
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
    digitStyle: DigitStyle
) {
    val radius = paintRect.width() / 2
    var number = 0
    val fm = digitPaint.fontMetrics
    val numberHeight = -fm.ascent + fm.descent
    var degree = -60
    while (degree < 300) {
        val radian = degree * Math.PI / 180
        val numberText = if (digitStyle == DigitStyle.ROMAN) {
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
            digitPaint
        )
        degree += 30
    }
}

private fun drawInnerRim(
    canvas: Canvas
) {
    val radius = paintRect.width() / 2
    val fm = digitPaint.fontMetrics
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
    val fm = digitPaint.fontMetrics
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

private fun getColor(value: androidx.compose.ui.graphics.Color) = Color.parseColor("#${Integer.toHexString(value.toArgb())}")

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

private val DEFAULT_THIN_MARKER_LENGTH = dipToPx(10F)
private val DEFAULT_THICK_MARKER_LENGTH = dipToPx(20F)
private val ROMAN_NUMBER_LIST = arrayOf("Ⅰ", "Ⅱ", "Ⅲ", "Ⅳ", "Ⅴ", "Ⅵ", "Ⅶ", "Ⅷ", "Ⅸ", "Ⅹ", "Ⅺ", "Ⅻ")

private val clockFacePaint = Paint()
private val outerRimPaint = Paint()
private val innerRimPaint = Paint()
private val thickMarkerPaint = Paint()
private val thinMarkerPaint = Paint()
private val digitPaint = TextPaint()
private val hourHandPaint = Paint()
private val minuteHandPaint = Paint()
private val sweepHandPaint = Paint()
private val centerCirclePaint = Paint()
private val paintRect = Rect()

const val DEFAULT_OUTER_RIM_WIDTH = 1F
const val DEFAULT_SECOND_HAND_WIDTH = 1F
const val DEFAULT_MINUTE_HAND_WIDTH = 3F
const val DEFAULT_HOUR_HAND_WIDTH = 5F
const val DEFAULT_DIGIT_TEXT_SIZE = 18F
const val DEFAULT_THIN_MARKER_WIDTH = 1F
const val DEFAULT_THICK_MARKER_WIDTH = 3F
const val DEFAULT_INNER_RIM_WIDTH = 1F
const val DEFAULT_CENTER_CIRCLE_RADIUS = 5F