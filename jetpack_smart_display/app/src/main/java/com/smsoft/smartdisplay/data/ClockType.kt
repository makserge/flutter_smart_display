package com.smsoft.smartdisplay.data

import android.content.Context
import com.smsoft.smartdisplay.R

enum class ClockType(val id: String, val titleId: Int) {
    ANALOG_ROUND("analogRound", R.string.analog_round),
    ANALOG_RECTANGULAR("analogRectangular", R.string.analog_rectangular),
    ANALOG_FSCLOCK("analogFsclock", R.string.analog_fsclock),
    DIGITAL("digital", R.string.digital);

    companion object {
        fun toMap(context: Context): Map<String, String> {
            return values().associate {
                it.id to context.getString(it.titleId)
            }
        }

        fun getTitleIdById(id: String): Int {
            return getById(id).titleId
        }

        fun getDefault(): ClockType {
            return ANALOG_ROUND
        }

        fun getDefaultId(): String {
            return getDefault().id
        }

        fun getDefaultTitleId(): Int {
            return getDefault().titleId
        }

        fun getById(id: String): ClockType {
            val item = values().filter {
                it.id == id
            }
            return item[0]
        }
    }
}