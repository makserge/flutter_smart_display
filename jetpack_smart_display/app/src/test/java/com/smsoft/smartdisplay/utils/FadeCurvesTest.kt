package com.smsoft.smartdisplay.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.cbrt
import kotlin.math.log10

/**
 * The alarm fade curves: equal time steps must give equal steps of what is perceived, decibels for
 * the volume and CIE lightness for the light, and the ends must be exact.
 */
class FadeCurvesTest {

    private fun db(gain: Float) = 20.0 * log10(gain.toDouble())

    // CIE 1976 lightness L* of a relative luminance, written here independently of FadeCurves.kt.
    private fun lightness(luminance: Float): Double {
        val y = luminance.toDouble()
        return if (y > 216.0 / 24389.0) 116.0 * cbrt(y) - 16.0 else y * 24389.0 / 27.0
    }

    // The progress at each 100 ms step of a fade of [durationMs], start and end included.
    private fun steps(durationMs: Long) =
        (0..durationMs / STEP_MS).map { fadeProgress(it * STEP_MS, durationMs) }

    @Test
    fun progress() {
        assertEquals(0F, fadeProgress(-100L, ALARM_FADE_MS), 0F)
        assertEquals(0F, fadeProgress(0L, ALARM_FADE_MS), 0F)
        assertEquals(0.5F, fadeProgress(7_500L, ALARM_FADE_MS), 0F)
        assertEquals(1F, fadeProgress(ALARM_FADE_MS, ALARM_FADE_MS), 0F)
        assertEquals(1F, fadeProgress(60_000L, ALARM_FADE_MS), 0F)
        assertEquals(1F, fadeProgress(0L, 0L), 0F)
    }

    @Test
    fun volumeStartsSilentAndEndsAtTheTarget() {
        for (target in TARGETS) {
            assertEquals(0F, fadeVolume(0F, target, 0F), 0F)
            assertEquals(target, fadeVolume(0F, target, 1F), 0F)
            assertEquals(0F, fadeVolume(0F, target, -0.5F), 0F)
            assertEquals(target, fadeVolume(0F, target, 1.5F), 0F)
        }
    }

    @Test
    fun volumeRisesByEqualDecibelSteps() {
        // 40 dB in 150 steps of 100 ms; the first step comes out of silence at -40 dB of the
        // target. The shape in dB is the same for every target volume.
        for (target in TARGETS) {
            val volumes = steps(ALARM_FADE_MS).map { fadeVolume(0F, target, it) }
            assertEquals(151, volumes.size)
            assertEquals(-40.0 + 40.0 / 150, db(volumes[1] / target), 1e-3)
            for (i in 2 until volumes.size) {
                assertEquals(40.0 / 150, db(volumes[i]) - db(volumes[i - 1]), 1e-3)
            }
        }
    }

    @Test
    fun volumeLevelsAlongTheFade() {
        // dB relative to the target. The old linear fade was at -12 dB after a quarter, -6 dB
        // after half: loud at once, then hardly louder.
        val target = 0.2F
        assertEquals(-30.0, db(fadeVolume(0F, target, 0.25F) / target), 1e-3)
        assertEquals(-20.0, db(fadeVolume(0F, target, 0.5F) / target), 1e-3)
        assertEquals(-10.0, db(fadeVolume(0F, target, 0.75F) / target), 1e-3)
        assertEquals(-4.0, db(fadeVolume(0F, target, 0.9F) / target), 1e-3)
    }

    @Test
    fun volumeIsMonotonic() {
        val progress = (0..3_000).map { it / 3_000F }
        for (target in TARGETS) {
            progress.map { fadeVolume(0F, target, it) }.zipWithNext().forEach { (a, b) ->
                assertTrue("fade in: $a then $b", b >= a)
            }
            progress.map { fadeVolume(target, 0F, it) }.zipWithNext().forEach { (a, b) ->
                assertTrue("fade out: $a then $b", b <= a)
            }
        }
    }

