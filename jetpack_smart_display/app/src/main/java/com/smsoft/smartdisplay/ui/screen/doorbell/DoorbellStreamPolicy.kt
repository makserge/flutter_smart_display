package com.smsoft.smartdisplay.ui.screen.doorbell

// When DoorbellStreamPlayer gives up on a connection attempt and how long it waits before the
// next one. Times are SystemClock.elapsedRealtime() milliseconds.

/** How often the attempt of the current visit is checked. */
internal const val WATCHDOG_INTERVAL_MS = 1_000L

/**
 * No Playing event by then: the attempt hangs while connecting. An HTTP camera can accept the
 * connection and send nothing. For RTSP, libVLC 3 gives up on the OPTIONS and DESCRIBE requests
 * after ipv4-timeout (5 s) by itself, but waits for the camera's SETUP and PLAY answers without a
 * limit, and stop() cannot cut that wait short: it returns when the camera answers or the
 * connection fails, possibly never (a camera that lost power after it took the request). The
 * timeout cannot free such a player; the next attempt then starts without waiting for it (see
 * DoorbellStreamPlayer.Visit.reconnect).
 */
internal const val CONNECT_TIMEOUT_MS = 15_000L

/**
 * Playing, but the stream time or the shown pictures have not moved for this long: the picture is
 * frozen. libVLC ends an RTSP stream itself after "no data received in 10s", but that does not
 * cover an HTTP camera or a decoder that stopped showing pictures.
 */
internal const val STALL_TIMEOUT_MS = 10_000L

/** Delay before the first reconnect; it doubles with every failed attempt in a row. */
internal const val RECONNECT_FIRST_DELAY_MS = 1_000L

/** A camera that stays away is tried this often (one that never played: see reconnectDelayMs). */
internal const val RECONNECT_MAX_DELAY_MS = 10_000L

/**
 * After this many failures in a row while no attempt of the visit has played, the delays grow
 * further: from [SLOW_RECONNECT_FIRST_DELAY_MS] up to [SLOW_RECONNECT_MAX_DELAY_MS].
 */
internal const val SLOW_RECONNECT_AFTER_FAILURES = 5

/** The first delay once a stream that never played is tried less often; it doubles from there. */
internal const val SLOW_RECONNECT_FIRST_DELAY_MS = 60_000L

/** A stream that never played is still tried this often, so a camera that boots comes up. */
internal const val SLOW_RECONNECT_MAX_DELAY_MS = 300_000L

/** An attempt that has played this long was a success: the next failure waits the first delay. */
internal const val STABLE_PLAYBACK_MS = 30_000L

/** What the watchdog does with an attempt. */
internal enum class StreamHealth { OK, CONNECT_TIMEOUT, STALLED }

/**
 * When a counter of an attempt (the stream time, the number of shown pictures) last moved; null
 * until it has moved once. Not every stream reports both: libVLC 3 reports no RTSP stream time
 * without a normal play time from the camera, and the time of an HTTP MJPEG stream stays 0. A
 * counter that never moved is therefore not used, so such a stream is not reconnected over and
 * over.
 */
internal class ProgressSignal {
    private var value: Long? = null

    var lastChangeMs: Long? = null
        private set

    fun update(newValue: Long, nowMs: Long) {
        val oldValue = value
        value = newValue
        if (oldValue != null && newValue != oldValue) {
            lastChangeMs = nowMs
        }
    }
}

/**
 * Health of an attempt at [nowMs] that called play() at [startedMs] and got its first Playing
 * event at [playingSinceMs] (null before). [lastTimeChangeMs] and [lastPictureChangeMs] are the
 * [ProgressSignal.lastChangeMs] of the stream time and of the shown pictures.
 */
internal fun streamHealth(
    nowMs: Long,
    startedMs: Long,
    playingSinceMs: Long?,
    lastTimeChangeMs: Long?,
    lastPictureChangeMs: Long?
): StreamHealth = when {
    playingSinceMs == null ->
        if (nowMs - startedMs >= CONNECT_TIMEOUT_MS) StreamHealth.CONNECT_TIMEOUT
        else StreamHealth.OK
    lastTimeChangeMs != null && nowMs - lastTimeChangeMs >= STALL_TIMEOUT_MS -> StreamHealth.STALLED
    lastPictureChangeMs != null && nowMs - lastPictureChangeMs >= STALL_TIMEOUT_MS ->
        StreamHealth.STALLED
    else -> StreamHealth.OK
}

/** The attempt has played long enough to reset the backoff ([STABLE_PLAYBACK_MS]). */
internal fun isStablePlayback(nowMs: Long, playingSinceMs: Long?): Boolean =
    playingSinceMs != null && nowMs - playingSinceMs >= STABLE_PLAYBACK_MS

/**
 * Delay before the next attempt after [failuresInARow] failed attempts before the one that just
 * failed: 1 s, 2 s, 4 s, 8 s, then 10 s. Once an attempt of the visit has played ([hasPlayed]),
 * that stays so: the camera rebooted or dropped the stream and should be back soon.
 *
 * A stream that has never played since the visit started probably cannot open at all: a wrong
 * URL or camera password fails every attempt, and libVLC's error event does not say why (the app
 * sets no login dialog, so a 401 fails at once). Retried every 10 s, a wrong password would be a
 * failed login on the camera every 10 s, for hours on the dashboard page (it has no back timer),
 * and cameras can lock an account after failed logins. From the [SLOW_RECONNECT_AFTER_FAILURES]th
 * failure in a row on, such a stream waits 60 s, 120 s, 240 s, then 5 min. It is never given up:
 * a camera that is still booting comes up, only later.
 */
internal fun reconnectDelayMs(failuresInARow: Int, hasPlayed: Boolean): Long =
    if (!hasPlayed && failuresInARow >= SLOW_RECONNECT_AFTER_FAILURES) {
        val doublings = (failuresInARow - SLOW_RECONNECT_AFTER_FAILURES).coerceIn(0, MAX_DOUBLINGS)
        (SLOW_RECONNECT_FIRST_DELAY_MS shl doublings).coerceAtMost(SLOW_RECONNECT_MAX_DELAY_MS)
    } else {
        (RECONNECT_FIRST_DELAY_MS shl failuresInARow.coerceIn(0, MAX_DOUBLINGS))
            .coerceAtMost(RECONNECT_MAX_DELAY_MS)
    }

/**
 * The reconnect after [failuresInARow] failures is the first with the long delays of
 * [reconnectDelayMs], which is worth one line in the log.
 */
internal fun startsSlowReconnects(failuresInARow: Int, hasPlayed: Boolean): Boolean =
    !hasPlayed && failuresInARow == SLOW_RECONNECT_AFTER_FAILURES

// Enough to reach either maximum, small enough that the shifts cannot overflow.
private const val MAX_DOUBLINGS = 16

/**
 * An `rtsps://` (RTSP over TLS) URL. libVLC 3 cannot play it: its RTSP access (live555) only
 * handles `rtsp://`; RTSPS came with VLC 4.
 */
internal fun isRtspsUrl(url: String): Boolean {
    val trimmed = url.trim()
    val colon = trimmed.indexOf(':')
    return colon > 0 && trimmed.substring(0, colon).equals("rtsps", ignoreCase = true)
}
