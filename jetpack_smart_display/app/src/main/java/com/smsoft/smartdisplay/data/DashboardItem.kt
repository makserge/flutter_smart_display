package com.smsoft.smartdisplay.data

enum class DashboardItem {
    CLOCK,
    WEATHER,
    SENSORS,
    INTERNET_RADIO,
    DOORBELL;

    companion object {
        fun getItem(index: Int): DashboardItem {
            return when (index) {
                0 -> CLOCK
                1 -> WEATHER
                2 -> SENSORS
                3 -> INTERNET_RADIO
                else -> DOORBELL
            }
        }
    }
}