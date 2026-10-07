package com.smsoft.smartdisplay

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.em
import com.smsoft.smartdisplay.ui.composable.clock.digitalclock.timeText
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.hourCards
import com.smsoft.smartdisplay.ui.screen.clock.displayHour
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** 12- and 24-hour display of the digital clocks. */
class HourFormatTest {

    private val amPm = listOf("AM", "PM")

    // The text as it looks: characters drawn transparent (the hidden hour tens) left out
    private fun AnnotatedString.visible(): String {
        val hidden = spanStyles.filter { it.item.color == Color.Transparent }
        return text.filterIndexed { i, _ -> hidden.none { i >= it.start && i < it.end } }
    }

    @Test
    fun displayHour_24Hour_isTheHour() {
        for (hour in 0..23) {
            assertEquals(hour, displayHour(hour, is24Hour = true))
        }
    }

    @Test
    fun displayHour_12Hour() {
        val expected = listOf(
            12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
            12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
        )
        assertEquals(expected, (0..23).map { displayHour(it, is24Hour = false) })
    }

    @Test
    fun timeText_24Hour_hasLeadingZeroAndNoAmPm() {
        assertEquals("09:05", timeText(9, 5, null, is24Hour = true, amPmStrings = amPm).text)
        assertEquals("00:00:07", timeText(0, 0, 7, is24Hour = true, amPmStrings = amPm).text)
        assertEquals("23:59", timeText(23, 59, null, is24Hour = true, amPmStrings = amPm).text)
        assertTrue(timeText(21, 5, 30, is24Hour = true, amPmStrings = amPm).spanStyles.isEmpty())
    }

    @Test
    fun timeText_12Hour_hasNoLeadingZeroAndAmPm() {
        fun shown(h: Int, m: Int, s: Int?) =
            timeText(h, m, s, is24Hour = false, amPmStrings = amPm).visible()
        assertEquals("12:00 AM", shown(0, 0, null))
        assertEquals("9:05 AM", shown(9, 5, null))
        assertEquals("11:59:59 AM", shown(11, 59, 59))
        assertEquals("12:00 PM", shown(12, 0, null))
        assertEquals("1:00:00 PM", shown(13, 0, 0))
        assertEquals("9:05 PM", shown(21, 5, null))
    }

    @Test
    fun timeText_12Hour_hourKeepsTwoPlaces() {
        // Below 10 the tens is there but transparent, so the minutes never move at 10:00 and 1:00
        for (hour in 0..23) {
            val time = timeText(hour, 5, null, is24Hour = false, amPmStrings = amPm)
            val shown = displayHour(hour, is24Hour = false)
            assertEquals("$hour", shown.toString().padStart(2, '0'), time.text.substring(0, 2))
            val hidden = time.spanStyles.filter { it.item.color == Color.Transparent }
            if (shown < 10) {
                assertEquals("$hour", 0 until 1, hidden.single().let { it.start until it.end })
            } else {
                assertTrue("$hour", hidden.isEmpty())
            }
        }
    }

    @Test
    fun timeText_12Hour_amPmIsSmaller() {
        val time = timeText(21, 5, null, is24Hour = false, amPmStrings = amPm)
        val span = time.spanStyles.single { it.item.fontSize != androidx.compose.ui.unit.TextUnit.Unspecified }
        assertEquals("PM", time.text.substring(span.start, span.end))
        assertEquals(0.4F.em, span.item.fontSize)
    }

    @Test
    fun hourCards_showTheHour() {
        for (is24Hour in listOf(true, false)) {
            for (hour in 0..23) {
                val shown = displayHour(hour, is24Hour)
                val expected = when {
                    is24Hour -> shown.toString().padStart(2, '0')
                    else -> shown.toString().padStart(2, ' ')
                }
                val (tens, units) = hourCards(hour, is24Hour)
                assertEquals(
                    "$hour, 24 h: $is24Hour",
                    expected,
                    "${tens.chars[tens.index]}${units.chars[units.index]}"
                )
            }
        }
    }

    @Test
    fun hourCards_flipToTheNextHour() {
        for (is24Hour in listOf(true, false)) {
            for (hour in 0..23) {
                val from = hourCards(hour, is24Hour).toList()
                val to = hourCards((hour + 1) % 24, is24Hour).toList()
                from.zip(to).forEach { (card, next) ->
                    // TabDigit takes its characters once, so they must not change
                    assertSame(card.chars, next.chars)
                    // TabDigit flips only a single forward step and jumps on anything else
                    assertTrue(
                        "$hour -> ${hour + 1}, 24 h: $is24Hour",
                        (next.index == card.index) ||
                            (next.index == (card.index + 1) % card.chars.size)
                    )
                }
            }
        }
    }
}
