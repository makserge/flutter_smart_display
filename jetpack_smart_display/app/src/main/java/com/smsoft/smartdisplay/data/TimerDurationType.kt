package com.smsoft.smartdisplay.data

import android.content.Context
import com.smsoft.smartdisplay.R

enum class TimerDurationType(val id: String, val titleId: Int, vararg val commandIds: Int) {
    DURATION1("60", R.string.timer_time_type_1, R.string.timer_1_min_command),
    DURATION2("120", R.string.timer_time_type_2, R.string.timer_2_min_command),
    DURATION3("180", R.string.timer_time_type_3, R.string.timer_3_min_command),
    DURATION4("240", R.string.timer_time_type_4, R.string.timer_4_min_command),
    DURATION5("300", R.string.timer_time_type_5, R.string.timer_5_min_command),
    DURATION6("360", R.string.timer_time_type_6, R.string.timer_6_min_command),
    DURATION7("420", R.string.timer_time_type_7, R.string.timer_7_min_command),
    DURATION8("480", R.string.timer_time_type_8, R.string.timer_8_min_command),
    DURATION9("540", R.string.timer_time_type_9, R.string.timer_9_min_command),
    DURATION10("600", R.string.timer_time_type_10, R.string.timer_10_min_command),
    DURATION15("900", R.string.timer_time_type_15, R.string.timer_15_min_command),
    DURATION20("1200", R.string.timer_time_type_20, R.string.timer_20_min_command),
    DURATION30("1800", R.string.timer_time_type_30, R.string.timer_30_min_command, R.string.timer_30_min_command2),
    DURATION45("2700", R.string.timer_time_type_45, R.string.timer_45_min_command),
    DURATION60("3600", R.string.timer_time_type_60, R.string.timer_60_min_command, R.string.timer_60_min_command2),
    DURATION90("5400", R.string.timer_time_type_90, R.string.timer_90_min_command, R.string.timer_90_min_command2),
    DURATION120("7200", R.string.timer_time_type_120, R.string.timer_120_min_command, R.string.timer_120_min_command2),
    DURATION150("9000", R.string.timer_time_type_150, R.string.timer_150_min_command),
    DURATION180("10800", R.string.timer_time_type_180, R.string.timer_180_min_command, R.string.timer_180_min_command2);

    companion object {
        private var commandCache: Map<String, TimerDurationType> = emptyMap()

        fun toMap(context: Context): Map<String, String> {
            return entries.associate {
                it.id to context.getString(it.titleId)
            }
        }

        fun getDefault(): TimerDurationType {
            return DURATION15
        }

        fun getDefaultId(): String {
            return getDefault().id
        }

        fun getByCommand(context: Context, command: String): TimerDurationType? {
            if (commandCache.isEmpty()) {
                commandCache = entries.flatMap { type ->
                    type.commandIds.map { commandId -> normalize(context.getString(commandId)) to type }
                }.toMap()
            }
            return commandCache[normalize(command)]
        }

        private fun normalize(value: String): String {
            return value.trim().lowercase().replace(Regex("\\s+"), "")
        }
    }
}