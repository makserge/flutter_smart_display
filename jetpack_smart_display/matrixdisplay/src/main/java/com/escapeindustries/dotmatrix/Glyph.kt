package com.escapeindustries.dotmatrix

/**
 * Represents a thing that can be drawn on a [Grid]. To implement a Glyph,
 * sub-class it and provide an implementation for [draw][.draw].
 *
 * @author Mark Roberts
 */
internal abstract class Glyph {
    /**
     * Get the width of this Glyph in dots.
     *
     * @return The width in dots needed to draw this Glyph.
     */
    var width = 0
        protected set

    /**
     * Get the height of this Glyph in dots.
     *
     * @return The height in dots needed to draw this Glyph
     */
    var height = 0
        protected set
    protected var leftMostColumn = 0
    protected var topRow = 0
    protected lateinit var grid: Grid

    /**
     * Set the left-most column of the target [Grid] that this Glyph will
     * draw at.
     *
     * @param column
     * The left-most column at which to start drawing this Glyph in
     * the target [Grid].
     * @see .setGrid
     */
    fun setColumn(column: Int) {
        leftMostColumn = column
    }

    /**
     * Set the top-most column of the target [Grid] that this Glyph will
     * draw at.
     *
     * @param row
     * The top-most row at which to start drawing this Glyph in the
     * target [Grid].
     * @see .setGrid
     */
    fun setRow(row: Int) {
        topRow = row
    }

    /**
     * Convenience method for setting a dot on or off.
     *
     * @param index
     * One-dimensional zero-based index to the dot. For example, if
     * the dot was the the right-most of the second line of a Glyph
     * with a width of 5, index would be 9.
     * @param on
     * The new state of the dot.
     */
    protected fun changeDot(index: Int, on: Boolean) {
        // index is relative to topRow and leftMostColumn.
        val x = leftMostColumn + index % width
        val y = index / width + topRow
        grid.changeDot(x, y, on)
    }

    /**
     * Override this to add code to draw a Glyph into the target [Grid].
     * Make calls to [.changeDot] to set individual dots on or off.
     */
    abstract fun draw()
}