package com.firebirdberlin.nightdream

import android.content.Context

class AnalogClockConfig(private var context: Context, private var style: Style) {
    var digitPosition = 0.85f
    var digitStyle = DigitStyle.ARABIC
    var emphasizeHour12 = true
    var showSecondHand = true
    var handShape = HandShape.TRIANGLE
    var handLengthHours = 0.8F
    var handLengthMinutes = 0.95F
    var handWidthHours = 0.04F
    var handWidthMinutes = 0.04F
    var highlightQuarterOfHour = true
    var innerCircleRadius = 0.045F
    var tickStartMinutes = 0.95F
    var tickStyleMinutes = TickStyle.DASH
    var tickLengthMinutes = 0.04F
    var tickStartHours = 0.95F
    var tickWidthHours = 0.01F
    var tickWidthMinutes = 0.01F
    var tickStyleHours = TickStyle.CIRCLE
    var tickLengthHours = 0.04F
    var outerCircleRadius = 1F
    var outerCircleWidth = 0F
    var fontSize = 0.08F
    var fontUri = "file:///android_asset/fonts/dancingscript_regular.ttf"

    init {
        if (!storedPreferencesExists()) {
            reset()
        } else {
            load()
        }
    }

    /*
    public static Style toClockStyle(int layoutId) {
        switch (layoutId) {
            case ClockLayout.LAYOUT_ID_ANALOG:
                return Style.MINIMALISTIC;
            case ClockLayout.LAYOUT_ID_ANALOG2:
                return Style.SIMPLE;
            case ClockLayout.LAYOUT_ID_ANALOG3:
                return Style.ARC;
            case ClockLayout.LAYOUT_ID_ANALOG4:
                return Style.DEFAULT;
            default:
                return Style.MINIMALISTIC;
        }
    }
*/
    private fun reset() {
        initStyle(style)
        save()
    }

    private fun storedPreferencesExists(): Boolean {
        return context.getSharedPreferences(style.name, 0).contains("digitStyle")
    }

    private fun load() {
        val settings = context.getSharedPreferences(style.name, 0)
        val digitStyleString = settings.getString("digitStyle", DigitStyle.NONE.name)
        digitStyle = DigitStyle.valueOf(digitStyleString!!)
        digitPosition = settings.getFloat("digitPosition", 0.85F)
        emphasizeHour12 = settings.getBoolean("emphasizeHour12", true)
        showSecondHand = settings.getBoolean("showSecondHand", true)
        val handShapeString = settings.getString("handShape", HandShape.TRIANGLE.name)
        handShape = HandShape.valueOf(handShapeString!!)
        handLengthHours = settings.getFloat("handLengthHours", 0.8F)
        handLengthMinutes = settings.getFloat("handLengthMinutes", 0.95F)
        handWidthHours = settings.getFloat("handWidthHours", 0.04F)
        handWidthMinutes = settings.getFloat("handWidthMinutes", 0.04F)
        highlightQuarterOfHour = settings.getBoolean("highlightQuarterOfHour", true)
        innerCircleRadius = settings.getFloat("innerCircleRadius", 0.045F)
        tickLengthMinutes = settings.getFloat("tickLengthMinutes", 0.04F)
        tickLengthHours = settings.getFloat("tickLengthHours", 0.04F)
        tickStartMinutes = settings.getFloat("tickStartMinutes", 0.95F)
        tickStartHours = settings.getFloat("tickStartHours", 0.95F)
        val tickStyleMinutesString = settings.getString("tickStyleMinutes", TickStyle.DASH.name)
        tickStyleMinutes = TickStyle.valueOf(tickStyleMinutesString!!)
        val tickStyleHoursString = settings.getString("tickStyleHours", TickStyle.CIRCLE.name)
        tickStyleHours = TickStyle.valueOf(tickStyleHoursString!!)
        tickWidthHours = settings.getFloat("tickWidthHours", 0.01F)
        tickWidthMinutes = settings.getFloat("tickWidthMinutes", 0.01F)
        outerCircleRadius = settings.getFloat("outerCircleRadius", 1F)
        outerCircleWidth = settings.getFloat("outerCircleWidth", 0F)
        fontSize = settings.getFloat("fontSize", 0.08F)
        fontUri = settings.getString("fontUri", "file:///android_asset/fonts/dancingscript_regular.ttf")!!
    }

