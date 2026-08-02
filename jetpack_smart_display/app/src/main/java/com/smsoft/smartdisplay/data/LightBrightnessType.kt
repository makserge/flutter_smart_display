package com.smsoft.smartdisplay.data

import android.content.Context
import com.smsoft.smartdisplay.R

enum class LightBrightnessType(val percent: Int, val commandId: Int) {
    PERCENT_10(10, R.string.light_brightness_10_command),
    PERCENT_20(20, R.string.light_brightness_20_command),
    PERCENT_30(30, R.string.light_brightness_30_command),
    PERCENT_40(40, R.string.light_brightness_40_command),
    PERCENT_50(50, R.string.light_brightness_50_command),
    PERCENT_60(60, R.string.light_brightness_60_command),
    PERCENT_70(70, R.string.light_brightness_70_command),
    PERCENT_80(80, R.string.light_brightness_80_command),
    PERCENT_90(90, R.string.light_brightness_90_command),
    PERCENT_100(100, R.string.light_brightness_100_command);

    companion object {
        private var commandCache: Map<String, LightBrightnessType> = emptyMap()

        fun getByCommand(context: Context, command: String): LightBrightnessType? {
            if (commandCache.isEmpty()) {
                commandCache = entries.associateBy { context.getString(it.commandId) }
            }
            return commandCache[command]
        }
    }
}
