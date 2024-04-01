package com.smsoft.smartdisplay.data

enum class AudioType(val path: String) {
    WAKE_WORD("asset:///sounds/ding.wav"),
    ERROR("asset:///sounds/dong.wav");
}