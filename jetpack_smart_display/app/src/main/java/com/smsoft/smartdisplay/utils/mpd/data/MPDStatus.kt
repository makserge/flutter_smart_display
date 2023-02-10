package com.smsoft.smartdisplay.utils.mpd.data

class MPDStatus(status: List<String>) {
    var volume = 0
        private set
    private var bitrate = 0
    var playlistVersion = 0
        private set
    private var playlistLength = 0
    var songPos = 0
        private set
    private var songId = 0
    var isRepeat = false
        private set
    var isRandom = false
        private set
    private var isSingle = false
    var state = MPDState.UNKNOWN
        private set
    private var error: String? = null
    var elapsedTime = 0L
        private set
    var totalTime = 0L
        private set
    private var crossFade = 0
    private var sampleRate = 0
    private var channels = 0
    private var bitsPerSample = 0
    var isUpdating = false
        private set
    private var nextSong = 0
    private var nextSongId = 0
    private var isConsume = false

    init {
        update(status)
    }

    private fun update(status: List<String>) {
        isUpdating = false
        for (line in status) {
            try {
                if (line.startsWith("volume:")) volume = line.substring("volume: ".length).toInt()
                else if (line.startsWith("bitrate:")) bitrate = line.substring("bitrate: ".length).toInt()
                else if (line.startsWith("playlist:")) playlistVersion = line.substring("playlist: ".length).toInt()
                else if (line.startsWith("playlistlength:")) playlistLength = line.substring("playlistlength: ".length).toInt()
                else if (line.startsWith("song:")) songPos = line.substring("song: ".length).toInt()
                else if (line.startsWith("songid:")) songId = line.substring("songid: ".length).toInt()
                else if (line.startsWith("repeat:")) isRepeat = ("1" == line.substring("repeat: ".length))
                else if (line.startsWith("random:")) isRandom = "1" == line.substring("random: ".length)
                else if (line.startsWith("state:")) state = MPDState.getById(line.substring("state: ".length))
                else if (line.startsWith("error:")) error = line.substring("error: ".length)
                else if (line.startsWith("time:")) {
                    val time = line.substring("time: ".length).split(":".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    elapsedTime = time[0].toLong()
                    totalTime = time[1].toLong()
                } else if (line.startsWith("audio:")) {
                    val audio = line.substring("audio: ".length).split(":".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    try {
                        sampleRate = audio[0].toInt()
                        bitsPerSample = audio[1].toInt()
                        channels = audio[2].toInt()
                    } catch (ignored: NumberFormatException) {
                    }
                } else if (line.startsWith("xfade:")) crossFade = line.substring("xfade: ".length).toInt()
                else if (line.startsWith("updating_db:")) isUpdating = true
                else if (line.startsWith("nextsong:")) nextSong = line.substring("nextsong: ".length).toInt()
                else if (line.startsWith("nextsongid:")) nextSongId = line.substring("nextsongid: ".length).toInt()
                else if (line.startsWith("consume:")) isConsume = "1" == line.substring("consume: ".length)
                else if (line.startsWith("single:")) isSingle = "1" == line.substring("single: ".length)
            } catch (ignored: RuntimeException) {
            }
        }
    }
}