package com.smsoft.smartdisplay.data

import android.content.Context
import com.smsoft.smartdisplay.R

enum class VoiceCommandType(
    val commandId: Int,
    val page: DashboardItem
) {
    CLOCK(R.string.clock_command, DashboardItem.CLOCK),
    WEATHER(R.string.weather_command, DashboardItem.WEATHER),
    SENSORS(R.string.sensors_command, DashboardItem.SENSORS),
    INTERNET_RADIO(R.string.internet_radio_command, DashboardItem.INTERNET_RADIO),
    ALARM(R.string.doorbell_command, DashboardItem.ALARMS),
    TIMER(R.string.timer_command, DashboardItem.TIMERS),
    DOORBELL(R.string.doorbell_command, DashboardItem.DOORBELL),
    INTERNET_RADIO_ON(R.string.internet_radio_on_command, DashboardItem.INTERNET_RADIO),
    INTERNET_RADIO_ON2(R.string.internet_radio_on2_command, DashboardItem.INTERNET_RADIO),
    INTERNET_RADIO_OFF(R.string.internet_radio_off_command, DashboardItem.INTERNET_RADIO),
    INTERNET_RADIO_OFF2(R.string.internet_radio_off2_command, DashboardItem.INTERNET_RADIO),
    INTERNET_RADIO_PREV_ITEM(R.string.internet_radio_prev_item_command, DashboardItem.INTERNET_RADIO),
    INTERNET_RADIO_NEXT_ITEM(R.string.internet_radio_prev_next_command, DashboardItem.INTERNET_RADIO),
    INTERNET_RADIO_VOL_DOWN(R.string.internet_radio_vol_down_command, DashboardItem.INTERNET_RADIO),
    INTERNET_RADIO_VOL_UP(R.string.internet_radio_vol_up_command, DashboardItem.INTERNET_RADIO),
    TIMER_ON_1_MIN(R.string.timer_1_min_command, DashboardItem.TIMERS),
    TIMER_ON_2_MIN(R.string.timer_2_min_command, DashboardItem.TIMERS),
    TIMER_ON_3_MIN(R.string.timer_3_min_command, DashboardItem.TIMERS),
    TIMER_ON_4_MIN(R.string.timer_4_min_command, DashboardItem.TIMERS),
    TIMER_ON_5_MIN(R.string.timer_5_min_command, DashboardItem.TIMERS),
    TIMER_ON_6_MIN(R.string.timer_6_min_command, DashboardItem.TIMERS),
    TIMER_ON_7_MIN(R.string.timer_7_min_command, DashboardItem.TIMERS),
    TIMER_ON_8_MIN(R.string.timer_8_min_command, DashboardItem.TIMERS),
    TIMER_ON_9_MIN(R.string.timer_9_min_command, DashboardItem.TIMERS),
    TIMER_ON_10_MIN(R.string.timer_10_min_command, DashboardItem.TIMERS),
    TIMER_ON_15_MIN(R.string.timer_15_min_command, DashboardItem.TIMERS),
    TIMER_ON_20_MIN(R.string.timer_20_min_command, DashboardItem.TIMERS),
    TIMER_ON_30_MIN(R.string.timer_30_min_command, DashboardItem.TIMERS),
    TIMER_ON_45_MIN(R.string.timer_45_min_command, DashboardItem.TIMERS),
    TIMER_ON_60_MIN(R.string.timer_60_min_command, DashboardItem.TIMERS),
    TIMER_ON_90_MIN(R.string.timer_90_min_command, DashboardItem.TIMERS),
    TIMER_ON_120_MIN(R.string.timer_120_min_command, DashboardItem.TIMERS),
    TIMER_ON_150_MIN(R.string.timer_150_min_command, DashboardItem.TIMERS),
    TIMER_ON_180_MIN(R.string.timer_180_min_command, DashboardItem.TIMERS);
    companion object {
        private var resourceCache: Map<String, DashboardItem> = HashMap()

        fun getDashboardItem(
            context: Context,
            command: String
        ): DashboardItem? {
            if (resourceCache.isEmpty()) {
                resourceCache = entries.associate {
                    context.getString(it.commandId) to it.page
                }
            }
            return resourceCache[command]
        }

        fun getByCommand(
            context: Context,
            command: String
        ) : VoiceCommandType {
            return entries.find {
                context.getString(it.commandId) == command
            } ?: CLOCK
        }
    }
}