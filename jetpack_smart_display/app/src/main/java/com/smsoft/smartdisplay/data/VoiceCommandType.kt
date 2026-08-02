package com.smsoft.smartdisplay.data

import android.content.Context
import com.smsoft.smartdisplay.R

enum class VoiceCommandType(
    val commandIds: List<Int>,
    val page: DashboardItem
) {
    CLOCK(listOf(R.string.clock_command), DashboardItem.CLOCK),
    WEATHER(listOf(R.string.weather_command), DashboardItem.WEATHER),
    SENSORS(listOf(R.string.sensors_command), DashboardItem.SENSORS),
    INTERNET_RADIO(listOf(R.string.internet_radio_command), DashboardItem.INTERNET_RADIO),
    ALARM(listOf(R.string.alarm_command), DashboardItem.ALARMS),
    TIMER(listOf(R.string.timer_command), DashboardItem.TIMERS),
    DOORBELL(listOf(R.string.doorbell_command), DashboardItem.DOORBELL),
    INTERNET_RADIO_ON(
        listOf(R.string.internet_radio_on_command, R.string.internet_radio_on2_command),
        DashboardItem.INTERNET_RADIO
    ),
    INTERNET_RADIO_OFF(
        listOf(R.string.internet_radio_off_command, R.string.internet_radio_off2_command),
        DashboardItem.INTERNET_RADIO
    ),
    INTERNET_RADIO_PREV_ITEM(listOf(R.string.internet_radio_prev_item_command), DashboardItem.INTERNET_RADIO),
    INTERNET_RADIO_NEXT_ITEM(listOf(R.string.internet_radio_prev_next_command), DashboardItem.INTERNET_RADIO),
    INTERNET_RADIO_VOL_DOWN(listOf(R.string.internet_radio_vol_down_command), DashboardItem.INTERNET_RADIO),
    INTERNET_RADIO_VOL_UP(listOf(R.string.internet_radio_vol_up_command), DashboardItem.INTERNET_RADIO),

    TIMER_SET(emptyList(), DashboardItem.TIMERS);

    companion object {
        private var resourceCache: Map<String, VoiceCommandType> = emptyMap()

        private fun cache(context: Context): Map<String, VoiceCommandType> {
            if (resourceCache.isEmpty()) {
                resourceCache = entries
                    .flatMap { type -> type.commandIds.map { context.getString(it) to type } }
                    .toMap()
            }
            return resourceCache
        }

        fun getDashboardItem(
            context: Context,
            command: String
        ): DashboardItem? {
            return cache(context)[command]?.page
        }

        fun getByCommand(
            context: Context,
            command: String
        ) : VoiceCommandType {
            return cache(context)[command] ?: CLOCK
        }
    }
}