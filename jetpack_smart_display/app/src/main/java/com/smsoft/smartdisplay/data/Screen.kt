package com.smsoft.smartdisplay.data

sealed class Screen(val route: String) {
    object Clock : Screen("clock")
    object ClockSettings : Screen("clock-settings")
    object Dashboard : Screen("dashboard")
    object Doorbell : Screen("doorbell")
    object Radio : Screen("radio")
    object Sensors : Screen("sensors")
    object SensorsSettings : Screen("sensors-settings")
}