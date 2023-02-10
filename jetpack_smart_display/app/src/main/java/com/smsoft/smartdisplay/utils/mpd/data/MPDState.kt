package com.smsoft.smartdisplay.utils.mpd.data

enum class MPDState(private val id: String) {
    UNKNOWN("unknown"),
    PLAYING("play"),
    STOPPED("stop"),
    PAUSED("pause");

    companion object {
        fun getById(id: String): MPDState {
            val item = MPDState.values().filter {
                it.id == id
            }
            return item[0]
        }
    }
}
