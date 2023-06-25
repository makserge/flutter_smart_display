package com.smsoft.smartdisplay.data

import android.content.Context
import com.smsoft.smartdisplay.R

enum class VoiceCommand(
    val commandId: Int,
    val page: DashboardItem
) {
    CLOCK(R.string.clock_command, DashboardItem.CLOCK),
    WEATHER(R.string.weather_command, DashboardItem.WEATHER),
    SENSORS(R.string.sensors_command, DashboardItem.SENSORS),
    INTERNET_RADIO(R.string.internet_radio_command, DashboardItem.INTERNET_RADIO),
    DOORBELL(R.string.doorbell_command, DashboardItem.DOORBELL),
    INTERNET_RADIO_ON(R.string.internet_radio_on_command, DashboardItem.INTERNET_RADIO),
    INTERNET_RADIO_ON2(R.string.internet_radio_on2_command, DashboardItem.INTERNET_RADIO),
    INTERNET_RADIO_OFF(R.string.internet_radio_off_command, DashboardItem.INTERNET_RADIO),
    INTERNET_RADIO_OFF2(R.string.internet_radio_off2_command, DashboardItem.INTERNET_RADIO),
    INTERNET_RADIO_PREV_ITEM(R.string.internet_radio_prev_item_command, DashboardItem.INTERNET_RADIO),
    INTERNET_RADIO_NEXT_ITEM(R.string.internet_radio_prev_next_command, DashboardItem.INTERNET_RADIO),
    INTERNET_RADIO_VOL_DOWN(R.string.internet_radio_vol_down_command, DashboardItem.INTERNET_RADIO),
    INTERNET_RADIO_VOL_UP(R.string.internet_radio_vol_up_command, DashboardItem.INTERNET_RADIO);
    companion object {
        private var resourceCache: Map<String, DashboardItem> = HashMap()

        fun getDashboardItem(
            context: Context,
            command: String
        ): DashboardItem? {
            if (resourceCache.isEmpty()) {
                resourceCache = VoiceCommand.values().associate {
                    context.getString(it.commandId) to it.page
                }
            }
            return resourceCache[command]
        }

        fun getByCommand(
            context: Context,
            command: String
        ) : VoiceCommand? {
            return VoiceCommand.values().find {
                context.getString(it.commandId) == command
            }
        }
    }
}