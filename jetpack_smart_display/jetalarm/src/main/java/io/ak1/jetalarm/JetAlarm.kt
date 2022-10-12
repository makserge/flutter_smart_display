package io.ak1.jetalarm

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.min
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JetAlarm(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    dataStore: DataStore<Preferences>,
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int
) {
    val defaultPrimaryColor = getColor(MaterialTheme.colors.primary)
    var primaryColor by remember { mutableStateOf(defaultPrimaryColor) }
    getColorSetings(
        key = PreferenceKey.PRIMARY_COLOR,
        dataStore = dataStore,
        defaultValue = MaterialTheme.colors.primary
    )?.let {
        primaryColor = Color(android.graphics.Color.parseColor(it))
    }

    val defaultSecondaryColor = getColor(MaterialTheme.colors.secondary)
    var secondaryColor by remember { mutableStateOf(defaultSecondaryColor) }
    getColorSetings(
        key = PreferenceKey.SECONDARY_COLOR,
        dataStore = dataStore,
        defaultValue = MaterialTheme.colors.secondary
    )?.let {
        secondaryColor = Color(android.graphics.Color.parseColor(it))
    }

    val borderRadius = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_BORDER_RADIUS
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.BORDER_RADIUS_JET.key)] }
    as Float

    val borderThickness = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_BORDER_THICKNESS
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.BORDER_THICKNESS_JET.key)] }
    as Float

    val secondsHandLength = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_HAND_LEN_SECONDS_JET
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_LENGTH_SECONDS_JET.key)] }
    as Float

    val minutesHandLength = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_HAND_LEN_MINUTES_JET
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_LENGTH_MINUTES_JET.key)] }
    as Float

    val secondsHandWidth = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_HAND_WIDTH_SECONDS_JET
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_WIDTH_SECONDS_JET.key)] }
    as Float

    val minutesHandWidth = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_HAND_WIDTH_MINUTES_JET
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_WIDTH_MINUTES_JET.key)] }
    as Float

    val hoursHandLength = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_HAND_LEN_HOURS_JET
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_LENGTH_HOURS_JET.key)] }
    as Float

    val hoursHandWidth = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_HAND_WIDTH_HOURS_JET
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_WIDTH_HOURS_JET.key)] }
    as Float

    val showSecondHand = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_SHOW_SECOND_HAND_JET
    ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.HAND_SHOW_SECOND_HAND_JET.key)] }
    as Boolean

    OnDraw(
        modifier = modifier,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        borderRadius = borderRadius,
        borderThickness = borderThickness,
        showSecondHand = showSecondHand,
        secondsHandLength = secondsHandLength,
        secondsHandWidth = secondsHandWidth,
        minutesHandLength = minutesHandLength,
        minutesHandWidth = minutesHandWidth,
        hoursHandLength = hoursHandLength,
        hoursHandWidth = hoursHandWidth,
        hour = hour,
        minute = minute,
        second = second,
        milliSecond = milliSecond
    )
}

@Composable
fun OnDraw(
    modifier: Modifier,
    primaryColor: Color,
    secondaryColor: Color,
    borderRadius: Float,
    borderThickness: Float,
    showSecondHand: Boolean,
    secondsHandLength: Float,
    secondsHandWidth: Float,
    minutesHandLength: Float,
    minutesHandWidth: Float,
    hoursHandLength: Float,
    hoursHandWidth: Float,
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int
) {
    Box(
        modifier = modifier
    ) {
        DrawStaticUi(
            modifier = modifier,
            borderRadius = borderRadius,
            borderThickness = borderThickness,
            color = primaryColor
        )
        DrawHands(
            modifier = modifier,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            showSecondHand = showSecondHand,
            secondsHandLength = secondsHandLength,
            secondsHandWidth = secondsHandWidth,
            minutesHandLength = minutesHandLength,
            minutesHandWidth = minutesHandWidth,
            hoursHandLength = hoursHandLength,
            hoursHandWidth = hoursHandWidth,
            hour = hour,
            minute = minute,
            second = second,
            milliSecond = milliSecond
        )
    }
}

@Composable
fun DrawStaticUi(
    modifier: Modifier,
    borderRadius: Float,
    borderThickness: Float,
    color: Color
) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        drawCircle(
            color = color,
            radius = 15F
        )
        drawCircle(
            color = color,
            radius = size.getRadius(borderRadius),
            style = Stroke(borderThickness)
        )
    }
}

