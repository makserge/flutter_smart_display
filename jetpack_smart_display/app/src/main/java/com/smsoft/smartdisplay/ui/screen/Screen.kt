package com.smsoft.smartdisplay.ui.screen

sealed class Screen(val route: String) {
    object Clock : Screen("clock")
    object ClockSettings : Screen("clocksettings")
    object Dashboard : Screen("dashboard")
    object Doorbell : Screen("doorbell")
}