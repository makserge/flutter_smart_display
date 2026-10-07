package com.smsoft.smartdisplay.ui.composable.clock.digitalclock

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.utils.getStateFromFlow
import com.smsoft.smartdisplay.ui.screen.clock.ClockViewModel
import com.smsoft.smartdisplay.ui.screen.clock.displayHour
import java.text.DateFormatSymbols
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
    second: Int,
    is24Hour: Boolean
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

    val timeFont = remember(timeFontRes) {
        FontFamily(
            Font(timeFontRes, weight = FontWeight.Normal)
        )
    }

    val timeFontSize = scale * getStateFromFlow(
        flow = viewModel.timeFontSizeDC,
        defaultValue = DEFAULT_TIME_FONT_SIZE_DC
    )
    as Float

    val dateFontRes = getStateFromFlow(
        flow = viewModel.dateFontResDC,
        defaultValue = DigitFont.getDefault().font
    ) as Int

    val dateFont = remember(dateFontRes) {
        FontFamily(
            Font(dateFontRes, weight = FontWeight.Normal)
        )
    }

    val dateFontSize = scale * getStateFromFlow(
        flow = viewModel.dateFontSizeDC,
        defaultValue = DEFAULT_DATE_FONT_SIZE_DC
    ) as Float

    // Weekday names and AM/PM follow the panel language (upper case as before)
    val locale = LocalConfiguration.current.locales[0]
    val weekdays = remember(locale) {
        getWeekdays(locale)
    }
    val amPmStrings = remember(locale) {
        getAmPmStrings(locale)
    }
    // The segment fonts have ASCII letters only. Cyrillic or accented weekday names were drawn
    // in a fallback font next to the segment digits; then the whole date line uses the system
    // font. (Paint.hasGlyph cannot tell: it counts the system's fallback fonts.)
    val dateLineFont = remember(dateFont, dateFontRes, weekdays) {
        val isLatinOnlyFont = DigitFont.entries.firstOrNull { it.font == dateFontRes }?.isLatinOnly ?: false
        val hasOtherLetters = weekdays.any { name -> name.any { it.code > 0x7E } }
        if (isLatinOnlyFont && hasOtherLetters) FontFamily.Default else dateFont
    }

    val time = timeText(
        hour = hour,
        minute = minute,
        second = if (isShowSeconds) second else null,
        is24Hour = is24Hour,
        amPmStrings = amPmStrings
    )
    val date = formatDate(
        weekday = weekdays.getOrElse(dayOfWeek - Calendar.SUNDAY) { "" },
        day = day.toString(),
        month = (month + 1).toString(),
        year = year.toString()
    )

    OnDraw(
        modifier = modifier,
        time = time,
        isShowSeconds = isShowSeconds,
        amPm = if (is24Hour) null else amPmStrings,
        primaryColor = primaryColor,
        timeFont = timeFont,
        timeFontSize = timeFontSize,
        isShowDate = isShowDate,
        date = date,
        weekdays = weekdays,
        secondaryColor = secondaryColor,
        dateFont = dateLineFont,
        dateFontSize = dateFontSize,
        spaceHeight = spaceHeight
    )
}

/**
 * Time and date, each on one line. The font sizes (sp) and the space (dp) from the settings are
 * maximum sizes: when the text does not fit the page, the time and the date each shrink just
 * enough to fit the width, and then all three together shrink to fit the height. [amPm] holds
 * AM and PM in 12-hour format and is null in 24-hour format.
 */
