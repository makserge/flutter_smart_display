package com.smsoft.smartdisplay.ui.composable.clock.digitalmatrixclock

import kotlin.math.max
import kotlin.math.min

/**
 * A [Grid] implementation that only stores the state of all the dots.
 * Drawing to this only changes the stored state.
 *
 * @author Mark Roberts
 */
internal class Grid {
    var columns = 0
    var rows = 0
    val paddingColumnsLeft = 0
    val paddingColumnsRight = 0
    val paddingRowsTop = 0
    val paddingRowsBottom = 0
    private var paddingTop = 0
    private var paddingLeft = 0
    private var paddingBottom = 0
    private var paddingRight = 0

    private lateinit var grid: Array<IntArray>
    private lateinit var digits: Array<Digit>
    private var separatorColumns = BooleanArray(0)

    /** True for the columns of the ":" separators (they blink). */
    fun isSeparatorColumn(x: Int): Boolean = separatorColumns[x]

    fun changeDot(
        x: Int,
        y: Int,
        on: Boolean
    ) {
        val current = grid[x][y]
        if (on) {
            if (!isLitState(current)) {
                grid[x][y] = 2
            }
        } else {
            if (isLitState(current)) {
                grid[x][y] = 1
            }
        }
    }

    /**
     * Whether the dot is lit. Dot states: 2 and 3 lit; 0 (never lit) and 1 (switched off) unlit.
     * The drawing and the centring both use this, so they always agree on the lit dots.
     */
    fun isLit(x: Int, y: Int): Boolean = isLitState(grid[x][y])

    private fun isLitState(state: Int) = (state == 2) || (state == 3)

    /**
     * The columns from the left-most to the right-most lit dot, or null when no dot is lit.
     * Separator dots count as lit even while they blink off, so the blink never moves anything.
     */
    fun litColumns(): IntRange? {
        var first = -1
        var last = -1
        for (x in 0 until columns) {
            if (grid[x].any { isLitState(it) }) {
                if (first < 0) {
                    first = x
                }
                last = x
            }
        }
        return if (first < 0) null else first..last
    }

    fun setFormat(format: String) {
        // The format drives the size of the grid in dots. This method
        // parses the format into Glyphs, filters out the digits to
        // simplify updating them, derives the number of rows and
        // columns of dots, and gets the initial value drawn into the grid.
        val glyphs = parseFormat(format)
        digits = extractDigits(glyphs)
        var gridHeight = 0
        var column = paddingColumnsLeft
        val separatorAt = ArrayList<Int>(2)
        for (glyph in glyphs) {
            glyph.setColumn(column)
            glyph.setRow(paddingRowsTop)
            if (glyph is Separator) {
                separatorAt += column
            }
            column += glyph.width
            gridHeight = max(gridHeight, glyph.height)
        }
        columns = column + paddingRight
        rows = paddingTop + gridHeight + paddingBottom
        grid = Array(columns) { IntArray(rows) }
        separatorColumns = BooleanArray(columns).also { flags ->
            separatorAt.forEach { flags[it] = true }
        }
        for (glyph in glyphs) {
            glyph.draw()
        }
    }

    /**
     * Shows [values] in the digits from left to right; [BLANK_DIGIT] leaves a digit dark. Every
     * digit is set, so a digit that is not needed any more (e.g. the tens of the hour at 0:00)
     * goes dark instead of keeping its last number.
     */
    fun setDigits(vararg values: Int) {
        values.forEachIndexed { i, value ->
            if (i < digits.size) {
                digits[i].number = value
            }
        }
    }

    fun setValue(value: String) {
        // The number of digits in the input must be compared
        // to the number of digits available on the grid so
        // that an offset can be found, if necessary,
        // to make the least-significant digit of input to
        // render in the left-most digit of the grid, and
        // to make sure that the least significant digits
        // are displayed if there are not enough digits
        // on the grid to display the whole input.
        val values = parseDigit(value)
        var digitsOffset = digits.size - values.size
        var valuesOffset = 0
        if (digitsOffset < 0) {
            valuesOffset = digitsOffset * -1
            digitsOffset = 0
        }
        val limit = min(digits.size, values.size)
        for (i in 0 until limit) {
            digits[i + digitsOffset].number = values[i + valuesOffset]
        }
    }

    fun setPaddingDots(
        top: Int,
        left: Int,
        bottom: Int,
        right: Int
    ) {
        paddingTop = top
        paddingLeft = left
        paddingBottom = bottom
        paddingRight = right
    }

    private fun extractDigits(
        glyphs: Array<Glyph>
    ): Array<Digit> {
        val digits: MutableList<Digit> = ArrayList()
        for (glyph in glyphs) {
            if (glyph is Digit) {
                digits.add(glyph)
            }
        }
        return digits.toTypedArray()
    }

    /**
     * Extract all the digits from input.
     *
     * @param input
     * The string from which to extract digits
     * @return An array containing all the digits in the order in which they
     * were found
     */
    private fun parseDigit(input: String): IntArray {
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

    /**
     * Parse a format string into a set of [Glyph]s
     *
     * @param format
     * The format to be parsed. The format can be made up of '0' (a
     * digit), ':" (a colon) and ' ' (space - a gap between other
     * [Glyph]s)
     * @return All the [Glyph]s that were found, working from left to
     * right.
     */
    private fun parseFormat(format: String): Array<Glyph> {
        val glyphs: MutableList<Glyph> = ArrayList()
        for (element in format) {
            when (element) {
                '0' -> glyphs.add(Digit(this, 0, 0))
                ':' -> glyphs.add(Separator(this, 0, 0))
                ' ' -> glyphs.add(Space())
            }
        }
        return glyphs.toTypedArray()
    }
}
/** Digit value that switches all dots of a digit off. */
internal const val BLANK_DIGIT = 10
