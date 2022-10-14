package com.smsoft.smartdisplay.ui.composable.clock.nightdream

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey
import kotlinx.coroutines.flow.map

data class AnalogClockConfig(
    var digitStyle: DigitStyle = DigitStyle.getDefault(),
    var digitPosition: Float = DEFAULT_DIGIT_POSITION,
    var highlightQuarterOfHour: Boolean = DEFAULT_DIGIT_EMP_QUARTERS,
    var fontUri: String = "file:///android_asset/fonts/" + Font.getDefault(),
    var fontSize: Float = DEFAULT_DIGIT_FONT_SIZE / 100,
    var handStyle: HandStyle = HandStyle.getDefault(),
    var handLengthMinutes: Float = DEFAULT_HAND_LEN_MIN,
    var handLengthHours: Float = DEFAULT_HAND_LEN_HOURS,
    var handWidthMinutes: Float = DEFAULT_HAND_WIDTH_MIN / 100,
    var handWidthHours: Float = DEFAULT_HAND_WIDTH_HOURS / 100,
    var showSecondHand: Boolean = DEFAULT_SHOW_SECOND_HAND,
    var tickStyleMinutes: TickStyle = TickStyle.getDefault(),
    var tickStartMinutes: Float = DEFAULT_TICK_START_MINUTES,
    var tickLengthMinutes: Float = DEFAULT_TICK_LEN_MINUTES / 100,
    var tickStyleHours: TickStyle = TickStyle.CIRCLE,
    var tickStartHours: Float = DEFAULT_TICK_START_HOURS,
    var tickLengthHours: Float = DEFAULT_TICK_LEN_HOURS / 100,
    var emphasizeHour12: Boolean = true,
    var tickWidthHours: Float = 0.01F,
    var tickWidthMinutes: Float = 0.01F,
    var innerCircleRadius: Float = DEFAULT_INNER_CIRCLE_RADIUS / 10
) {
    @Composable
    fun InitDataStore(
        dataStore: DataStore<Preferences>
    ) {
        digitStyle = getParam(
            dataStore = dataStore,
            defaultValue = DigitStyle.getDefault()
        ) { preferences -> DigitStyle.getById(preferences[stringPreferencesKey(PreferenceKey.DIGIT_STYLE.key)] ?: DigitStyle.getDefaultId()) }
        as DigitStyle

        digitPosition = getParam(
            dataStore = dataStore,
            defaultValue = DEFAULT_DIGIT_POSITION
        ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.DIGIT_POSITION.key)] }
        as Float

        highlightQuarterOfHour = getParam(
            dataStore = dataStore,
            defaultValue = DEFAULT_DIGIT_EMP_QUARTERS
        ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.DIGIT_EMP_QUARTERS.key)] }
        as Boolean

        fontUri = "file:///android_asset/fonts/" + getParam(
            dataStore = dataStore,
            defaultValue = Font.getDefault()
        ) { preferences ->
            Font.getById(preferences[stringPreferencesKey(PreferenceKey.DIGIT_FONT.key)] ?: Font.getDefault()).font
        }

        fontSize = getParam(
            dataStore = dataStore,
            defaultValue = DEFAULT_DIGIT_FONT_SIZE
        ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.DIGIT_FONT_SIZE.key)] }
        as Float / 100

        handStyle = getParam(
            dataStore = dataStore,
            defaultValue = HandStyle.getDefault()
        ) { preferences -> HandStyle.getById(preferences[stringPreferencesKey(PreferenceKey.HAND_STYLE.key)] ?: HandStyle.getDefaultStyle()) }
        as HandStyle

        handLengthMinutes = getParam(
            dataStore = dataStore,
            defaultValue = DEFAULT_HAND_LEN_MIN
        ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_LENGTH_MINUTES.key)] }
        as Float

        handLengthHours = getParam(
            dataStore = dataStore,
            defaultValue = DEFAULT_HAND_LEN_HOURS
        ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_LENGTH_HOURS.key)] }
        as Float

        handWidthMinutes = getParam(
            dataStore = dataStore,
            defaultValue = DEFAULT_HAND_WIDTH_MIN
        ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_WIDTH_MINUTES.key)] }
        as Float / 100

        handWidthHours = getParam(
            dataStore = dataStore,
            defaultValue = DEFAULT_HAND_WIDTH_HOURS
        ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.HAND_WIDTH_HOURS.key)] }
        as Float / 100

        showSecondHand = getParam(
            dataStore = dataStore,
            defaultValue = DEFAULT_SHOW_SECOND_HAND
        ) { preferences -> preferences[booleanPreferencesKey(PreferenceKey.HAND_SHOW_SECOND_HAND.key)] }
        as Boolean

        tickStyleMinutes = getParam(
            dataStore = dataStore,
            defaultValue = TickStyle.getDefault()
        ) { preferences -> TickStyle.getById(preferences[stringPreferencesKey(PreferenceKey.TICK_STYLE_MINUTES.key)] ?: TickStyle.getDefaultStyle()) }
        as TickStyle

        tickStartMinutes = getParam(
            dataStore = dataStore,
            defaultValue = DEFAULT_TICK_START_MINUTES
        ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.TICK_START_MINUTES.key)] }
        as Float

        tickLengthMinutes = getParam(
            dataStore = dataStore,
            defaultValue = DEFAULT_TICK_LEN_MINUTES
        ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.TICK_LEN_MINUTES.key)] }
        as Float / 100

        tickStyleHours = getParam(
            dataStore = dataStore,
            defaultValue = TickStyle.CIRCLE
        ) { preferences -> TickStyle.getById(preferences[stringPreferencesKey(PreferenceKey.TICK_STYLE_HOURS.key)] ?: TickStyle.CIRCLE.style) }
        as TickStyle

        tickStartHours = getParam(
            dataStore = dataStore,
            defaultValue = DEFAULT_TICK_START_HOURS
        ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.TICK_START_HOURS.key)] }
        as Float

        tickLengthHours = getParam(
            dataStore = dataStore,
            defaultValue = DEFAULT_TICK_LEN_HOURS
        ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.TICK_LEN_HOURS.key)] }
        as Float / 100

        innerCircleRadius = getParam(
            dataStore = dataStore,
            defaultValue = DEFAULT_INNER_CIRCLE_RADIUS
        ) { preferences -> preferences[floatPreferencesKey(PreferenceKey.INNER_CIRCLE_RADIUS.key)] }
        as Float / 10
    }

    @SuppressLint("FlowOperatorInvokedInComposition")
    @Composable
    private fun getParam(dataStore: DataStore<Preferences>, defaultValue: Any?, getter: (preferences: Preferences) -> Any?): Any? {
        return dataStore.data.map {
            getter(it) ?: defaultValue
        }.collectAsState(initial = defaultValue).value
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
        const val DEFAULT_DIGIT_POSITION = 0.85F
        const val DEFAULT_DIGIT_EMP_QUARTERS = true
        const val DEFAULT_DIGIT_FONT_SIZE = 8F
        val DEFAULT_HAND_STYLE = HandStyle.getDefaultStyle()
        const val DEFAULT_HAND_LEN_MIN = 0.9F
        const val DEFAULT_HAND_LEN_HOURS = 0.7F
        const val DEFAULT_HAND_WIDTH_MIN = 4F
        const val DEFAULT_HAND_WIDTH_HOURS = 4F
        const val DEFAULT_SHOW_SECOND_HAND = true
        val DEFAULT_TICK_STYLE_MINUTES = TickStyle.getDefaultStyle()
        val DEFAULT_TICK_STYLE_HOURS = TickStyle.CIRCLE.style
        const val DEFAULT_TICK_START_MINUTES = 0.9F
        const val DEFAULT_TICK_LEN_MINUTES = 4F
        const val DEFAULT_TICK_START_HOURS = 0.9F
        const val DEFAULT_TICK_LEN_HOURS = 4F
        const val DEFAULT_INNER_CIRCLE_RADIUS = 0.45F
    }
}