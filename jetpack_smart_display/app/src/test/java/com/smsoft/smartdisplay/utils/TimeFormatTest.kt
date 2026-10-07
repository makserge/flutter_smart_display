package com.smsoft.smartdisplay.utils

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

/** Alarm times (minutes of the day) and timer durations (seconds) as text. */
class TimeFormatTest {

    private lateinit var formatLocale: Locale

    // The functions use String.format, i.e. the default format locale, whose digits need not be
    // ASCII; the expected texts are in Locale.ROOT.
    @Before
    fun pinLocale() {
        formatLocale = Locale.getDefault(Locale.Category.FORMAT)
        Locale.setDefault(Locale.Category.FORMAT, Locale.ROOT)
    }

    @After
    fun restoreLocale() {
        Locale.setDefault(Locale.Category.FORMAT, formatLocale)
    }

    @Test
    fun alarmTime() {
        assertEquals("00:00", formatTime(0))
        assertEquals("07:05", formatTime(7 * 60 + 5))
        assertEquals("23:59", formatTime(24 * 60 - 1))
    }

    @Test
    fun timerDuration() {
        assertEquals("0:00:00", formatTimeLong(0))
        assertEquals("0:01:05", formatTimeLong(65))
        assertEquals("1:00:00", formatTimeLong(3600))
        assertEquals("25:59:59", formatTimeLong(26 * 3600 - 1))
    }
}
