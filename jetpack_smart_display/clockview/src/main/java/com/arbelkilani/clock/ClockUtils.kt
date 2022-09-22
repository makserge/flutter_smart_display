package com.arbelkilani.clock

import java.util.*

object ClockUtils {
    private val romanMap = TreeMap<Int, String>()

    init {
        romanMap[12] = "XII"
        romanMap[11] = "XI"
        romanMap[10] = "X"
        romanMap[9] = "IX"
        romanMap[8] = "VIII"
        romanMap[7] = "VII"
        romanMap[6] = "VI"
        romanMap[5] = "V"
        romanMap[4] = "IV"
        romanMap[3] = "III"
        romanMap[2] = "II"
        romanMap[1] = "I"
    }

    fun toRoman(number: Int?): String? {
        val l = romanMap.floorKey(number)
        return if (number == l) {
            romanMap[number]
        } else romanMap[l as Int] + toRoman(number!! - l)
    }
}