@Composable
fun OnDraw(
    modifier: Modifier,
    time: AnnotatedString,
    isShowSeconds: Boolean,
    amPm: List<String>?,
    primaryColor: Color,
    timeFont: FontFamily,
    timeFontSize: Float,
    isShowDate: Boolean,
    date: String,
    weekdays: List<String>,
    secondaryColor: Color,
    dateFont: FontFamily,
    dateFontSize: Float,
    spaceHeight: Float
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val textStyle = LocalTextStyle.current
        val timeStyle = remember(textStyle, timeFont, timeFontSize) {
            textStyle.merge(
                TextStyle(
                    fontFamily = timeFont,
                    fontSize = timeFontSize.sp
                )
            )
        }
        val dateStyle = remember(textStyle, dateFont, dateFontSize) {
            textStyle.merge(
                TextStyle(
                    fontFamily = dateFont,
                    fontSize = dateFontSize.sp
                )
            )
        }
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val spaceHeightPx = with(density) {
            spaceHeight.dp.toPx()
        }
        // Measured on the widest possible strings, so the size stays the same every second and
        // every day; measured again only when the page size, a font or a setting changes.
        val fitted = remember(
            constraints, measurer, timeStyle, dateStyle, isShowSeconds, amPm, isShowDate, weekdays, spaceHeight, spaceHeightPx, density
        ) {
            fitText(
                measurer = measurer,
                constraints = constraints,
                timeStyle = timeStyle,
                isShowSeconds = isShowSeconds,
                amPm = amPm,
                dateStyle = dateStyle,
                isShowDate = isShowDate,
                weekdays = weekdays,
                spaceHeight = spaceHeight,
                spaceHeightPx = spaceHeightPx,
                density = density
            )
        }
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 12-hour format: as wide as the widest time and aligned to its start, like an LCD. The
            // hour's tens is drawn invisibly below 10, so nothing moves at 10:00 and 1:00 (right
            // aligned, the dropped digit moved the colon and minutes by a rounding pixel), and a
            // wider AM/PM marker only takes more room on the right. 24-hour format uses the
            // plain text, centred by the column.
            Text(
                modifier = if (amPm != null) Modifier.width(fitted.timeWidth) else Modifier,
                text = time,
                color = primaryColor,
                textAlign = if (amPm != null) TextAlign.Start else TextAlign.Unspecified,
                // Left to right whatever the AM/PM marker's script
                style = if (amPm != null) {
                    fitted.timeStyle.copy(textDirection = TextDirection.Ltr)
                } else {
                    fitted.timeStyle
                },
                maxLines = 1,
                softWrap = false
            )
            if (isShowDate) {
                Spacer(
                    modifier = Modifier
                        .height(fitted.spaceHeight)
                )
                Text(
                    modifier = Modifier,
                    text = date,
                    color = secondaryColor,
                    style = fitted.dateStyle,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

private class FittedText(
    val timeStyle: TextStyle,
    // Width of the widest time the clock can show
    val timeWidth: Dp,
    val dateStyle: TextStyle,
    val spaceHeight: Dp
)

private fun fitText(
    measurer: TextMeasurer,
    constraints: Constraints,
    timeStyle: TextStyle,
    isShowSeconds: Boolean,
    amPm: List<String>?,
    dateStyle: TextStyle,
    isShowDate: Boolean,
    weekdays: List<String>,
    spaceHeight: Float,
    spaceHeightPx: Float,
    density: Density
): FittedText {
    val maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth.toFloat() else Float.MAX_VALUE
    val maxHeight = if (constraints.hasBoundedHeight) constraints.maxHeight.toFloat() else Float.MAX_VALUE

    val t = measurer.widestDigit(timeStyle)
    val seconds = if (isShowSeconds) "$t$t" else null
    // Every time the clock can show fits one of these (with AM and with PM in 12-hour format),
    // so neither the size nor the place of the time changes with the time
    val timeTemplates = (amPm ?: listOf(null)).map {
        formatTime(hours = "$t$t", minutes = "$t$t", seconds = seconds, amPm = it)
    }
    val timeSizes = timeTemplates.map {
        measurer.measureLine(it, timeStyle)
    }
    val timeScale = shrinkToFit(maxWidth, timeSizes.maxOf { it.width }.toFloat())
    // The space belongs to the time, so it shrinks with it
    val spaceScale = timeScale
    var height = timeSizes.maxOf { it.height } * timeScale

    var dateScale = 1F
    if (isShowDate) {
        val d = measurer.widestDigit(dateStyle)
        val dateSizes = weekdays.map {
            measurer.measureLine(AnnotatedString(formatDate(it, "$d$d", "$d$d", "$d$d$d$d")), dateStyle)
        }
        dateScale = shrinkToFit(maxWidth, dateSizes.maxOf { it.width }.toFloat())
        height += spaceHeightPx * spaceScale + dateSizes.maxOf { it.height } * dateScale
    }
    val heightScale = shrinkToFit(maxHeight, height)

    val fittedTimeStyle = timeStyle.copy(fontSize = timeStyle.fontSize * (timeScale * heightScale))
    // Measured again at the final size, which is rounded and not exactly linear; 1 px spare, so
    // the widest time is never clipped
    val timeWidth = timeTemplates.maxOf {
        measurer.measureLine(it, fittedTimeStyle).width
    } + 1
    return FittedText(
        timeStyle = fittedTimeStyle,
        timeWidth = with(density) { timeWidth.toDp() },
        dateStyle = dateStyle.copy(fontSize = dateStyle.fontSize * (dateScale * heightScale)),
        spaceHeight = (spaceHeight * spaceScale * heightScale).dp
    )
}

// Text size is rounded to whole pixels and does not scale exactly linearly, so leave 2 % spare
// whenever the text has to shrink.
private fun shrinkToFit(available: Float, needed: Float) =
    if (needed <= available) 1F else available / needed * 0.98F

private fun TextMeasurer.measureLine(text: AnnotatedString, style: TextStyle): IntSize =
    measure(
        text = text,
        style = style,
        softWrap = false,
        maxLines = 1
    ).size

private fun TextMeasurer.widestDigit(style: TextStyle): Char =
    ('0'..'9').maxBy { measureLine(AnnotatedString(it.toString()), style).width }

/**
 * The time as the clock shows it: "09:05" in 24-hour format, "9:05 PM" in 12-hour format, where
 * the hour has no visible leading zero (it is drawn transparent, so it keeps its place) and is
 * followed by AM or PM from [amPmStrings] in a smaller size. [second] is null when the seconds
 * are not shown.
 */
internal fun timeText(
    hour: Int,
    minute: Int,
    second: Int?,
    is24Hour: Boolean,
    amPmStrings: List<String>
): AnnotatedString {
    val shownHour = displayHour(hour, is24Hour)
    return formatTime(
        hours = shownHour.twoDigits(),
        minutes = minute.twoDigits(),
        seconds = second?.twoDigits(),
        amPm = if (is24Hour) null else amPmStrings[if (hour < 12) 0 else 1],
        isTensHidden = !is24Hour && (shownHour < 10)
    )
}

// AM/PM follow a space in the time size and are smaller, on the same baseline
private fun formatTime(
    hours: String,
    minutes: String,
    seconds: String?,
    amPm: String?,
    isTensHidden: Boolean = false
): AnnotatedString = buildAnnotatedString {
    if (isTensHidden) {
        // Digits are equally wide in the clock fonts, so the hidden "0" holds the tens' place
        withStyle(SpanStyle(color = Color.Transparent)) {
            append(hours.first())
        }
        append(hours.substring(1))
    } else {
        append(hours)
    }
    append(':')
    append(minutes)
    if (seconds != null) {
        append(':')
        append(seconds)
    }
    if (amPm != null) {
        append(' ')
        withStyle(SpanStyle(fontSize = AM_PM_SIZE.em)) {
            append(amPm)
        }
    }
}

private fun Int.twoDigits() = toString().padStart(2, '0')

// No spaces around the dots: DSEG fonts draw "." with zero width in the gap between two digits
private fun formatDate(
    weekday: String,
    day: String,
    month: String,
    year: String
) = "$weekday $day.$month.$year"

// Names from Calendar.SUNDAY to Calendar.SATURDAY
private fun getWeekdays(locale: Locale): List<String> {
    val names = DateFormatSymbols.getInstance(locale).weekdays
    return (Calendar.SUNDAY..Calendar.SATURDAY).map {
        names[it].uppercase(locale)
    }
}

// AM, then PM. The marker always follows the time, also in languages that put it first.
private fun getAmPmStrings(locale: Locale): List<String> =
    DateFormatSymbols.getInstance(locale).amPmStrings.map {
        it.uppercase(locale)
    }

/** [isLatinOnly]: the font has ASCII glyphs only (the segment fonts). */
enum class DigitFont(val id: String, val font: Int, val titleId: Int, val isLatinOnly: Boolean = false) {
    ROBOTO_REGULAR("roboto_regular", R.font.roboto_regular, R.string.digit_font_roboto_regular),
    ROBOTO_LIGHT("roboto_light", R.font.roboto_light, R.string.digit_font_roboto_light),
    ROBOTO_THIN("roboto_thin", R.font.roboto_thin, R.string.digit_font_roboto_thin),
    SEVEN_SEGMENT_DIGITAL(
        "seven_segment_digital", R.font.seven_segment_digital, R.string.digit_font_seven_segment_digital,
        isLatinOnly = true
    ),
    DSEG14_CLASSIC("dseg14classic", R.font.dseg14classic, R.string.digit_font_dseg14_classic, isLatinOnly = true);

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

        /** An unknown id falls back to the default instead of crashing the clock page. */
        fun getById(id: String): DigitFont {
            return entries.firstOrNull { it.id == id } ?: getDefault()
        }
    }
}

const val DEFAULT_SHOW_SECONDS_DC = false
const val DEFAULT_SHOW_DATE_DC = true
const val DEFAULT_TIME_FONT_SIZE_DC = 270F
const val DEFAULT_DATE_FONT_SIZE_DC = 68F
const val DEFAULT_SPACE_HEIGHT_DC = 70F

// Size of AM/PM per size of the time
private const val AM_PM_SIZE = 0.4F