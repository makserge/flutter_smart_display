package com.smsoft.smartdisplay.data

import android.content.Context
import com.smsoft.smartdisplay.R

enum class AlarmSoundType(val id: String, val titleId: Int) {
    TONE("tone", R.string.alarm_sound_type_tone),
    RADIO("radio", R.string.alarm_sound_type_radio);

    companion object {
        fun toMap(context: Context): Map<String, String> {
            return entries.associate {
                it.id to context.getString(it.titleId)
            }
        }

        fun getDefault(): AlarmSoundType {
            return TONE
        }

        fun getDefaultId(): String {
            return getDefault().id
        }

        fun getById(id: String): AlarmSoundType {
            val item = entries.filter {
                it.id == id
            }
            return item[0]
        }
    }
}