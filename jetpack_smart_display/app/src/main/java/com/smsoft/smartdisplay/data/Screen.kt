package com.smsoft.smartdisplay.data

sealed class Screen(val route: String) {
    object Clock : Screen("clock")
    object Settings : Screen("settings")
    object Dashboard : Screen("dashboard")
    object Doorbell : Screen("doorbell")
    object Radio : Screen("radio")
    object Sensors : Screen("sensors")
    object Weather : Screen("weather")
}