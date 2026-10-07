package com.smsoft.smartdisplay.service.clock

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.data.ClockType
import com.smsoft.smartdisplay.data.PreferenceKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import kotlin.random.Random

/**
 * "Random clock every day": the daily check and when it runs next. The setting is passed
 * explicitly, except in the test of the default.
 */
class DailyClockChangerTest {

    private val enabled = booleanPreferencesKey(PreferenceKey.RANDOM_CLOCK_DAILY.key)
    private val clockType = stringPreferencesKey(PreferenceKey.CLOCK_TYPE.key)
    private val today = LocalDate.of(2026, 10, 6).toEpochDay()

    private fun preferences(
        isEnabled: Boolean? = null,
        lastDay: Long? = null,
        clock: ClockType? = null
    ): MutablePreferences =
        mutablePreferencesOf().apply {
            isEnabled?.let { this[enabled] = it }
            lastDay?.let { this[LAST_DAY_KEY] = it }
            clock?.let { this[clockType] = it.id }
        }

    @Test
    fun disabled_clearsTheStoredDay() {
        val preferences =
            preferences(isEnabled = false, lastDay = today - 3, clock = ClockType.DIGITAL_CLOCK)
        assertNull(updateDailyClock(preferences, today))
        assertNull(preferences[LAST_DAY_KEY])
        assertEquals(ClockType.DIGITAL_CLOCK.id, preferences[clockType])
    }

    @Test
    fun disabled_neverSwitches() {
        val preferences = preferences(isEnabled = false, clock = ClockType.DIGITAL_CLOCK)
        assertNull(updateDailyClock(preferences, today))
        assertEquals(preferences(isEnabled = false, clock = ClockType.DIGITAL_CLOCK), preferences)
    }

    @Test
    fun firstRun_storesTodayWithoutSwitching() {
        // On by default: a fresh install starts counting today.
        val preferences = preferences()
        assertNull(updateDailyClock(preferences, today))
        assertEquals(today, preferences[LAST_DAY_KEY])
        assertNull(preferences[clockType])
    }

    @Test
    fun switchedOn_storesTodayWithoutSwitching() {
        val preferences = preferences(isEnabled = true, clock = ClockType.ANALOG_JETALARM)
        assertNull(updateDailyClock(preferences, today))
        assertEquals(today, preferences[LAST_DAY_KEY])
        assertEquals(ClockType.ANALOG_JETALARM.id, preferences[clockType])
    }

    @Test
    fun dateSetBack_storesTodayWithoutSwitching() {
        val preferences =
            preferences(isEnabled = true, lastDay = today + 5, clock = ClockType.ANALOG_JETALARM)
        assertNull(updateDailyClock(preferences, today))
        assertEquals(today, preferences[LAST_DAY_KEY])
        assertEquals(ClockType.ANALOG_JETALARM.id, preferences[clockType])
    }

    @Test
    fun sameDay_changesNothing() {
        fun sameDay() =
            preferences(isEnabled = true, lastDay = today, clock = ClockType.ANALOG_JETALARM)
        val preferences = sameDay()
        assertNull(updateDailyClock(preferences, today))
        assertEquals(sameDay(), preferences)
    }

    @Test
    fun laterDay_switchesToAnotherClock() {
        // The next day, or the first start after the panel was off for a while.
        val random = Random(42)
        for (lastDay in listOf(today - 1, today - 30)) {
            for (current in ClockType.entries) {
                repeat(50) {
                    val preferences =
                        preferences(isEnabled = true, lastDay = lastDay, clock = current)
                    val next = updateDailyClock(preferences, today, random)
                    assertNotEquals(current, next)
                    assertEquals(next?.id, preferences[clockType])
                    assertEquals(today, preferences[LAST_DAY_KEY])
                }
            }
        }
    }

