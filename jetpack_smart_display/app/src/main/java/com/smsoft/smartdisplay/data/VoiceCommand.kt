package com.smsoft.smartdisplay.data

import android.content.Context
import com.smsoft.smartdisplay.R

enum class VoiceCommand(val commandId: Int, val page: DashboardItem) {
    CLOCK(R.string.clock_command, DashboardItem.CLOCK),
    WEATHER(R.string.weather_command, DashboardItem.WEATHER),
    SENSORS(R.string.sensors_command, DashboardItem.SENSORS),
    INTERNET_RADIO(R.string.internet_radio_command, DashboardItem.INTERNET_RADIO),
    DOORBELL(R.string.doorbell_command, DashboardItem.DOORBELL);

    companion object {
        private var resourceCache: Map<String, DashboardItem> = HashMap()

        fun getDashboardItem(
            context: Context,
            command: String
        ): DashboardItem? {
            if (resourceCache.isEmpty()) {
                resourceCache = VoiceCommand.values().associate {
                    context.getString(it.commandId) to (it.page)
                }
            }
            return resourceCache[command]
        }
    }
}