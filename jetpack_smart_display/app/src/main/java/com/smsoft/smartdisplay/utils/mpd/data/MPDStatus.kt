package com.smsoft.smartdisplay.utils.mpd.data

/** Answer of the MPD "status" command. Unknown or malformed lines are ignored. */
class MPDStatus(status: List<String>) {
    /** 0..100, or -1 when MPD has no mixer (line missing or "volume: -1"). */
    var volume = -1
        private set
    var playlistVersion = -1
        private set
    var playlistLength = 0
        private set
    /** Position of the current song in the queue, -1 when there is none. */
    var songPos = -1
        private set
    var songId = -1
        private set
    var isRepeat = false
        private set
    var isRandom = false
        private set
    var state = MPDState.UNKNOWN
        private set
    var error: String? = null
        private set
    /** Elapsed time of the current song in ms ("elapsed", else the seconds of "time"). */
    var elapsedMs = 0L
        private set
    var isUpdating = false
        private set

    init {
        for (line in status) {
            val colon = line.indexOf(": ")
            if (colon <= 0) {
                continue
            }
            val value = line.substring(colon + 2)
            try {
                when (line.substring(0, colon)) {
                    "volume" -> volume = value.toInt()
                    "playlist" -> playlistVersion = value.toInt()
                    "playlistlength" -> playlistLength = value.toInt()
                    "song" -> songPos = value.toInt()
                    "songid" -> songId = value.toInt()
                    "repeat" -> isRepeat = value == "1"
                    "random" -> isRandom = value == "1"
                    "state" -> state = MPDState.getById(value)
                    "error" -> error = value
                    "elapsed" -> elapsedMs = (value.toDouble() * 1000).toLong()
                    "time" -> if (elapsedMs == 0L) elapsedMs = value.substringBefore(':').toLong() * 1000
                    "updating_db" -> isUpdating = true
                }
            } catch (_: NumberFormatException) {
            }
        }
    }
}
