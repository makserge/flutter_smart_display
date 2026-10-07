package com.smsoft.smartdisplay.utils.mpd

import android.os.SystemClock
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.smsoft.smartdisplay.utils.mpd.data.MPDCredentials
import com.smsoft.smartdisplay.utils.mpd.data.MPDStatus

/** A song (radio station) in the MPD queue. */
data class MpdSong(
    val id: Int,
    val pos: Int,
    val file: String,
    val name: String?,
    val title: String?
) {
    /** Unique within the queue: MPD never reuses a song id while the song is queued. */
    val uid: String
        get() = "mpd-$id"

    /** icy-name or Artist when MPD knows it, else the "#label" of a stream URI, else the title. */
    val stationName: String
        get() = name
            ?: file.substringAfter('#', "").takeIf { file.contains("://") && it.isNotEmpty() }
            ?: title
            ?: ""

    fun toMediaItem(): MediaItem = MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(file)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setDisplayTitle(stationName)
                .setStation(stationName)
                .build()
        )
        .build()

    /** Metadata of the playing stream: station as display title, StreamTitle as title. */
    fun toPlayingMetadata(): MediaMetadata = MediaMetadata.Builder()
        .setDisplayTitle(stationName)
        .setStation(stationName)
        .setTitle(title)
        .build()
}

/** Answer of one refresh. [queue] is null when it was not requested. */
class MpdSnapshot(
    val status: MPDStatus,
    val currentSong: MpdSong?,
    val queue: List<MpdSong>?
)

/**
 * The command side of the MPD client: one lazily (re)connected connection.
 * Every method blocks; all calls must come from MPDPlayer's single MPD thread.
 */
class MPDHelper(private val credentials: MPDCredentials) {
    private var connection: Connection? = null

    /** status + currentsong (+ playlistid) in one command list. */
    @Throws(MpdException::class)
    fun fetch(includeQueue: Boolean): MpdSnapshot = withConnection { c ->
        val answers = if (includeQueue) {
            c.commandList(listOf("status"), listOf("currentsong"), listOf("playlistid"))
        } else {
            c.commandList(listOf("status"), listOf("currentsong"))
        }
        MpdSnapshot(
            status = MPDStatus(answers[0]),
            currentSong = parseSongs(answers[1]).firstOrNull(),
            queue = if (includeQueue) parseSongs(answers[2]) else null
        )
    }

    fun play() = playback("play")

    fun playId(songId: Int) = playback("playid", songId.toString())

    /** Explicit "pause 1": a bare "pause" toggles and could resume playback instead. */
    fun pause() = playback("pause", "1")

    fun stop() = playback("stop")

    fun setVolume(volume: Int) = playback("setvol", volume.coerceIn(0, 100).toString())

    /** Closes the command connection; the next call opens a new one. */
    fun disconnect() {
        connection?.close()
        connection = null
    }

    /**
     * A refused playback command (e.g. "playid" of a song another client just removed) is only
     * logged: the refresh that follows shows what MPD really does.
     */
    @Throws(MpdException::class)
    private fun playback(name: String, vararg args: String) {
        try {
            withConnection { it.command(name, *args) }
        } catch (e: MpdAckException) {
            Log.w(TAG, "MPD refused \"$name\": ${e.message}")
        }
    }

    /**
     * Runs [block] on the command connection. A connection that MPD may already have dropped
     * (silent for longer than its connection_timeout) is replaced first; a connection lost during
     * the command is reopened once and the command repeated (all commands sent here are idempotent).
     * Timeouts are not repeated: that would only double the wait for a hung server.
     * "No permission" means the password setting is missing or wrong (MpdSetupException).
     */
    @Throws(MpdException::class)
    private fun <T> withConnection(block: (Connection) -> T): T {
        try {
            val current = connection?.takeIf {
                it.isConnected && (SystemClock.elapsedRealtime() - it.lastAnswerAt) < STALE_AFTER_MS
            } ?: reconnect()
            return try {
                block(current)
            } catch (_: MpdConnectionException) {
                block(reconnect())
            }
        } catch (e: MpdAckException) {
            if ((e.code == MpdAckException.ACK_ERROR_PERMISSION) || (e.code == MpdAckException.ACK_ERROR_PASSWORD)) {
                throw MpdSetupException("MPD refused \"${e.command}\": ${e.message}", e)
            }
            throw e
        }
    }

    @Throws(MpdException::class)
    private fun reconnect(): Connection {
        connection?.close()
        val newConnection = Connection(credentials.host, credentials.port, credentials.password)
        connection = newConnection
        newConnection.connect()
        return newConnection
    }

    private fun parseSongs(lines: List<String>): List<MpdSong> {
        val songs = ArrayList<MpdSong>()
        var fields = HashMap<String, String>()
        fun flush() {
            val file = fields["file"]
            val id = fields["Id"]?.toIntOrNull()
            if (file != null && id != null) {
                songs.add(
                    MpdSong(
                        id = id,
                        pos = fields["Pos"]?.toIntOrNull() ?: songs.size,
                        file = file,
                        name = fields["Name"] ?: fields["Artist"],
                        title = fields["Title"]
                    )
                )
            }
            fields = HashMap()
        }
        for (line in lines) {
            val colon = line.indexOf(": ")
            if (colon <= 0) {
                continue
            }
            val key = line.substring(0, colon)
            if ((key == "file") && fields.isNotEmpty()) {
                flush()
            }
            // The first value wins: a stream may repeat Title.
            fields.putIfAbsent(key, line.substring(colon + 2))
        }
        flush()
        return songs
    }
}

/** Reconnect before MPD's default connection_timeout (60 s) can have closed the connection. */
private const val STALE_AFTER_MS = 50_000L
private const val TAG = "MPDHelper"
