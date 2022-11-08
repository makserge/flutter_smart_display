package com.smsoft.smartdisplay.ui.composable.clock.nightdream

import android.content.Context
import androidx.compose.runtime.Composable
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.utils.getStateFromFlow
import com.smsoft.smartdisplay.ui.screen.clock.ClockViewModel

data class AnalogClockConfig(
    var digitStyle: DigitStyle = DigitStyle.getDefault(),
    var digitPosition: Float = DEFAULT_DIGIT_POSITION_ND,
    var highlightQuarterOfHour: Boolean = DEFAULT_DIGIT_EMP_QUARTERS_ND,
    var fontUri: String = "file:///android_asset/fonts/" + Font.getDefault(),
    var fontSize: Float = DEFAULT_DIGIT_FONT_SIZE_ND / 100,
    var handStyle: HandStyle = HandStyle.getDefault(),
    var handLengthMinutes: Float = DEFAULT_HAND_LEN_MIN_ND,
    var handLengthHours: Float = DEFAULT_HAND_LEN_HOURS_ND,
    var handWidthMinutes: Float = DEFAULT_HAND_WIDTH_MIN_ND / 100,
    var handWidthHours: Float = DEFAULT_HAND_WIDTH_HOURS_ND / 100,
    var showSecondHand: Boolean = DEFAULT_SHOW_SECOND_HAND_ND,
    var tickStyleMinutes: TickStyle = TickStyle.getDefault(),
    var tickStartMinutes: Float = DEFAULT_TICK_START_MINUTES_ND,
    var tickLengthMinutes: Float = DEFAULT_TICK_LEN_MINUTES_ND / 100,
    var tickStyleHours: TickStyle = TickStyle.CIRCLE,
    var tickStartHours: Float = DEFAULT_TICK_START_HOURS_ND,
    var tickLengthHours: Float = DEFAULT_TICK_LEN_HOURS_ND / 100,
    var emphasizeHour12: Boolean = true,
    var tickWidthHours: Float = 0.01F,
    var tickWidthMinutes: Float = 0.01F,
    var innerCircleRadius: Float = DEFAULT_INNER_CIRCLE_RADIUS_ND / 10
) {
    @Composable
    fun InitDataStore(
        viewModel: ClockViewModel,
    ) {
        digitStyle = getStateFromFlow(
            flow = viewModel.digitStyleND,
            defaultValue = DigitStyle.getDefault()
        ) as DigitStyle

        digitPosition = getStateFromFlow(
            flow = viewModel.digitPositionND,
            defaultValue = DEFAULT_DIGIT_POSITION_ND
        ) as Float

        highlightQuarterOfHour = getStateFromFlow(
            flow = viewModel.highlightQuarterOfHourND,
            defaultValue = DEFAULT_DIGIT_EMP_QUARTERS_ND
        ) as Boolean

        fontUri = "file:///android_asset/fonts/" + getStateFromFlow(
            flow = viewModel.fontUriND,
            defaultValue = Font.getDefault()
        )

        fontSize = getStateFromFlow(
            flow = viewModel.fontSizeND,
            defaultValue = DEFAULT_DIGIT_FONT_SIZE_ND
        ) as Float / 100

        handStyle = getStateFromFlow(
            flow = viewModel.handStyleND,
            defaultValue = HandStyle.getDefault()
        )
        as HandStyle

        handLengthMinutes = getStateFromFlow(
            flow = viewModel.handLengthMinutesND,
            defaultValue = DEFAULT_HAND_LEN_MIN_ND
        ) as Float

        handLengthHours = getStateFromFlow(
            flow = viewModel.handLengthHoursND,
            defaultValue = DEFAULT_HAND_LEN_HOURS_ND
        ) as Float

        handWidthMinutes = getStateFromFlow(
            flow = viewModel.handWidthMinutesND,
            defaultValue = DEFAULT_HAND_WIDTH_MIN_ND
        ) as Float / 100

        handWidthHours = getStateFromFlow(
            flow = viewModel.handWidthHoursND,
            defaultValue = DEFAULT_HAND_WIDTH_HOURS_ND
        ) as Float / 100

        showSecondHand = getStateFromFlow(
            flow = viewModel.showSecondHandND,
            defaultValue = DEFAULT_SHOW_SECOND_HAND_ND
        ) as Boolean

        tickStyleMinutes = getStateFromFlow(
            flow = viewModel.tickStyleMinutesND,
            defaultValue = TickStyle.getDefault()
        ) as TickStyle

        tickStartMinutes = getStateFromFlow(
            flow = viewModel.tickStartMinutesND,
            defaultValue = DEFAULT_TICK_START_MINUTES_ND
        ) as Float

        tickLengthMinutes = getStateFromFlow(
            flow = viewModel.tickLengthMinutesND,
            defaultValue = DEFAULT_TICK_LEN_MINUTES_ND
        ) as Float / 100

        tickStyleHours = getStateFromFlow(
            flow = viewModel.tickStyleHoursND,
            defaultValue = TickStyle.CIRCLE
        ) as TickStyle

        tickStartHours = getStateFromFlow(
            flow = viewModel.tickStartHoursND,
            defaultValue = DEFAULT_TICK_START_HOURS_ND
        ) as Float

        tickLengthHours = getStateFromFlow(
            flow = viewModel.tickLengthHoursND,
            defaultValue = DEFAULT_TICK_LEN_HOURS_ND
        ) as Float / 100

        innerCircleRadius = getStateFromFlow(
            flow = viewModel.innerCircleRadiusND,
            defaultValue = DEFAULT_INNER_CIRCLE_RADIUS_ND
        ) as Float / 10
    }

    enum class DigitStyle(val id: Int, val titleId: Int) {
        NONE(0, R.string.digit_style_none), ARABIC(1, R.string.digit_style_arabic), ROMAN(2, R.string.digit_style_roman);

        companion object {
            fun toMap(context: Context): Map<String, String> {
                return values().associate {
                    it.id.toString() to context.getString(it.titleId)
                }
            }

            fun getDefault(): DigitStyle {
                return ARABIC
            }

            fun getDefaultId(): String {
                return getDefault().id.toString()
            }

            fun getById(id: String): DigitStyle {
                val item = values().filter {
                    it.id.toString() == id
                }
                return item[0]
            }
        }
    }

    enum class Font(val font: String, val titleId: Int) {
        ROBOTO_REGULAR("roboto_regular.ttf", R.string.digit_font_roboto_regular),
        ROBOTO_LIGHT("roboto_light.ttf", R.string.digit_font_roboto_light),
        ROBOTO_THIN("roboto_thin.ttf", R.string.digit_font_roboto_thin),
        SEVEN_SEGMENT_DIGITAL("7_segment_digital.ttf", R.string.digit_font_seven_segment_digital),
        DSEG14_CLASSIC("dseg14classic.ttf", R.string.digit_font_dseg14_classic);

        companion object {
            fun toMap(context: Context): Map<String, String> {
                return values().associate {
                    it.font to context.getString(it.titleId)
                }
            }

            fun getDefault(): String {
                return ROBOTO_REGULAR.font
            }

            fun getById(id: String): Font {
                val item = values().filter {
                    it.font == id
                }
                return item[0]
            }
        }
    }

    enum class TickStyle(val style: String, val titleId: Int) {
        NONE("none", R.string.tick_style_none),
        DASH("dash", R.string.tick_style_dash),
        CIRCLE("circle", R.string.tick_style_circle);

        companion object {
            fun toMap(context: Context): Map<String, String> {
                return values().associate {
                    it.style to context.getString(it.titleId)
                }
            }

            fun getDefault(): TickStyle {
                return DASH
            }

            fun getDefaultStyle(): String {
                return getDefault().style
            }

            fun getById(id: String): TickStyle {
                val item = values().filter {
                    it.style == id
                }
                return item[0]
            }
        }
    }

    enum class HandStyle(val style: String, val titleId: Int) {
        TRIANGLE("triangle", R.string.hand_style_triangle),
        BAR("bar", R.string.hand_style_bar);

        companion object {
            fun toMap(context: Context): Map<String, String> {
                return values().associate {
                    it.style to context.getString(it.titleId)
                }
            }

            fun getDefault(): HandStyle {
                return TRIANGLE
            }

            fun getDefaultStyle(): String {
                return getDefault().style
            }

            fun getById(id: String): HandStyle {
                val item = values().filter {
                    it.style == id
                }
                return item[0]
            }
        }
    }

    companion object {
        const val DEFAULT_DIGIT_POSITION_ND = 0.85F
        const val DEFAULT_DIGIT_EMP_QUARTERS_ND = true
        const val DEFAULT_DIGIT_FONT_SIZE_ND = 8F
        val DEFAULT_HAND_STYLE_ND = HandStyle.getDefaultStyle()
        const val DEFAULT_HAND_LEN_MIN_ND = 0.9F
        const val DEFAULT_HAND_LEN_HOURS_ND = 0.7F
        const val DEFAULT_HAND_WIDTH_MIN_ND = 4F
        const val DEFAULT_HAND_WIDTH_HOURS_ND = 4F
        const val DEFAULT_SHOW_SECOND_HAND_ND = true
        val DEFAULT_TICK_STYLE_MINUTES_ND = TickStyle.getDefaultStyle()
        val DEFAULT_TICK_STYLE_HOURS_ND = TickStyle.CIRCLE.style
        const val DEFAULT_TICK_START_MINUTES_ND = 0.9F
        const val DEFAULT_TICK_LEN_MINUTES_ND = 4F
        const val DEFAULT_TICK_START_HOURS_ND = 0.9F
        const val DEFAULT_TICK_LEN_HOURS_ND = 4F
        const val DEFAULT_INNER_CIRCLE_RADIUS_ND = 0.45F
    }
}