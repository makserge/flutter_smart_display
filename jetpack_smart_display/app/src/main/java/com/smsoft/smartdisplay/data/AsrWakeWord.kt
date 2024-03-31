package com.smsoft.smartdisplay.data

import android.content.Context
import com.smsoft.smartdisplay.R

enum class AsrWakeWord(val id: String, val titleId: Int) {
    JARVIS("jarvis", R.string.asr_wakeword_jarvis),
    COMPUTER("computer", R.string.asr_wakeword_computer);

    companion object {
        fun toMap(context: Context): Map<String, String> {
            return entries.associate {
                it.id to context.getString(it.titleId)
            }
        }

        fun getDefault(): AsrWakeWord {
            return JARVIS
        }

        fun getDefaultId(): String {
            return getDefault().id
        }

        fun getById(id: String): AsrWakeWord {
            val item = entries.filter {
                it.id == id
            }
            return item[0]
        }
    }
}