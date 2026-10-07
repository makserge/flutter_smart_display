package com.smsoft.smartdisplay.ui.screen.doorbell

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * When the doorbell stream gives up on an attempt and how long it waits before the next one.
 * Times are elapsed-realtime milliseconds; an attempt starts at [START].
 */
class DoorbellStreamPolicyTest {

    private fun health(
        now: Long,
        playingSince: Long? = null,
        lastTimeChange: Long? = null,
        lastPictureChange: Long? = null
    ) = streamHealth(now, START, playingSince, lastTimeChange, lastPictureChange)

    @Test
    fun connecting_okUntilTheConnectTimeout() {
        assertEquals(StreamHealth.OK, health(START))
        assertEquals(StreamHealth.OK, health(START + CONNECT_TIMEOUT_MS - 1))
        assertEquals(StreamHealth.CONNECT_TIMEOUT, health(START + CONNECT_TIMEOUT_MS))
    }

    @Test
    fun playing_neverTimesOutWhileConnecting() {
        val now = START + CONNECT_TIMEOUT_MS * 3
        assertEquals(StreamHealth.OK, health(now, playingSince = START + 500))
    }

    @Test
    fun playing_withoutAnyProgressSignal_isNotStalled() {
        // An MJPEG stream (time stays 0) on a player without statistics: nothing to judge by.
        val now = START + STALL_TIMEOUT_MS * 10
        assertEquals(StreamHealth.OK, health(now, playingSince = START + 500))
    }

    @Test
    fun streamTimeStops_stalledAfterTheStallTimeout() {
        val lastChange = START + 20_000
        val stopsAt = lastChange + STALL_TIMEOUT_MS
        assertEquals(
            StreamHealth.OK,
            health(stopsAt - 1, playingSince = START, lastTimeChange = lastChange)
        )
        assertEquals(
            StreamHealth.STALLED,
            health(stopsAt, playingSince = START, lastTimeChange = lastChange)
        )
    }

    @Test
    fun picturesStop_stalledAlthoughTheTimeMoves() {
        // A decoder that stopped showing pictures while data still arrives: a frozen picture.
        val now = START + 60_000
        assertEquals(
            StreamHealth.STALLED,
            health(
                now,
                playingSince = START,
                lastTimeChange = now - 200,
                lastPictureChange = now - STALL_TIMEOUT_MS
            )
        )
    }

    @Test
    fun bothSignalsMoving_ok() {
        val now = START + 60_000
        assertEquals(
            StreamHealth.OK,
            health(
                now,
                playingSince = START,
                lastTimeChange = now - 250,
                lastPictureChange = now - 1_000
            )
        )
    }

    @Test
    fun progressSignal_armedOnlyAfterTheFirstChange() {
        val signal = ProgressSignal()
        assertNull(signal.lastChangeMs)
        signal.update(0, 1_000)
        assertNull("the first value is only the baseline", signal.lastChangeMs)
        signal.update(0, 2_000)
        assertNull("a value that never moves does not arm it", signal.lastChangeMs)
        signal.update(40, 3_000)
        assertEquals(3_000L, signal.lastChangeMs)
        signal.update(40, 4_000)
        assertEquals("an unchanged value keeps the last change", 3_000L, signal.lastChangeMs)
        signal.update(12, 5_000)
        assertEquals("a jump back is a change too", 5_000L, signal.lastChangeMs)
    }

    @Test
    fun reconnectDelay_afterPlaying_doublesFromOneSecondUpToTenSeconds() {
        assertEquals(
            listOf(1_000L, 2_000L, 4_000L, 8_000L, 10_000L, 10_000L, 10_000L),
            (0..6).map { reconnectDelayMs(it, hasPlayed = true) }
        )
    }

    @Test
    fun reconnectDelay_afterPlaying_manyFailures_staysAtTenSeconds() {
        // A camera that has played is retried quickly however long it stays away.
        assertEquals(RECONNECT_MAX_DELAY_MS, reconnectDelayMs(1_000, hasPlayed = true))
        assertEquals(RECONNECT_MAX_DELAY_MS, reconnectDelayMs(Int.MAX_VALUE, hasPlayed = true))
    }

    @Test
    fun reconnectDelay_neverPlayed_slowsDownAfterFiveFailures() {
        // A wrong URL or password: the first retries as fast as for a camera that played, then
        // 1 min doubling up to 5 min.
        assertEquals(
            listOf(
                1_000L, 2_000L, 4_000L, 8_000L, 10_000L,
                60_000L, 120_000L, 240_000L, 300_000L, 300_000L
            ),
            (0..9).map { reconnectDelayMs(it, hasPlayed = false) }
        )
    }

    @Test
    fun reconnectDelay_neverPlayed_manyFailures_staysAtFiveMinutes() {
        assertEquals(SLOW_RECONNECT_MAX_DELAY_MS, reconnectDelayMs(1_000, hasPlayed = false))
        assertEquals(
            SLOW_RECONNECT_MAX_DELAY_MS,
            reconnectDelayMs(Int.MAX_VALUE, hasPlayed = false)
        )
    }

    @Test
    fun reconnectDelay_negativeCount_isTheFirstDelay() {
        assertEquals(RECONNECT_FIRST_DELAY_MS, reconnectDelayMs(-1, hasPlayed = true))
        assertEquals(RECONNECT_FIRST_DELAY_MS, reconnectDelayMs(-1, hasPlayed = false))
    }

    @Test
    fun slowReconnects_startOnceAndOnlyForAStreamThatNeverPlayed() {
        val starts = (0..20).filter { startsSlowReconnects(it, hasPlayed = false) }
        assertEquals(listOf(SLOW_RECONNECT_AFTER_FAILURES), starts)
        assertEquals(
            "it is the first long delay",
            SLOW_RECONNECT_FIRST_DELAY_MS,
            reconnectDelayMs(starts.single(), hasPlayed = false)
        )
        assertTrue((0..20).none { startsSlowReconnects(it, hasPlayed = true) })
    }

    @Test
    fun stablePlayback_afterThirtySecondsOfPlaying() {
        assertFalse(isStablePlayback(START + 60_000, playingSinceMs = null))
        assertFalse(isStablePlayback(START + STABLE_PLAYBACK_MS - 1, playingSinceMs = START))
        assertTrue(isStablePlayback(START + STABLE_PLAYBACK_MS, playingSinceMs = START))
    }

    @Test
    fun rtspsUrl_detected() {
        assertTrue(isRtspsUrl("rtsps://user:pass@192.168.1.20:322/stream1"))
        assertTrue(isRtspsUrl("RTSPS://camera/live"))
        assertTrue(isRtspsUrl("  rtsps://camera/live  "))
    }

    @Test
    fun otherUrls_notRtsps() {
        assertFalse(isRtspsUrl(""))
        assertFalse(isRtspsUrl("rtsp://user:pass@192.168.1.20:554/stream1"))
        assertFalse(isRtspsUrl("http://esp32cam.local:81/stream"))
        assertFalse(isRtspsUrl("https://camera/live.m3u8"))
        assertFalse(isRtspsUrl("rtsp://camera/rtsps"))
        assertFalse(isRtspsUrl(":rtsps"))
    }

    private companion object {
        const val START = 100_000L
    }
}
