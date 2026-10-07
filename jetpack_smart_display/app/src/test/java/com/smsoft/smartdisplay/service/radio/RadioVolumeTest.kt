package com.smsoft.smartdisplay.service.radio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.log10
import kotlin.math.roundToInt

/**
 * The radio volume the radio page shows and the voice commands step: even in loudness on the
 * internal player, MPD's own percent on MPD.
 */
class RadioVolumeTest {

    private fun db(gain: Float) = 20F * log10(gain)

    // The internal player's gain for a position, and for a slider value in percent
    private fun internalGain(position: Float) =
        RadioVolume.toPlayerVolume(position, isInternal = true)

    private fun internalGain(percent: Int) = internalGain(RadioVolume.fromPercent(percent))

    private fun up(position: Float) = RadioVolume.step(position, isUp = true)

    private fun down(position: Float) = RadioVolume.step(position, isUp = false)

    // What the page reads back after setting [position]: ExoPlayer keeps the gain as set; MPD
    // gets setvol with the rounded percent (MPDPlayer.handleSetVolume) and reports it / 100.
    private fun readBack(position: Float, isInternal: Boolean): Float {
        val playerVolume = RadioVolume.toPlayerVolume(position, isInternal)
        val reported = if (isInternal) playerVolume else (playerVolume * 100).roundToInt() / 100F
        return RadioVolume.toPosition(reported, isInternal)
    }

    @Test
    fun internalEnds() {
        // 0 is silence, 100 % full volume; a new ExoPlayer (volume 1) shows 100
        assertEquals(0F, internalGain(0), 0F)
        assertEquals(1F, internalGain(100), 1e-6F)
        assertEquals(0F, RadioVolume.toPosition(0F, isInternal = true), 0F)
        assertEquals(100, RadioVolume.toPercent(RadioVolume.toPosition(1F, isInternal = true)))
    }

    @Test
    fun internalSpansFortyDecibels() {
        assertEquals(-39.6F, db(internalGain(1)), 1e-3F)
        assertEquals(-20F, db(internalGain(50)), 1e-3F)
        assertEquals(-10F, db(internalGain(75)), 1e-3F)
        // A gain set from outside (e.g. the media session): 0.5 is -6 dB, 85 % of the slider
        assertEquals(85, RadioVolume.toPercent(RadioVolume.toPosition(0.5F, isInternal = true)))
    }

    @Test
    fun internalSliderStepsAreEqualDecibelSteps() {
        for (percent in 1 until 100) {
            val stepDb = db(internalGain(percent + 1)) - db(internalGain(percent))
            assertEquals("$percent %", 0.4F, stepDb, 1e-3F)
        }
    }

    @Test
    fun internalIsMonotonic() {
        for (percent in 0 until 100) {
            assertTrue("$percent %", internalGain(percent + 1) > internalGain(percent))
        }
    }

    @Test
    fun voiceStepIsTwoDecibelsAtAnyInternalVolume() {
        // Over the raw gain it was +6 dB at 5 % and +0.4 dB at 95 %
        for (percent in 1..95) {
            val position = RadioVolume.fromPercent(percent)
            val stepDb = db(internalGain(up(position))) - db(internalGain(percent))
            assertEquals("$percent % up", 2F, stepDb, 1e-3F)
        }
        for (percent in 6..100) {
            val position = RadioVolume.fromPercent(percent)
            val stepDb = db(internalGain(down(position))) - db(internalGain(percent))
            assertEquals("$percent % down", -2F, stepDb, 1e-3F)
        }
    }

    @Test
    fun voiceStepsStopAtTheEnds() {
        assertEquals(1F, up(1F), 0F)
        assertEquals(0F, down(0F), 0F)
        // Down from 5 % or less is silence, as before; up from silence is 5 % (-38 dB)
        assertEquals(0F, down(RadioVolume.fromPercent(5)), 0F)
        assertEquals(0F, down(RadioVolume.fromPercent(3)), 0F)
        assertEquals(0F, internalGain(down(0.05F)), 0F)
        assertEquals(-38F, db(internalGain(up(0F))), 1e-3F)
    }

    @Test
    fun repeatedVoiceStepsLandOnWholePercents() {
        // Adding 0.05F up gave 0.79999995 ..., shown as 79, 74, ... 4
        for (isInternal in listOf(true, false)) {
            var position = 1F
            for (expected in 95 downTo 0 step 5) {
                position = readBack(down(position), isInternal)
                assertEquals("internal $isInternal", expected, RadioVolume.toPercent(position))
            }
            for (expected in 5..100 step 5) {
                position = readBack(up(position), isInternal)
                assertEquals("internal $isInternal", expected, RadioVolume.toPercent(position))
            }
        }
    }

    @Test
    fun sliderValuesReadBackUnchanged() {
        // The thumb must not jump back while dragging: every value set is the value shown
        for (isInternal in listOf(true, false)) {
            for (percent in 0..100) {
                val position = readBack(RadioVolume.fromPercent(percent), isInternal)
                assertEquals("internal $isInternal", percent, RadioVolume.toPercent(position))
            }
        }
    }

    @Test
    fun mpdKeepsItsPercent() {
        // MPD's mixers already follow loudness, so the slider position is the setvol percent
        for (percent in 0..100) {
            val position = RadioVolume.fromPercent(percent)
            val playerVolume = RadioVolume.toPlayerVolume(position, isInternal = false)
            assertEquals(percent / 100F, playerVolume, 0F)
            assertEquals(percent, (playerVolume * 100).roundToInt())
            // MPD's 53 % (0.53F) was shown as 52 by the truncating slider
            val shown = RadioVolume.toPosition(percent / 100F, isInternal = false)
            assertEquals(percent, RadioVolume.toPercent(shown))
        }
    }

    @Test
    fun unknownVolumeShowsAnEmptySlider() {
        assertEquals(0, RadioVolume.toPercent(RadioVolume.UNKNOWN))
    }
}
