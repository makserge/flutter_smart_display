package com.smsoft.smartdisplay.utils

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

/**
 * VolumeFader on a test clock, in a blocking scope that waits for the fade to end. Every volume
 * change moves the clock on by [TICK_MS], as if the next change came after the 100 ms step; the
 * real delay between changes is only 1 ms.
 */
class VolumeFaderTest {

    private var now = 5_000_000L
    private var tickMs = TICK_MS
    // (time into the fade, volume) of every change.
    private val changes = mutableListOf<Pair<Long, Float>>()
    private var fadeStart = 0L

    private fun record(volume: Float) {
        changes += (now - fadeStart) to volume
        now += tickMs
    }

    /** Runs [block] with a fader on the test clock and waits until its fades are over. */
    private fun withFader(block: (VolumeFader) -> Unit) {
        fadeStart = now
        runBlocking {
            block(VolumeFader(this, EmptyCoroutineContext) { now })
        }
    }

    private fun curve(elapsedMs: Long) = fadeVolume(0F, TARGET, fadeProgress(elapsedMs, DURATION_MS))

    @Test
    fun followsTheCurveByTheClock() {
        withFader { it.fadeIn(0F, TARGET, step = 1L, durationMillis = DURATION_MS, onChange = ::record) }

        assertEquals((0..DURATION_MS step TICK_MS).toList(), changes.map { it.first })
        assertEquals(changes.map { curve(it.first) }, changes.map { it.second })
        assertEquals(0F, changes.first().second, 0F)
        assertEquals(TARGET, changes.last().second, 0F)
    }

    @Test
    fun resumedFadeContinuesOnTheCurve() {
        withFader { it.fadeIn(0F, TARGET, step = 1L, durationMillis = DURATION_MS, onChange = ::record) }
        val uninterrupted = changes.toList()
        changes.clear()

        // A ring that a new dashboard takes over 600 ms into the fade.
        withFader {
            it.fadeIn(0F, TARGET, step = 1L, durationMillis = DURATION_MS, elapsedMillis = 600L, onChange = ::record)
        }

        assertEquals(uninterrupted.drop(6).map { it.second }, changes.map { it.second })
        assertEquals(TARGET, changes.last().second, 0F)
    }

    @Test
    fun lateStepsDoNotStretchTheFade() {
        // Every step comes 400 ms late: the volume follows the clock, and the fade still ends
        // after its duration, with fewer changes.
        tickMs = 400L
        withFader { it.fadeIn(0F, TARGET, step = 1L, durationMillis = DURATION_MS, onChange = ::record) }

        assertEquals(listOf(0L, 400L, 800L, 1_200L, 1_600L), changes.map { it.first })
        assertEquals(listOf(curve(0L), curve(400L), curve(800L), curve(1_200L), TARGET), changes.map { it.second })
    }

    @Test
    fun cancelStopsTheFade() {
        withFader { fader ->
            fader.fadeIn(0F, TARGET, step = 1L, durationMillis = DURATION_MS) {
                record(it)
                if (changes.size == 3) {
                    fader.cancel()
                }
            }
        }

        assertEquals(3, changes.size)
    }

    @Test
    fun aNewFadeReplacesTheRunningOne() {
        // The second fade starts within a change of the first one; the first takes no further step.
        withFader { fader ->
            fader.fadeIn(0F, TARGET, step = 1L, durationMillis = DURATION_MS) {
                record(it)
                if (changes.size == 3) {
                    fader.fadeIn(TARGET, 0F, step = 1L, durationMillis = 300L, onChange = ::record)
                }
            }
        }

        val second = changes.drop(3).map { it.second }
        assertEquals(4, second.size)
        assertEquals(TARGET, second.first(), 0F)
        assertEquals(0F, second.last(), 0F)
        second.zipWithNext().forEach { (a, b) -> assertTrue("$a then $b", b < a) }
    }

    @Test
    fun fadeBetweenEqualVolumesEndsAtOnce() {
        // The linear fader never ended this one: its step was 0, so it never reached the target.
        withFader { it.fadeIn(0.3F, 0.3F, step = 1L, durationMillis = DURATION_MS, onChange = ::record) }

        assertEquals(listOf(0L to 0.3F), changes)
    }

    @Test
    fun fadeWithoutDurationSetsTheTarget() {
        withFader { it.fadeIn(0F, TARGET, step = 1L, durationMillis = 0L, onChange = ::record) }

        assertEquals(listOf(0L to TARGET), changes)
    }

    private companion object {
        const val TICK_MS = 100L
        const val DURATION_MS = 1_500L
        const val TARGET = 0.2F
    }
}
