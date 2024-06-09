package com.smsoft.smartdisplay.data

import android.content.Context
import com.smsoft.smartdisplay.R

enum class TimerDurationType(val id: String, val titleId: Int, val command: VoiceCommandType) {
    DURATION1("60", R.string.timer_time_type_1, VoiceCommandType.TIMER_ON_1_MIN),
    DURATION2("120", R.string.timer_time_type_2, VoiceCommandType.TIMER_ON_2_MIN),
    DURATION3("180", R.string.timer_time_type_3, VoiceCommandType.TIMER_ON_3_MIN),
    DURATION4("240", R.string.timer_time_type_4, VoiceCommandType.TIMER_ON_4_MIN),
    DURATION5("300", R.string.timer_time_type_5, VoiceCommandType.TIMER_ON_5_MIN),
    DURATION6("360", R.string.timer_time_type_6, VoiceCommandType.TIMER_ON_6_MIN),
    DURATION7("420", R.string.timer_time_type_7, VoiceCommandType.TIMER_ON_7_MIN),
    DURATION8("480", R.string.timer_time_type_8, VoiceCommandType.TIMER_ON_8_MIN),
    DURATION9("540", R.string.timer_time_type_9, VoiceCommandType.TIMER_ON_9_MIN),
    DURATION10("600", R.string.timer_time_type_10, VoiceCommandType.TIMER_ON_10_MIN),
    DURATION15("900", R.string.timer_time_type_15, VoiceCommandType.TIMER_ON_15_MIN),
    DURATION20("1200", R.string.timer_time_type_20, VoiceCommandType.TIMER_ON_20_MIN),
    DURATION30("1800", R.string.timer_time_type_30, VoiceCommandType.TIMER_ON_30_MIN),
    DURATION45("2700", R.string.timer_time_type_45, VoiceCommandType.TIMER_ON_45_MIN),
    DURATION60("3600", R.string.timer_time_type_60, VoiceCommandType.TIMER_ON_60_MIN),
    DURATION90("5400", R.string.timer_time_type_90, VoiceCommandType.TIMER_ON_90_MIN),
    DURATION120("7200", R.string.timer_time_type_120, VoiceCommandType.TIMER_ON_120_MIN),
    DURATION150("9000", R.string.timer_time_type_150, VoiceCommandType.TIMER_ON_150_MIN),
    DURATION180("10800", R.string.timer_time_type_180, VoiceCommandType.TIMER_ON_180_MIN);

    companion object {
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

        fun getByCommand(command: VoiceCommandType): TimerDurationType? {
            val item = entries.filter {
                it.command == command
            }
            return if (item.isNotEmpty()) item[0] else null
        }
    }
}