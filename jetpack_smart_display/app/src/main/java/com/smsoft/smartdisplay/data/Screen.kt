package com.smsoft.smartdisplay.data

sealed class Screen(val route: String) {
    object Clock : Screen("clock")
    object ClockSettings : Screen("clocksettings")
    object Dashboard : Screen("dashboard")
    object Doorbell : Screen("doorbell")
    object Radio : Screen("radio")
}