package com.smsoft.smartdisplay.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The alarm ring: sound and light up in 60 s, both down in 10 s from the timeout. */
class AlarmEnvelopeTest {

    private val timeout = 60_000L

    @Test
    fun soundFadesInHoldsAndFadesOut() {
        assertEquals(0F, alarmVolumeAt(0.5F, 0L, timeout), 0F)
        assertEquals(0.5F, alarmVolumeAt(0.5F, ALARM_SOUND_FADE_IN_MS, timeout), 0F)
        assertEquals(0.5F, alarmVolumeAt(0.5F, timeout - 1L, timeout), 1e-4F)
        assertEquals(0.5F, alarmVolumeAt(0.5F, timeout, timeout), 0F)
        assertEquals(0F, alarmVolumeAt(0.5F, timeout + ALARM_FADE_OUT_MS, timeout), 0F)
        // Half way down is -20 dB of the 40 dB range
        assertEquals(0.05F, alarmVolumeAt(0.5F, timeout + ALARM_FADE_OUT_MS / 2, timeout), 1e-4F)
    }

    @Test
    fun soundRisesWithTheLight() {
        // Half way up the 60 s fade-in is -20 dB; the old 15 s fade was at its end after 15 s
        assertEquals(0.1F, alarmVolumeAt(1F, 30_000L, 120_000L), 1e-4F)
        assertEquals(1F, alarmVolumeAt(1F, ALARM_SOUND_FADE_IN_MS, 120_000L), 0F)
    }

    @Test
    fun aShortTimeoutFadesOutFromWhereTheFadeInGot() {
        val shortTimeout = 30_000L
        val atTimeout = alarmVolumeAt(1F, shortTimeout, shortTimeout)
        assertEquals(0.1F, atTimeout, 1e-4F)
        assertTrue(alarmVolumeAt(1F, shortTimeout + 1_000L, shortTimeout) < atTimeout)
        assertEquals(0F, alarmVolumeAt(1F, shortTimeout + ALARM_FADE_OUT_MS, shortTimeout), 0F)
    }

    @Test
    fun aLaterSoundFadesInFromItsOwnStart() {
        assertEquals(0F, alarmVolumeAt(1F, 20_000L, 120_000L, fadeInStartMs = 20_000L), 0F)
        assertEquals(1F, alarmVolumeAt(1F, 80_000L, 120_000L, fadeInStartMs = 20_000L), 0F)
    }

    @Test
    fun soundNeverRisesDuringTheFadeOut() {
        var last = alarmVolumeAt(1F, timeout, timeout)
        for (t in timeout..timeout + ALARM_FADE_OUT_MS step 100L) {
            val volume = alarmVolumeAt(1F, t, timeout)
            assertTrue("at $t", volume <= last)
            last = volume
        }
    }

    @Test
    fun lightTakesAMinuteUpAndTenSecondsDown() {
        assertEquals(0, alarmLightPercentAt(0L, timeout))
        assertEquals(18, alarmLightPercentAt(ALARM_LIGHT_FADE_IN_MS / 2, timeout))
        assertEquals(100, alarmLightPercentAt(ALARM_LIGHT_FADE_IN_MS, timeout))
        assertEquals(18, alarmLightPercentAt(timeout + ALARM_FADE_OUT_MS / 2, timeout))
        assertEquals(0, alarmLightPercentAt(timeout + ALARM_FADE_OUT_MS, timeout))
    }

    @Test
    fun aLightSwitchedOnLateStartsFromZero() {
        assertEquals(0, alarmLightPercentAt(10_000L, timeout, fadeInStartMs = 10_000L))
        // 30 s up of a 60 s fade at the timeout: half way in L*, then down from there
        val atTimeout = alarmLightPercentAt(timeout, timeout, fadeInStartMs = 30_000L)
        assertEquals(18, atTimeout)
        assertTrue(alarmLightPercentAt(timeout + 2_000L, timeout, fadeInStartMs = 30_000L) < atTimeout)
    }
}