    private fun save() {
        val settings = context.getSharedPreferences(style.name, 0)
        settings.edit().apply {
            putBoolean("emphasizeHour12", emphasizeHour12)
            putBoolean("highlightQuarterOfHour", highlightQuarterOfHour)
            putBoolean("showSecondHand", showSecondHand)
            putFloat("digitPosition", digitPosition)
            putFloat("fontSize", fontSize)
            putFloat("handLengthHours", handLengthHours)
            putFloat("handLengthMinutes", handLengthMinutes)
            putFloat("handWidthHours", handWidthHours)
            putFloat("handWidthMinutes", handWidthMinutes)
            putFloat("innerCircleRadius", innerCircleRadius)
            putFloat("outerCircleRadius", outerCircleRadius)
            putFloat("outerCircleWidth", outerCircleWidth)
            putFloat("tickLengthHours", tickLengthHours)
            putFloat("tickLengthMinutes", tickLengthMinutes)
            putFloat("tickStartHours", tickStartHours)
            putFloat("tickStartMinutes", tickStartMinutes)
            putFloat("tickWidthHours", tickWidthHours)
            putFloat("tickWidthMinutes", tickWidthMinutes)
            putString("digitStyle", digitStyle.name)
            putString("fontUri", fontUri)
            putString("handShape", handShape.name)
            putString("tickStyleHours", tickStyleHours.name)
            putString("tickStyleMinutes", tickStyleMinutes.name)
            apply()
        }
    }

    private fun initStyle(style: Style?) {
        when (style) {
            Style.DEFAULT -> {
                digitPosition = 0.85F
                digitStyle = DigitStyle.ARABIC
                emphasizeHour12 = true
                fontSize = 0.08F
                handLengthHours = 0.8F
                handLengthMinutes = 0.95F
                handShape = HandShape.TRIANGLE
                handWidthHours = 0.04F
                handWidthMinutes = 0.04F
                highlightQuarterOfHour = true
                innerCircleRadius = 0.045F
                outerCircleRadius = 1F
                outerCircleWidth = 0F
                showSecondHand = true
                tickLengthHours = 0.04F
                tickLengthMinutes = 0.04F
                tickStartHours = 0.95F
                tickStartMinutes = 0.95F
                tickStyleHours = TickStyle.CIRCLE
                tickStyleMinutes = TickStyle.DASH
                tickWidthHours = 0.01F
                tickWidthMinutes = 0.01F
            }
            Style.SIMPLE -> {
                digitPosition = 0.85F
                digitStyle = DigitStyle.NONE
                emphasizeHour12 = true
                fontSize = 0.08F
                handLengthHours = 0.6F
                handLengthMinutes = 0.9F
                handShape = HandShape.TRIANGLE
                handWidthHours = 0.04F
                handWidthMinutes = 0.04F
                highlightQuarterOfHour = false
                innerCircleRadius = 0.045F
                outerCircleRadius = 1F
                outerCircleWidth = 0F
                showSecondHand = true
                tickLengthHours = 0.06F
                tickLengthMinutes = 0.06F
                tickStartHours = 0.87F
                tickStartMinutes = 0.87F
                tickStyleHours = TickStyle.DASH
                tickStyleMinutes = TickStyle.NONE
                tickWidthHours = 0.01F
                tickWidthMinutes = 0.01F
            }
            Style.ARC -> {
                digitPosition = 0.85F
                digitStyle = DigitStyle.NONE
                emphasizeHour12 = true
                fontSize = 0.08F
                handLengthHours = 0.80F
                handLengthMinutes = 0.90F
                handShape = HandShape.ARC
                handWidthHours = 0.06F
                handWidthMinutes = 0.06F
                highlightQuarterOfHour = false
                innerCircleRadius = 0.045F
                outerCircleRadius = 1F
                outerCircleWidth = 0F
                showSecondHand = true
                tickLengthHours = 0.06F
                tickLengthMinutes = 0.06F
                tickStartHours = 0.87F
                tickStartMinutes = 0.87F
                tickStyleHours = TickStyle.DASH
                tickStyleMinutes = TickStyle.NONE
                tickWidthHours = 0.01F
                tickWidthMinutes = 0.01F
            }
            Style.MINIMALISTIC -> {
                digitPosition = 0.7F
                digitStyle = DigitStyle.NONE
                emphasizeHour12 = true
                fontSize = 0.08F
                handLengthHours = 0.6F
                handLengthMinutes = 0.8F
                handShape = HandShape.BAR
                handWidthHours = 0.02F
                handWidthMinutes = 0.02F
                highlightQuarterOfHour = false
                innerCircleRadius = 0.0F
                outerCircleRadius = 1F
                outerCircleWidth = 0.01F
                showSecondHand = true
                tickLengthHours = 0.1F
                tickLengthMinutes = 0.06F
                tickStartHours = 0.84F
                tickStartMinutes = 0.87F
                tickStyleHours = TickStyle.DASH
                tickStyleMinutes = TickStyle.NONE
                tickWidthHours = 0.025F
                tickWidthMinutes = 0.025F
            }
            null -> TODO()
        }
    }

    enum class Style {
        DEFAULT, SIMPLE, ARC, MINIMALISTIC
    }

    enum class DigitStyle(val value: Int) {
        NONE(0), ARABIC(1), ROMAN(2);
    }

    enum class TickStyle(val value: Int) {
        NONE(0), DASH(1), CIRCLE(2);
    }

    enum class HandShape(val value: Int) {
        BAR(0), TRIANGLE(1), ARC(2);
    }
}