package com.smsoft.smartdisplay.data

enum class DashboardItem {
    CLOCK,
    SENSORS,
    INTERNET_RADIO;

    companion object {
        fun getItem(index: Int): DashboardItem {
            return when (index) {
                0 -> CLOCK
                1 -> SENSORS
                else -> INTERNET_RADIO
            }
        }
    }
}