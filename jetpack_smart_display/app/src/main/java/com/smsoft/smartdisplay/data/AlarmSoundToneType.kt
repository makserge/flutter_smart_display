package com.smsoft.smartdisplay.data

import android.content.Context
import com.smsoft.smartdisplay.R

enum class AlarmSoundToneType(val id: String, val titleId: Int, val path: String) {
    ALARM_BEEP1("alarm_beep1", R.string.alarm_sound_tone_ab1, "asset:///sounds/alarms/Alarm_Beep_01.ogg"),
    ALARM_BEEP2("alarm_beep2", R.string.alarm_sound_tone_ab2, "asset:///sounds/alarms/Alarm_Beep_02.ogg"),
    ALARM_BEEP3("alarm_beep3", R.string.alarm_sound_tone_ab3, "asset:///sounds/alarms/Alarm_Beep_03.ogg"),
    ALARM_BUZZER("alarm_buzzer", R.string.alarm_sound_tone_alarm_buzzer, "asset:///sounds/alarms/Alarm_Buzzer.ogg"),
    ALARM_ROOSTER("alarm_rooster", R.string.alarm_sound_tone_alarm_rooster,"asset:///sounds/alarms/Alarm_Rooster_02.ogg"),
    ARGON("argon", R.string.alarm_sound_tone_argon,"asset:///sounds/alarms/Argon.ogg"),
    BARIUM("barium", R.string.alarm_sound_tone_barium, "asset:///sounds/alarms/Barium.ogg"),
    CARBON("carbon", R.string.alarm_sound_tone_carbon,"asset:///sounds/alarms/Carbon.ogg"),
    CYAN_ALARM("cyan_alarm", R.string.alarm_sound_tone_cyan_alarm,"asset:///sounds/alarms/CyanAlarm.ogg"),
    HASSIUM("hassium", R.string.alarm_sound_tone_hassium, "asset:///sounds/alarms/Hassium.ogg"),
    KRYPTON("krypton", R.string.alarm_sound_tone_krypton,"asset:///sounds/alarms/Krypton.ogg"),
    NEON("neon", R.string.alarm_sound_tone_neon,"asset:///sounds/alarms/Neon.ogg"),
    NUCLEAR_LAUNCH("nuclear_launch", R.string.alarm_sound_tone_nuclear_launch,"asset:///sounds/alarms/NuclearLaunch.ogg"),
    OSMIUM("osmium", R.string.alarm_sound_tone_osmium,"asset:///sounds/alarms/Osmium.ogg"),
    PLATINUM("platinum", R.string.alarm_sound_tone_platinum,"asset:///sounds/alarms/Platinum.ogg"),
    SCANDIUM("scandium", R.string.alarm_sound_tone_scandium,"asset:///sounds/alarms/Scandium.ogg");

    companion object {
        fun toMap(context: Context): Map<String, String> {
            return entries.associate {
                it.id to context.getString(it.titleId)
            }
        }

        fun getDefault(): AlarmSoundToneType {
            return ARGON
        }

        fun getDefaultId(): String {
            return getDefault().id
        }

        fun getById(id: String): AlarmSoundToneType {
            val item = entries.filter {
                it.id == id
            }
            return item[0]
        }
    }
}