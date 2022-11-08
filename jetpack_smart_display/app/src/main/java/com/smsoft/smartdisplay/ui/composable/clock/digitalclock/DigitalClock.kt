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
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.utils.getStateFromFlow
import com.smsoft.smartdisplay.ui.screen.clock.ClockViewModel
import java.util.*

@Composable
fun DigitalClock(
    modifier: Modifier = Modifier
        .fillMaxSize(),
    viewModel: ClockViewModel,
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

    val isShowSeconds = getStateFromFlow(
        flow = viewModel.isShowSecondsDC,
        defaultValue = DEFAULT_SHOW_SECONDS_DC
    ) as Boolean

    val isShowDate = getStateFromFlow(
        flow = viewModel.isShowDateDC,
        defaultValue = DEFAULT_SHOW_DATE_DC
    ) as Boolean

    val spaceHeight = scale * getStateFromFlow(
        flow = viewModel.spaceHeightDC,
        defaultValue = DEFAULT_SPACE_HEIGHT_DC
    ) as Float

    val timeFontRes = getStateFromFlow(
        flow = viewModel.timeFontResDC,
        defaultValue = DigitFont.getDefault().font
    ) as Int

    val timeFont = FontFamily(
        Font(timeFontRes, weight = FontWeight.Normal)
    )

    val timeFontSize = scale * getStateFromFlow(
        flow = viewModel.timeFontSizeDC,
        defaultValue = DEFAULT_TIME_FONT_SIZE_DC
    )
    as Float

    val dateFontRes = getStateFromFlow(
        flow = viewModel.dateFontResDC,
        defaultValue = DigitFont.getDefault().font
    ) as Int

    val dateFont = FontFamily(
        Font(dateFontRes, weight = FontWeight.Normal)
    )

    val dateFontSize = scale * getStateFromFlow(
        flow = viewModel.dateFontSizeDC,
        defaultValue = DEFAULT_DATE_FONT_SIZE_DC
    ) as Float

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

const val DEFAULT_SHOW_SECONDS_DC = false
const val DEFAULT_SHOW_DATE_DC = true
const val DEFAULT_TIME_FONT_SIZE_DC = 270F
const val DEFAULT_DATE_FONT_SIZE_DC = 68F
const val DEFAULT_SPACE_HEIGHT_DC = 70F