package com.smsoft.smartdisplay.data

import android.content.Context
import com.smsoft.smartdisplay.R

enum class Font(val font: Int, val titleId: Int) {
    ROBOTO_REGULAR(R.font.roboto_regular, R.string.digit_font_roboto_regular),
    ROBOTO_LIGHT(R.font.roboto_light, R.string.digit_font_roboto_light),
    ROBOTO_THIN(R.font.roboto_thin, R.string.digit_font_roboto_thin),
    SEVEN_SEGMENT_DIGITAL(R.font.seven_segment_digital, R.string.digit_font_seven_segment_digital),
    DSEG14_CLASSIC(R.font.dseg14classic, R.string.digit_font_dseg14_classic);

    companion object {
        fun toMap(context: Context): Map<String, String> {
            return entries.associate {
                it.font.toString() to context.getString(it.titleId)
            }
        }

        fun getDefault(): Int {
            return SEVEN_SEGMENT_DIGITAL.font
        }

        // The stored id is the raw R.font value, which can change between builds. An unknown id
        // falls back to the default font instead of crashing the clock page after an app update.
        fun getById(id: String): Font {
            return entries.firstOrNull {
                it.font.toString() == id
            } ?: SEVEN_SEGMENT_DIGITAL
        }
    }
}