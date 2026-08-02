package com.smsoft.smartdisplay.data

import com.smsoft.smartdisplay.R

/**
 * Groups the settings sub-sections into meaningful tabs for the Settings screen, instead of
 * one long scrolling list. Add new settings sections to whichever tab they belong with, or add
 * a new tab entry here (and a matching branch in SettingsScreen's pager) if none fit.
 */
enum class SettingsTab(val titleId: Int) {
    GENERAL(R.string.settings_tab_general),
    CONNECTIVITY(R.string.settings_tab_connectivity),
    SENSORS(R.string.settings_tab_sensors),
    ALARM(R.string.settings_tab_alarm),
    VOICE_ALERTS(R.string.settings_tab_voice_alerts);

    companion object {
        fun getItem(index: Int): SettingsTab {
            return when (index) {
                0 -> GENERAL
                1 -> CONNECTIVITY
                2 -> SENSORS
                3 -> ALARM
                else -> VOICE_ALERTS
            }
        }
    }
}
