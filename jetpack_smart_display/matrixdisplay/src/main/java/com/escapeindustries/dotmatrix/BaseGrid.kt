package com.escapeindustries.dotmatrix

import kotlin.math.max
import kotlin.math.min

/**
 * A helper for classes that implement [Grid]. Default implementations are
 * provided for most interface methods. It remains for the sub-class to provide
 * an implementation of [.changeDot] and [.initializeGrid].
 *
 * @author Mark Roberts
 */
internal abstract class BaseGrid : Grid {
    override var columns = 0
    override var rows = 0
    private var paddingTop = 0
    private var paddingLeft = 0
    private var paddingBottom = 0
    private var paddingRight = 0
    private lateinit var digits: Array<Digit>

    override fun setPaddingDots(top: Int, left: Int, bottom: Int, right: Int) {
        paddingTop = top
        paddingLeft = left
        paddingBottom = bottom
        paddingRight = right
    }

    override fun setFormat(format: String?) {
        // The format drives the size of the grid in dots. This method
        // parses the format into Glyphs, filters out the digits to
        // simplify updating them, derives the number of rows and
        // columns of dots, and gets the initial value drawn into the grid.
        val factory = GlyphFactory(this)
        val parser = FormatStringParser(factory)
        val glyphs = parser.parse(format!!)
        digits = extractDigits(glyphs)
        var gridHeight = 0
        var column = paddingColumnsLeft
        for (glyph in glyphs) {
            glyph.setColumn(column)
            glyph.setRow(paddingRowsTop)
            column += glyph.width
            gridHeight = max(gridHeight, glyph.height)
        }
        columns = column + paddingRight
        rows = paddingTop + gridHeight + paddingBottom
        initializeGrid()
        for (glyph in glyphs) {
            glyph.draw()
        }
    }

    override fun setValue(value: String) {
        // The number of digits in the input must be compared
        // to the number of digits available on the grid so
        // that an offset can be found, if necessary,
        // to make the least-significant digit of input to
        // render in the left-most digit of the grid, and
        // to make sure that the least significant digits
        // are displayed if there are not enough digits
        // on the grid to display the whole input.
        val parser = DigitsParser()
        val values = parser.parse(value)
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

    abstract override fun changeDot(x: Int, y: Int, on: Boolean)

    /**
     * Called by [.setFormat] this is a chance to set up data structures
     * etc. needed by the grid.
     */
    abstract fun initializeGrid()
    private fun extractDigits(glyphs: Array<Glyph>): Array<Digit> {
        val digits: MutableList<Digit> = ArrayList()
        for (glyph in glyphs) {
            if (glyph is Digit) {
                digits.add(glyph)
            }
        }
        return digits.toTypedArray()
    }
}