@Composable
fun DrawHands(
    modifier: Modifier,
    primaryColor: Color,
    secondaryColor: Color,
    showSecondHand: Boolean,
    secondsHandLength: Float,
    secondsHandWidth: Float,
    minutesHandLength: Float,
    minutesHandWidth: Float,
    hoursHandLength: Float,
    hoursHandWidth: Float,
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int
) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val progression = ((milliSecond % 1000) / 1000.0)

        val centerX = (size.width / 2)
        val centerY = (size.height / 2)

        val hourInt = if (hour > 12) hour - 12 else hour

        val animatedSecond = second + progression
        val animatedMinute = minute + animatedSecond / 60
        val animatedHour = (hourInt + (animatedMinute / 60)) * 5F
        if (showSecondHand) {
            drawHand(
                scope = this,
                centerX = centerX,
                centerY = centerY,
                clockRadius = size.getRadius(minutesHandLength),
                animatedValue = animatedMinute,
                color = primaryColor,
                strokeWidth = minutesHandWidth
            )
            drawHand(
                scope = this,
                centerX = centerX,
                centerY = centerY,
                clockRadius = size.getRadius(hoursHandLength),
                animatedValue = animatedHour,
                color = primaryColor,
                strokeWidth = hoursHandWidth
            )
            drawHand(
                scope = this,
                centerX = centerX,
                centerY = centerY,
                clockRadius = size.getRadius(secondsHandLength),
                animatedValue = animatedSecond,
                color = secondaryColor,
                strokeWidth = secondsHandWidth
            )
        } else {
            drawHand2(
                scope = this,
                centerX = centerX,
                centerY = centerY,
                clockRadius = size.getRadius(hoursHandLength),
                animatedValue = animatedHour,
                color = primaryColor,
                strokeWidth = hoursHandWidth
            )
            drawHand2(
                scope = this,
                centerX = centerX,
                centerY = centerY,
                clockRadius = size.getRadius(minutesHandLength),
                animatedValue = animatedMinute,
                color = primaryColor,
                strokeWidth = minutesHandWidth
            )
        }
    }
}

private fun drawHand(
    scope: DrawScope,
    centerX: Float,
    centerY: Float,
    clockRadius: Float,
    animatedValue: Double,
    color: Color,
    strokeWidth: Float
) {
    val degree = animatedValue * (Math.PI / 30) - Math.PI / 2
    scope.drawLine(
        start = Offset(
            x = centerX,
            y = centerY),
        end = Offset(
            x = (centerX + cos(degree) * clockRadius).toFloat(),
            y = (centerY + sin(degree) * clockRadius).toFloat()
        ),
        color = color,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

private fun drawHand2(
    scope: DrawScope,
    centerX: Float,
    centerY: Float,
    clockRadius: Float,
    animatedValue: Double,
    color: Color,
    strokeWidth: Float
) {
    val degree = animatedValue * (Math.PI / 30) - Math.PI / 2
    scope.drawLine(
        start = Offset(
            x = (centerX + cos(degree) * -30).toFloat(),
            y = (centerY + sin(degree) * -30).toFloat()
        ),
        end = Offset(
            x = (centerX + cos(degree) * clockRadius).toFloat(),
            y = (centerY + sin(degree) * clockRadius).toFloat()
        ),
        color = color,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

private fun Size.getRadius(
    expo: Float = 1F
) = expo * min(Dp(width / 2), Dp(height / 2)).value

@Composable
private fun getColorSetings(
    key: PreferenceKey,
    dataStore: DataStore<Preferences>,
    defaultValue: Color
): String? {
    return getParam(
        dataStore = dataStore,
        defaultValue = "#${Integer.toHexString(defaultValue.toArgb())}"
    ) { preferences -> preferences[stringPreferencesKey(key.key)] }
    as String?
}

private fun getColor(
    color: Color
): Color {
    val defaultPrimaryColorStr = "#${Integer.toHexString(color.toArgb())}"
    return Color(android.graphics.Color.parseColor(defaultPrimaryColorStr))
}

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
private fun getParam(
    dataStore: DataStore<Preferences>,
    defaultValue: Any?,
    getter: (preferences: Preferences) -> Any?
): Any? {
    return dataStore.data.map {
        getter(it) ?: defaultValue
    }.collectAsState(initial = defaultValue).value
}

const val DEFAULT_BORDER_RADIUS = 0.9F
const val DEFAULT_BORDER_THICKNESS = 7F
const val DEFAULT_HAND_LEN_SECONDS_JET = 0.7F
const val DEFAULT_HAND_LEN_MINUTES_JET = 0.6F
const val DEFAULT_HAND_LEN_HOURS_JET = 0.45F
const val DEFAULT_HAND_WIDTH_SECONDS_JET = 4F
const val DEFAULT_HAND_WIDTH_MINUTES_JET = 8F
const val DEFAULT_HAND_WIDTH_HOURS_JET = 8F
const val DEFAULT_SHOW_SECOND_HAND_JET = true