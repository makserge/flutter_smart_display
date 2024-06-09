package com.smsoft.smartdisplay.data

import android.content.Context
import com.smsoft.smartdisplay.R

enum class ClockType(val id: String, val titleId: Int) {
    ANALOG_NIGHTDREAM("analogNightDream", R.string.analog_nightdream),
    ANALOG_CLOCKVIEW("analogClockView", R.string.analog_clockview),
    ANALOG_CLOCKVIEW2("analogClockView2", R.string.analog_clockview2),
    ANALOG_RECTANGULAR("analogRectangular", R.string.analog_rectangular),
    ANALOG_FSCLOCK("analogFsclock", R.string.analog_fsclock),
    ANALOG_JETALARM("analogJetAlarm", R.string.analog_jetalarm),
    DIGITAL_FLIPCLOCK("digitalFlipClock", R.string.digital_flipclock),
    DIGITAL_MATRIXCLOCK("digitalMatrixClock", R.string.digital_matrixclock),
    DIGITAL_CLOCK("digitalClock", R.string.digital_clock),
    DIGITAL_CLOCK2("digitalClock2", R.string.digital_clock2);

    companion object {
        fun toMap(context: Context): Map<String, String> {
            return entries.associate {
                it.id to context.getString(it.titleId)
            }
        }

        fun getDefault(): ClockType {
            return ANALOG_NIGHTDREAM
        }

        fun getDefaultId(): String {
            return getDefault().id
        }

        fun getById(id: String): ClockType {
            val item = entries.filter {
                it.id == id
            }
            return item[0]
        }
    }
}