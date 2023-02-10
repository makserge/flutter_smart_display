package com.smsoft.smartdisplay.utils.mpd.data

data class MPDCredentials(
    val host: String,
    val port: Int,
    val password: String = ""
)
