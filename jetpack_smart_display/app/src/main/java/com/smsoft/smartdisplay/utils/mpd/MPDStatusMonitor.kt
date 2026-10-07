package com.smsoft.smartdisplay.utils.mpd

import android.util.Log
import com.smsoft.smartdisplay.utils.mpd.data.MPDCredentials

/** The subsystems whose changes the radio shows. */
private val IDLE_SUBSYSTEMS = arrayOf("player", "playlist", "mixer", "options")

/**
 * After this long without news the monitor checks that the server is still there ("noidle").
 * MPD does not time out clients in idle, but a powered-off server or a dropped network does not
 * close the socket either; without the check the monitor waited forever.
 */
const val MPD_IDLE_CHECK_MS = 55_000

private const val RETRY_MIN_MS = 1_000L
private const val RETRY_MAX_MS = 30_000L

/**
 * Watches MPD on its own connection with "idle" and reports changes. Runs on its own thread from
 * [start] until [stop]; one instance per prepare() of the player, so two monitors can never share
 * a socket. Listener calls come from the monitor thread.
 */
class MPDStatusMonitor(
    private val credentials: MPDCredentials,
    private val listener: Listener
) {
    interface Listener {
        /** true after every (re)connect: anything may have changed in between. */
        fun onConnectionChanged(isConnected: Boolean)

        fun onChanged(subsystems: Set<String>)
    }

    @Volatile
    private var isStopped = false
    @Volatile
    private var connection: Connection? = null
    private val thread = Thread({ run() }, "MPD idle").apply { isDaemon = true }

    fun start() {
        thread.start()
    }

    /** Any thread. Closing the socket ends a blocked idle at once. */
    fun stop() {
        isStopped = true
        connection?.close()
        thread.interrupt()
    }

    private fun run() {
        var retryDelayMs = RETRY_MIN_MS
        var isReportedConnected = false
        while (!isStopped) {
            val current = Connection(credentials.host, credentials.port, credentials.password)
            connection = current
            try {
                current.connect()
                if (isStopped) {
                    break
                }
                retryDelayMs = RETRY_MIN_MS
                isReportedConnected = true
                listener.onConnectionChanged(true)
                while (!isStopped) {
                    val changes = current.idle(MPD_IDLE_CHECK_MS, *IDLE_SUBSYSTEMS)
                    if (changes.isNotEmpty()) {
                        listener.onChanged(changes.toSet())
                    }
                }
            } catch (e: MpdException) {
                current.close()
                if (isStopped) {
                    break
                }
                Log.w(TAG, "MPD idle connection failed: ${e.message}")
                if (isReportedConnected) {
                    isReportedConnected = false
                    listener.onConnectionChanged(false)
                }
                try {
                    Thread.sleep(retryDelayMs)
                } catch (_: InterruptedException) {
                }
                retryDelayMs = (retryDelayMs * 2).coerceAtMost(RETRY_MAX_MS)
            }
        }
        connection?.close()
    }
}

private const val TAG = "MPDStatusMonitor"
