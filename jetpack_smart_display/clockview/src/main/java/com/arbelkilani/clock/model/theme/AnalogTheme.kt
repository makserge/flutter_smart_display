package com.arbelkilani.clock.model.theme

import com.arbelkilani.clock.enumeration.ClockType
import com.arbelkilani.clock.enumeration.analog.*

class AnalogTheme private constructor(analogicalThemeBuilder: AnalogicalThemeBuilder) {
    val clockType: ClockType
    val clockBackground: Int
    val isShowCenter: Boolean
    val centerInnerColor: Int
    val centerOuterColor: Int
    val isShowBorder: Boolean
    val borderColor: Int
    val isShowSecondsNeedle: Boolean
    val needleHoursColor: Int
    val needleMinutesColor: Int
    val needleSecondsColor: Int
    val minutesProgressColor: Int
    val isShowDegrees: Boolean
    val degreesColor: Int
    val degreesType: DegreeType
    val degreesStep: DegreesStep
    val valuesFont: Int
    val valuesColor: Int
    val isShowHoursValues: Boolean
    val isShowMinutesValues: Boolean
    val minutesValuesFactor: Float
    val valueStep: ValueStep
    val valueType: ValueType
    val valueDisposition: ValueDisposition

    init {
        clockType = analogicalThemeBuilder.clockType
        clockBackground = analogicalThemeBuilder.clockBackground
        isShowCenter = analogicalThemeBuilder.showCenter
        centerInnerColor = analogicalThemeBuilder.centerInnerColor
        centerOuterColor = analogicalThemeBuilder.centerOuterColor
        isShowBorder = analogicalThemeBuilder.showBorder
        borderColor = analogicalThemeBuilder.borderColor
        isShowSecondsNeedle = analogicalThemeBuilder.showSecondsNeedle
        needleHoursColor = analogicalThemeBuilder.needleHoursColor
        needleMinutesColor = analogicalThemeBuilder.needleMinutesColor
        needleSecondsColor = analogicalThemeBuilder.needleSecondsColor
        minutesProgressColor = analogicalThemeBuilder.minutesProgressColor
        isShowDegrees = analogicalThemeBuilder.showDegrees
        degreesColor = analogicalThemeBuilder.degreesColor
        degreesType = analogicalThemeBuilder.degreesType!!
        degreesStep = analogicalThemeBuilder.degreesStep!!
        valuesFont = analogicalThemeBuilder.valuesFont
        valuesColor = analogicalThemeBuilder.valuesColor
        isShowHoursValues = analogicalThemeBuilder.showHoursValues
        isShowMinutesValues = analogicalThemeBuilder.showMinutesValues
        minutesValuesFactor = analogicalThemeBuilder.minutesValuesFactor
        valueStep = analogicalThemeBuilder.valueStep!!
        valueType = analogicalThemeBuilder.valueType!!
        valueDisposition = analogicalThemeBuilder.valueDisposition!!
    }

    class AnalogicalThemeBuilder {
        internal lateinit var clockType: ClockType
        internal var clockBackground = 0
        internal var showCenter = false
        internal var centerInnerColor = 0
        internal var centerOuterColor = 0
        internal var showBorder = false
        internal var borderColor = 0
        internal var showSecondsNeedle = false
        internal var needleHoursColor = 0
        internal var needleMinutesColor = 0
        internal var needleSecondsColor = 0
        internal var minutesProgressColor = 0
        internal var showDegrees = false
        internal var degreesColor = 0
        internal var degreesType: DegreeType? = null
        internal var degreesStep: DegreesStep? = null
        internal var valuesFont = 0
        internal var valuesColor = 0
        internal var showHoursValues = false
        internal var showMinutesValues = false
        internal var minutesValuesFactor = 0f
        internal var valueStep: ValueStep? = null
        internal var valueType: ValueType? = null
        internal var valueDisposition: ValueDisposition? = null

