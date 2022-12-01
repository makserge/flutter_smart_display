package com.smsoft.smartdisplay.data

import com.smsoft.smartdisplay.R

enum class DashboardItems(val textId: Int, val iconId: Int, val route: String) {
    INTERNET_RADIO(R.string.internet_radio, R.drawable.ic_internet_radio_96, Screen.Radio.route),
    BLUETOOTH(R.string.bluetooth, R.drawable.ic_bluetooth_96, ""),
    MUSIC(R.string.music, R.drawable.ic_music_96, ""),
    RADIO(R.string.radio, R.drawable.ic_radio_96, ""),
    VIDEO(R.string.video, R.drawable.ic_video_96, ""),
    TIMER(R.string.timer, R.drawable.ic_timer_96, "")
}