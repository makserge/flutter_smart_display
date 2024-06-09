package com.smsoft.smartdisplay.data

sealed class Screen(val route: String) {
    data object Alarms : Screen("alarms")
    data object Clock : Screen("clock")
    data object Dashboard : Screen("dashboard")
    data object Doorbell : Screen("doorbell")
    data object Radio : Screen("radio")
    data object Sensors : Screen("sensors")
    data object Settings : Screen("settings")
    data object Timers : Screen("timers")
    data object Weather : Screen("weather")
}