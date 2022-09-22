package com.arbelkilani.clock.model.theme

import com.arbelkilani.clock.enumeration.ClockType
import com.arbelkilani.clock.enumeration.numeric.NumericFormat

class NumericTheme private constructor(numericThemeBuilder: NumericThemeBuilder) {
    val clockType: ClockType
    val clockBackground: Int
    val valuesFont: Int
    val valuesColor: Int
    val isShowBorder: Boolean
    val borderColor: Int
    val borderRadiusRx: Int
    val borderRadiusRy: Int
    val numericFormat: NumericFormat?

    init {
        clockType = numericThemeBuilder.clockType
        clockBackground = numericThemeBuilder.clockBackground
        valuesFont = numericThemeBuilder.valuesFont
        valuesColor = numericThemeBuilder.valuesColor
        isShowBorder = numericThemeBuilder.showBorder
        borderColor = numericThemeBuilder.borderColor
        borderRadiusRx = numericThemeBuilder.borderRadiusRx
        borderRadiusRy = numericThemeBuilder.borderRadiusRy
        numericFormat = numericThemeBuilder.numericFormat
    }

    class NumericThemeBuilder {
        internal lateinit var clockType: ClockType
        internal var clockBackground = 0
        internal var valuesFont = 0
        internal var valuesColor = 0
        internal var showBorder = false
        internal var borderColor = 0
        internal var borderRadiusRx = 0
        internal var borderRadiusRy = 0
        internal var numericFormat: NumericFormat? = null

        fun setClockType(clockType: ClockType): NumericThemeBuilder {
            this.clockType = clockType
            return this
        }

        fun setClockBackground(clockBackground: Int): NumericThemeBuilder {
            this.clockBackground = clockBackground
            return this
        }

        fun setValuesFont(valuesFont: Int): NumericThemeBuilder {
            this.valuesFont = valuesFont
            return this
        }

        fun setValuesColor(valuesColor: Int): NumericThemeBuilder {
            this.valuesColor = valuesColor
            return this
        }

        fun setShowBorder(showBorder: Boolean): NumericThemeBuilder {
            this.showBorder = showBorder
            return this
        }

        fun setBorderColor(borderColor: Int): NumericThemeBuilder {
            this.borderColor = borderColor
            return this
        }

        fun setBorderRadius(borderRadiusRx: Int, borderRadiusRy: Int): NumericThemeBuilder {
            this.borderRadiusRx = borderRadiusRx
            this.borderRadiusRy = borderRadiusRy
            return this
        }

        fun setNumericFormat(numericFormat: NumericFormat?): NumericThemeBuilder {
            this.numericFormat = numericFormat
            return this
        }

        fun build(): NumericTheme {
            return NumericTheme(this)
        }
    }
}