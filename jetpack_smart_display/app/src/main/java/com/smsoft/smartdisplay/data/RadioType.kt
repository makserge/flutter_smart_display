package com.smsoft.smartdisplay.data

import android.content.Context
import com.smsoft.smartdisplay.R

enum class RadioType(val id: String, val titleId: Int) {
    INTERNAL("internal", R.string.radio_type_internal),
    MPD("mpd", R.string.radio_type_mpd);

    companion object {
        fun toMap(context: Context): Map<String, String> {
            return entries.associate {
                it.id to context.getString(it.titleId)
            }
        }

        fun getDefault(): RadioType {
            return INTERNAL
        }

        fun getDefaultId(): String {
            return getDefault().id
        }

        fun getById(id: String): RadioType {
            val item = entries.filter {
                it.id == id
            }
            return item[0]
        }
    }
}