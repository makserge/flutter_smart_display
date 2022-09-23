package com.escapeindustries.dotmatrix

/**
 * A utility class to help extract just the digits from values.
 *
 * @author Mark Roberts
 */
internal class DigitsParser {
    /**
     * Extract all the digits from input.
     *
     * @param input
     * The string from which to extract digits
     * @return An array containing all the digits in the order in which they
     * were found
     */
    fun parse(input: String): IntArray {
        // Parse out only the digits in input
        val temp: MutableList<Int> = ArrayList()
        for (element in input) {
            val code = element.code
            if (code in (48..57)) {
                temp.add(code - 48)
            }
        }
        val results = IntArray(temp.size)
        for (i in results.indices) {
            results[i] = temp[i]
        }
        return results
    }
}