        fun setClockType(clockType: ClockType): AnalogicalThemeBuilder {
            this.clockType = clockType
            return this
        }

        fun setClockBackground(clockBackground: Int): AnalogicalThemeBuilder {
            this.clockBackground = clockBackground
            return this
        }

        fun setShowCenter(showCenter: Boolean): AnalogicalThemeBuilder {
            this.showCenter = showCenter
            return this
        }

        fun setCenterInnerColor(centerInnerColor: Int): AnalogicalThemeBuilder {
            this.centerInnerColor = centerInnerColor
            return this
        }

        fun setCenterOuterColor(centerOuterColor: Int): AnalogicalThemeBuilder {
            this.centerOuterColor = centerOuterColor
            return this
        }

        fun setShowBorder(showBorder: Boolean): AnalogicalThemeBuilder {
            this.showBorder = showBorder
            return this
        }

        fun setBorderColor(borderColor: Int): AnalogicalThemeBuilder {
            this.borderColor = borderColor
            return this
        }

        fun setShowSecondsNeedle(showSecondsNeedle: Boolean): AnalogicalThemeBuilder {
            this.showSecondsNeedle = showSecondsNeedle
            return this
        }

        fun setNeedleHoursColor(needleHoursColor: Int): AnalogicalThemeBuilder {
            this.needleHoursColor = needleHoursColor
            return this
        }

        fun setNeedleMinutesColor(needleMinutesColor: Int): AnalogicalThemeBuilder {
            this.needleMinutesColor = needleMinutesColor
            return this
        }

        fun setNeedleSecondsColor(needleSecondsColor: Int): AnalogicalThemeBuilder {
            this.needleSecondsColor = needleSecondsColor
            return this
        }

        fun setMinutesProgressColor(minutesProgressColor: Int): AnalogicalThemeBuilder {
            this.minutesProgressColor = minutesProgressColor
            return this
        }

        fun setShowDegrees(showDegrees: Boolean): AnalogicalThemeBuilder {
            this.showDegrees = showDegrees
            return this
        }

        fun setClockDegreesType(degreesType: DegreeType?): AnalogicalThemeBuilder {
            this.degreesType = degreesType
            return this
        }

        fun setDegreesStep(degreesStep: DegreesStep?): AnalogicalThemeBuilder {
            this.degreesStep = degreesStep
            return this
        }

        fun setDegreesColor(degreesColor: Int): AnalogicalThemeBuilder {
            this.degreesColor = degreesColor
            return this
        }

        fun setDegreesType(degreesType: DegreeType?): AnalogicalThemeBuilder {
            this.degreesType = degreesType
            return this
        }

        fun setValuesFont(valuesFont: Int): AnalogicalThemeBuilder {
            this.valuesFont = valuesFont
            return this
        }

        fun setValuesColor(valuesColor: Int): AnalogicalThemeBuilder {
            this.valuesColor = valuesColor
            return this
        }

        fun setShowHoursValues(showHoursValues: Boolean): AnalogicalThemeBuilder {
            this.showHoursValues = showHoursValues
            return this
        }

        fun setShowMinutesValues(showMinutesValues: Boolean): AnalogicalThemeBuilder {
            this.showMinutesValues = showMinutesValues
            return this
        }

        fun setMinutesValuesFactor(minutesValuesFactor: Float): AnalogicalThemeBuilder {
            this.minutesValuesFactor = minutesValuesFactor
            return this
        }

        fun setValueStep(valueStep: ValueStep?): AnalogicalThemeBuilder {
            this.valueStep = valueStep
            return this
        }

        fun setValueType(valueType: ValueType?): AnalogicalThemeBuilder {
            this.valueType = valueType
            return this
        }

        fun setValueDisposition(valueDisposition: ValueDisposition?): AnalogicalThemeBuilder {
            this.valueDisposition = valueDisposition
            return this
        }

        fun build(): AnalogTheme {
            return AnalogTheme(this)
        }
    }
}