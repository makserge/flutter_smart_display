package com.smsoft.smartdisplay.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.log10

/**
 * The volume sliders in Settings (alarm, timer, message, voice chime; gain 0.1..1) are even in
 * loudness, and the scale with a mute at 0 (the radio's) keeps its curve above 0.
 */
class LoudnessScaleTest {

    private val scale = VOLUME_SETTING_SCALE

    private val muteScale = LoudnessScale(minVolume = 0.01F, isMuteAtZero = true)

    private fun db(gain: Float) = 20F * log10(gain)

    @Test
    fun ends() {
        assertEquals(0.1F, scale.volume(0F), 1e-6F)
        assertEquals(1F, scale.volume(1F), 1e-6F)
        assertEquals(0F, scale.position(0.1F), 1e-6F)
        assertEquals(1F, scale.position(1F), 1e-6F)
    }

    @Test
    fun equalSliderStepsAreEqualDecibelSteps() {
        // -20 dB at 0 to 0 dB at 1: 2 dB per tenth of the slider
        for (i in 0..10) {
            assertEquals("position ${i / 10F}", -20F + 2F * i, db(scale.volume(i / 10F)), 1e-3F)
        }
        assertEquals(-10F, db(scale.volume(0.5F)), 1e-3F)
    }

    @Test
    fun savedVolumesKeepTheirLoudness() {
        // The stored value is still the gain: every old setting maps to a position and back
        for (gain in listOf(0.1F, 0.15F, 0.2F, 0.3162F, 0.55F, 0.8F, 1F)) {
            assertEquals("$gain", gain, scale.volume(scale.position(gain)), 1e-5F)
        }
        // The default 0.2 (-14 dB) sits at 30 % of the slider; the linear slider had it at 11 %
        assertEquals(0.301F, scale.position(0.2F), 1e-3F)
    }

    @Test
    fun timerMessageAndChimeDefaultsStayFullVolume() {
        // Their default gain 1 is the right end, and the old slider's left end (0.1) the left end
        assertEquals(1F, scale.position(1F), 0F)
        assertEquals(1F, scale.volume(scale.position(1F)), 0F)
        assertEquals(0F, scale.position(0.1F), 1e-6F)
    }

    @Test
    fun outOfRangeVolumesSitAtTheEnds() {
        assertEquals(0F, scale.position(0F), 0F)
        assertEquals(0F, scale.position(0.05F), 0F)
        assertEquals(1F, scale.position(1.5F), 0F)
        assertEquals(0.1F, scale.volume(-1F), 1e-6F)
        assertEquals(1F, scale.volume(2F), 1e-6F)
    }

    @Test
    fun monotonic() {
        var last = 0F
        for (i in 0..1000) {
            val volume = scale.volume(i / 1000F)
            assertTrue("step $i", volume > last)
            last = volume
        }
    }

    @Test
    fun muteAtZeroBothWays() {
        assertEquals(0F, muteScale.volume(0F), 0F)
        assertEquals(0F, muteScale.volume(-0.5F), 0F)
        assertEquals(0F, muteScale.position(0F), 0F)
        assertEquals(1F, muteScale.volume(1F), 1e-6F)
        assertEquals(1F, muteScale.position(1F), 1e-6F)
        // A gain below the curve (set from outside) reads as the mute end
        assertEquals(0F, muteScale.position(0.005F), 0F)
        // Without the flag, 0 is the quietest level, not silence
        assertEquals(0.01F, LoudnessScale(minVolume = 0.01F).volume(0F), 1e-7F)
    }

    @Test
    fun muteScaleKeepsTheCurveAboveZero() {
        // Every position above 0 is where the scale without mute has it: 0.4 dB per hundredth
        val plain = LoudnessScale(minVolume = 0.01F)
        for (i in 1..100) {
            val position = i / 100F
            val volume = muteScale.volume(position)
            assertEquals("position $position", plain.volume(position), volume, 0F)
            assertEquals("position $position", -40F + 0.4F * i, db(volume), 1e-3F)
        }
    }

    @Test
    fun muteScaleIsMonotonic() {
        var last = -1F
        for (i in 0..1000) {
            val volume = muteScale.volume(i / 1000F)
            assertTrue("step $i", volume > last)
            last = volume
        }
    }

    @Test
    fun muteScaleRoundTrips() {
        for (i in 0..1000) {
            val position = i / 1000F
            val back = muteScale.position(muteScale.volume(position))
            assertEquals("position $position", position, back, 1e-5F)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun minVolumeMustBeAboveZero() {
        LoudnessScale(minVolume = 0F)
    }
}