    @Test
    fun laterDay_noClockStored_switchesAwayFromTheDefault() {
        // No clock stored shows the default, so it must never be picked. Many draws: a single one
        // misses the default by chance most of the time.
        val random = Random(1)
        val picked = (1..500).map {
            val preferences = preferences(isEnabled = true, lastDay = today - 1)
            val next = updateDailyClock(preferences, today, random)
            assertNotNull(next)
            assertNotEquals(ClockType.getDefault(), next)
            assertEquals(next?.id, preferences[clockType])
            next
        }.toSet()
        assertEquals(ClockType.entries.toSet() - ClockType.getDefault(), picked)
    }

    @Test
    fun laterDay_reachesEveryOtherClock() {
        val current = ClockType.DIGITAL_MATRIXCLOCK
        val random = Random(7)
        val picked = (1..500).mapNotNull {
            val preferences = preferences(isEnabled = true, lastDay = today - 1, clock = current)
            updateDailyClock(preferences, today, random)
        }.toSet()
        assertEquals(ClockType.entries.toSet() - current, picked)
    }

    @Test
    fun laterDay_thenSameDay_switchesOnce() {
        val preferences =
            preferences(isEnabled = true, lastDay = today - 1, clock = ClockType.DIGITAL_CLOCK)
        val next = updateDailyClock(preferences, today, Random(3))
        assertNull(updateDailyClock(preferences, today, Random(4)))
        assertEquals(next?.id, preferences[clockType])
    }

    @Test
    fun nextCheck_justAfterMidnight() {
        val day = LocalDate.of(2026, 10, 6)
        assertEquals(
            500 + MIDNIGHT_MARGIN_MS,
            nextCheckDelayMs(day.atTime(23, 59, 59, 500_000_000))
        )
        assertEquals(10 * 60_000 + MIDNIGHT_MARGIN_MS, nextCheckDelayMs(day.atTime(23, 50)))
        // Never earlier than the margin, so the check always lands on the new day.
        assertEquals(MIDNIGHT_MARGIN_MS, nextCheckDelayMs(day.atTime(LocalTime.MAX)))
    }

    @Test
    fun nextCheck_atMostTheInterval() {
        val day = LocalDate.of(2026, 10, 6)
        assertEquals(MAX_CHECK_INTERVAL_MS, nextCheckDelayMs(day.atStartOfDay()))
        assertEquals(MAX_CHECK_INTERVAL_MS, nextCheckDelayMs(day.atTime(12, 0)))
        assertEquals(MAX_CHECK_INTERVAL_MS, nextCheckDelayMs(day.atTime(23, 44)))
    }

    @Test
    fun nextCheck_wholeDay() {
        val start = LocalDate.of(2026, 10, 6).atStartOfDay()
        val justAfterMidnight = start.plusDays(1).plusNanos(MIDNIGHT_MARGIN_MS * 1_000_000)
        for (minute in 0 until 24 * 60) {
            val now = start.plusMinutes(minute.toLong()).plusSeconds(17)
            val delay = nextCheckDelayMs(now)
            assertTrue("$now: $delay", delay in MIDNIGHT_MARGIN_MS..MAX_CHECK_INTERVAL_MS)
            if (delay < MAX_CHECK_INTERVAL_MS) {
                // Lands just after the next midnight.
                assertEquals("$now", justAfterMidnight, now.plusNanos(delay * 1_000_000))
            } else {
                assertFalse("$now", now.plusNanos(delay * 1_000_000).isAfter(start.plusDays(1)))
            }
        }
    }

    @Test
    fun nextCheck_now_isWithinBounds() {
        // Smoke test of the default argument (the wall clock) the service calls it with; the
        // values themselves are pinned above with fixed times.
        assertTrue(nextCheckDelayMs() in MIDNIGHT_MARGIN_MS..MAX_CHECK_INTERVAL_MS)
    }

    @Test
    fun clockTypeIds_areUniqueAndRoundTrip() {
        // The switch picks the other clocks by id.
        assertEquals(ClockType.entries.size, ClockType.entries.map { it.id }.toSet().size)
        ClockType.entries.forEach { assertEquals(it, ClockType.getById(it.id)) }
        assertEquals(ClockType.getDefault(), ClockType.getById(ClockType.getDefaultId()))
    }
}