    @Test
    fun fadeDownIsTheFadeInBackwards() {
        val target = 0.6F
        assertEquals(target, fadeVolume(target, 0F, 0F), 0F)
        assertEquals(0F, fadeVolume(target, 0F, 1F), 0F)
        for (p in steps(ALARM_FADE_MS)) {
            assertEquals(fadeVolume(0F, target, 1F - p), fadeVolume(target, 0F, p), 1e-6F)
        }
    }

    @Test
    fun fadeBetweenTwoVolumesIsEvenInDecibels() {
        // 0.25 -> 1 is 12.04 dB, in 10 equal steps either way.
        val stepDb = db(4F) / 10
        val progress = (0..10).map { it / 10F }
        val up = progress.map { fadeVolume(0.25F, 1F, it) }
        val down = progress.map { fadeVolume(1F, 0.25F, it) }
        assertEquals(0.25F, up.first(), 0F)
        assertEquals(1F, up.last(), 0F)
        assertEquals(1F, down.first(), 0F)
        assertEquals(0.25F, down.last(), 0F)
        up.zipWithNext().forEach { (a, b) -> assertEquals(stepDb, db(b) - db(a), 1e-3) }
        down.zipWithNext().forEach { (a, b) -> assertEquals(-stepDb, db(b) - db(a), 1e-3) }
    }

    @Test
    fun equalVolumesStay() {
        for (p in steps(ALARM_FADE_MS)) {
            assertEquals(0.3F, fadeVolume(0.3F, 0.3F, p), 0F)
            assertEquals(0F, fadeVolume(0F, 0F, p), 0F)
        }
    }

    @Test
    fun lightEnds() {
        assertEquals(0F, lightFadeLuminance(0F), 0F)
        assertEquals(1F, lightFadeLuminance(1F), 0F)
        assertEquals(0, lightFadePercent(0F))
        assertEquals(100, lightFadePercent(1F))
        assertEquals(0, lightFadePercent(-0.5F))
        assertEquals(100, lightFadePercent(1.5F))
    }

    @Test
    fun lightRisesByEqualLightnessSteps() {
        // L* rises by 100 / 300 per 100 ms step of the 30 s fade, also across the knee at L* = 8
        // where the CIE curve turns linear.
        val lightness = steps(LIGHT_FADE_MS).map { lightness(lightFadeLuminance(it)) }
        assertEquals(301, lightness.size)
        assertEquals(0.0, lightness.first(), 1e-9)
        assertEquals(100.0, lightness.last(), 1e-3)
        lightness.zipWithNext().forEach { (a, b) -> assertEquals(100.0 / 300, b - a, 1e-3) }
    }

    @Test
    fun lightIsMonotonic() {
        (0..3_000).map { lightFadeLuminance(it / 3_000F) }.zipWithNext().forEach { (a, b) ->
            assertTrue("$a then $b", b >= a)
        }
    }

    @Test
    fun dimmerPercentAlongTheFade() {
        assertEquals(1, lightFadePercent(0.1F))
        assertEquals(4, lightFadePercent(0.25F))
        assertEquals(18, lightFadePercent(0.5F))
        assertEquals(48, lightFadePercent(0.75F))
        assertEquals(76, lightFadePercent(0.9F))
    }

    @Test
    fun dimmerPercentIsSentOnceForEachValue() {
        // Sampled every 100 ms, the integer percent never rises by more than 1, so the fade sends
        // each of 0..100 exactly once: 101 values. The first 1 % comes after 1.4 s.
        val levels = steps(LIGHT_FADE_MS).map { lightFadePercent(it) }
        levels.zipWithNext().forEach { (a, b) -> assertTrue("$a then $b", b - a in 0..1) }
        assertEquals((0..100).toList(), levels.distinct())
        assertEquals(14, levels.indexOfFirst { it > 0 })
    }

    private companion object {
        const val STEP_MS = 100L
        const val ALARM_FADE_MS = 15_000L
        const val LIGHT_FADE_MS = 30_000L
        // The alarm volume slider's range (0.1..1) and its default.
        val TARGETS = listOf(0.1F, 0.2F, 0.55F, 1F)
    }
}
