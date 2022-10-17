package com.smsoft.smartdisplay.ui.composable.clock.digitalclock

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.getParam
import java.util.*

@Composable
fun DigitalClock(
    modifier: Modifier = Modifier
        .fillMaxSize(),
    dataStore: DataStore<Preferences>,
    scale: Float,
    primaryColor: Color,
    secondaryColor: Color,
    year: Int,
    month: Int,
    day: Int,
    dayOfWeek: Int,
    hour: Int,
    minute: Int,
    second: Int
) {

    val isShowSeconds = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_SHOW_SECONDS
    ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.SHOW_SECONDS_DIGITAL_CLOCK.key)] }
    as Boolean

    val isShowDate = getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_SHOW_DATE
    ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.SHOW_DATE_DIGITAL_CLOCK.key)] }
    as Boolean

    val spaceHeight = scale * getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_SPACE_HEIGHT
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.SPACE_HEIGHT_DIGITAL_CLOCK.key)] }
    as Float

    val timeFontRes = getParam(
        dataStore = dataStore,
        defaultValue = DigitFont.getDefault().font
    ) { preferences -> DigitFont.getById((preferences[stringPreferencesKey(PreferenceKey.TIME_FONT_DIGITAL_CLOCK.key)] ?: DigitFont.getDefaultId())).font }
    as Int

    val timeFont = FontFamily(
        Font(timeFontRes, weight = FontWeight.Normal)
    )

    val timeFontSize = scale * getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_TIME_FONT_SIZE
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.TIME_FONT_SIZE_DIGITAL_CLOCK.key)] }
    as Float

    val dateFontRes = getParam(
        dataStore = dataStore,
        defaultValue = DigitFont.getDefault().font
    ) { preferences -> DigitFont.getById((preferences[stringPreferencesKey(PreferenceKey.DATE_FONT_DIGITAL_CLOCK.key)] ?: DigitFont.getDefaultId())).font }
    as Int

    val dateFont = FontFamily(
        Font(dateFontRes, weight = FontWeight.Normal)
    )

    val dateFontSize = scale * getParam(
        dataStore = dataStore,
        defaultValue = DEFAULT_DATE_FONT_SIZE
    ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.DATE_FONT_SIZE_DIGITAL_CLOCK.key)] }
    as Float

    val time = addLeadingZeros(
        hour = hour,
        minute = minute,
        second = second,
        isShowSeconds = isShowSeconds
    )
    val date = getDayOfWeek(dayOfWeek) + " " + day.toString() + " . " + (month + 1) + " . " + year.toString()

    OnDraw(
        time = time,
        primaryColor = primaryColor,
        timeFont = timeFont,
        timeFontSize = timeFontSize.sp,
        isShowDate = isShowDate,
        date = date,
        secondaryColor = secondaryColor,
        dateFont = dateFont,
        dateFontSize = dateFontSize.sp,
        spaceHeight = spaceHeight
    )
}

@Composable
fun OnDraw(
    time: String,
    primaryColor: Color,
    timeFont: FontFamily,
    timeFontSize: TextUnit,
    isShowDate: Boolean,
    date: String,
    secondaryColor: Color,
    dateFont: FontFamily,
    dateFontSize: TextUnit,
    spaceHeight: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier,
                text = time,
                color = primaryColor,
                fontFamily = timeFont,
                fontSize = timeFontSize
            )
            if (isShowDate) {
                Spacer(
                    modifier = Modifier
                        .height(spaceHeight.dp)
                )
                Text(
                    modifier = Modifier,
                    text = date,
                    color = secondaryColor,
                    fontFamily = dateFont,
                    fontSize = dateFontSize
                )
            }
        }
    }
}

private fun addLeadingZeros(
    hour: Int,
    minute: Int,
    second: Int,
    isShowSeconds:
    Boolean
): String {
    val hoursStr = hour.toString()
    val minutesStr = if (minute <= 9) "0$minute" else minute.toString()
    val secondsStr = if (second <= 9) "0$second" else second.toString()
    return if (isShowSeconds) "$hoursStr:$minutesStr:$secondsStr" else "$hoursStr:$minutesStr"
}

private fun getDayOfWeek(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        Calendar.MONDAY -> "MONDAY"
        Calendar.TUESDAY -> "TUESDAY"
        Calendar.WEDNESDAY -> "WEDNESDAY"
        Calendar.THURSDAY -> "THURSDAY"
        Calendar.FRIDAY -> "FRIDAY"
        Calendar.SATURDAY -> "SATURDAY"
        Calendar.SUNDAY -> "SUNDAY"
        else -> "MONDAY"
    }
}

enum class DigitFont(val id: String, val font: Int, val titleId: Int) {
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

        fun getDefault(): DigitFont {
            return DSEG14_CLASSIC
        }

        fun getDefaultId(): String {
            return getDefault().id
        }

        fun getById(id: String): DigitFont {
            val item = values().filter {
                it.id == id
            }
            return item[0]
        }
    }
}

const val DEFAULT_SHOW_SECONDS = false
const val DEFAULT_SHOW_DATE = true
const val DEFAULT_TIME_FONT_SIZE = 270F
const val DEFAULT_DATE_FONT_SIZE = 72F
const val DEFAULT_SPACE_HEIGHT = 70F