package com.smsoft.smartdisplay.ui.composable.clock.nightdream

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.digit.TabDigit
import kotlinx.coroutines.flow.map

@Composable
fun DigitalFlipClock(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    dataStore: DataStore<Preferences>,
    scale: Float,
    primaryColor: Color,
    secondaryColor: Color,
    hour: Int,
    minute: Int
) {
    val primaryColor =  primaryColor.toArgb()
    val secondaryColor =  secondaryColor.toArgb()

    val reverseRotation = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_REVERSE_ROTATION
    ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.REVERSE_ROTATION_FLIP_CLOCK.key)] }
    as Boolean

    val cornerSize = DEFAULT_CORNER_SIZE
    val background = DEFAULT_BACKGROUND

    val padding = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_PADDING
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.PADDING_FLIP_CLOCK.key)] }
    as Float

    val fontSize = scale * getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_TEXT_SIZE
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.FONT_SIZE_FLIP_CLOCK.key)] }
    as Float

    val highHour = hour / 10
    val highMinute = minute / 10
    val lowMinute = minute - highMinute * 10

    var currentHourHigh by remember { mutableStateOf(highHour) }
    var currentHourLow by remember { mutableStateOf(hour) }
    var currentMinuteHigh by remember { mutableStateOf(highMinute) }
    var currentMinuteLow by remember { mutableStateOf(lowMinute) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = modifier
                .padding(
                    all = 20.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AndroidView(
                modifier = Modifier,
                factory = { context ->
                    TabDigit(context).apply {
                        chars = HOURS
                        this.textSize = fontSize
                        textColor = primaryColor
                        setChar(highHour)
                    }
                },
                update = { view ->
                    view.reverseRotation = reverseRotation
                    view.cornerSize = cornerSize
                    view.background = background
                    view.padding = padding
                    view.textSize = fontSize
                    view.apply {
                        dividerColor = secondaryColor
                        textColor = primaryColor

                        if (currentHourHigh != highHour) {
                            currentHourHigh = highHour
                            start()
                        }
                    }
                }
            )
            AndroidView(
                modifier = Modifier,
                factory = { context ->
                    TabDigit(context).apply {
                        chars = LOW_HOURS24
                        this.textSize = fontSize
                        textColor = primaryColor
                        setChar(hour)
                    }
                },
                update = { view ->
                    view.reverseRotation = reverseRotation
                    view.cornerSize = cornerSize
                    view.background = background
                    view.padding = padding
                    view.textSize = fontSize
                    view.apply {
                        dividerColor = secondaryColor
                        textColor = primaryColor

                        if (currentHourLow != hour) {
                            currentHourLow = hour
                            start()
                        }
                    }
                }
            )
            Box(
                modifier = Modifier
                    .absolutePadding(
                        left = 5.dp,
                        right = 5.dp,
                        bottom = (scale * 40).dp
                    ),
            ) {
                Text(
                    modifier = Modifier,
                    text = ":",
                    color = Color(primaryColor),
                    fontSize = (scale * fontSize / 2).sp,
                    fontFamily = FontFamily(
                        Font(R.font.roboto_regular, weight = FontWeight.Bold)
                    )
                )
            }
            AndroidView(
                modifier = Modifier,
                factory = { context ->
                    TabDigit(context).apply {
                        chars = HIGH_MINUTES
                        this.textSize = fontSize
                        textColor = primaryColor
                        setChar(highMinute)
                    }
                },
                update = { view ->
                    view.reverseRotation = reverseRotation
                    view.cornerSize = cornerSize
                    view.background = background
                    view.padding = padding
                    view.textSize = fontSize
                    view.apply {
                        dividerColor = secondaryColor
                        textColor = primaryColor

                        if (currentMinuteHigh != highMinute) {
                            currentMinuteHigh = highMinute
                            start()
                        }
                    }
                }
            )
            AndroidView(
                modifier = Modifier,
                factory = { context ->
                    TabDigit(context).apply {
                        chars = LOW_HOURS24
                        this.textSize = fontSize
                        textColor = primaryColor
                        setChar(lowMinute)
                    }
                },
                update = { view ->
                    view.reverseRotation = reverseRotation
                    view.cornerSize = cornerSize
                    view.background = background
                    view.padding = padding
                    view.textSize = fontSize
                    view.apply {
                        dividerColor = secondaryColor
                        textColor = primaryColor

                        if (currentMinuteLow != lowMinute) {
                            currentMinuteLow = lowMinute
                            start()
                        }
                    }
                }
            )
        }
    }
}

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
private fun getParam(dataStore: DataStore<Preferences>, defaultValue: Any?, getter: (preferences: Preferences) -> Any?): Any? {
    return dataStore.data.map {
        getter(it) ?: defaultValue
    }.collectAsState(initial = defaultValue).value
}

private val HOURS = charArrayOf('0', '1', '2')
private val LOW_HOURS24 = charArrayOf(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3'
)
private val HIGH_MINUTES = charArrayOf('0', '1', '2', '3', '4', '5')

const val DEFAULT_REVERSE_ROTATION = true
const val DEFAULT_CORNER_SIZE = 0F
val DEFAULT_BACKGROUND = android.graphics.Color.parseColor("#2C2C2C")
const val DEFAULT_TEXT_SIZE = 440F
const val DEFAULT_PADDING = 12F