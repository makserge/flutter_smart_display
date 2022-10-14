package com.smsoft.smartdisplay.data

import android.content.Context
import com.smsoft.smartdisplay.R

enum class ClockType(val id: String, val titleId: Int, val previewScale: Float) {
    ANALOG_NIGHTDREAM("analogNightDream", R.string.analog_nightdream, 1F),
    ANALOG_CLOCKVIEW("analogClockView", R.string.analog_clockview, 0.7F),
    ANALOG_CLOCKVIEW2("analogClockView2", R.string.analog_clockview2, 1F),
    ANALOG_RECTANGULAR("analogRectangular", R.string.analog_rectangular, 0.5F),
    ANALOG_FSCLOCK("analogFsclock", R.string.analog_fsclock, 1F),
    ANALOG_JETALARM("analogJetAlarm", R.string.analog_jetalarm, 1F),
    DIGITAL_FLIPCLOCK("digitalFlipClock", R.string.digital_flipclock, 1F),
    DIGITAL_MATRIXCLOCK("digitalMatrixClock", R.string.digital_matrixclock, 1F),
    DIGITAL_CLOCK("digitalClock", R.string.digital_clock, 1F),
    DIGITAL_CLOCK2("digitalClock2", R.string.digital_clock2, 1F);

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
            return ANALOG_NIGHTDREAM
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