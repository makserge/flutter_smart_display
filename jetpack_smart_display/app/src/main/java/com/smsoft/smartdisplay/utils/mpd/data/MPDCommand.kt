package com.smsoft.smartdisplay.utils.mpd.data

enum class MPDCommand(val id: String) {
    IDLE("idle"),
    STATUS("status"),
    PLAYLIST("playlistid"),
    PLAYLIST_CHANGES("plchanges"),
    NEXT("next"),
    PREVIOUS("previous"),
    PLAY("play"),
    PAUSE("pause"),
    PLAY_ID("playid"),
    SET_VOLUME("setvol");
}