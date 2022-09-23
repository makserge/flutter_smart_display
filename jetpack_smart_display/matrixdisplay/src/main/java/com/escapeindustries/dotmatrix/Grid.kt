package com.escapeindustries.dotmatrix

/**
 * A Grid that can display [Glyphs][Glyph].
 *
 * @author Mark Roberts
 */
internal interface Grid {
    /**
     * Get the width of the Grid in columns of dots.
     *
     * @return The width of the grid in columns of dots
     */
    /**
     * Set the width of the Grid in columns of dots.
     *
     * @param columns
     * The new width of the grid in columns of dots
     */
    var columns: Int
    /**
     * Get the height of the Grid in rows of dots.
     *
     * @return The new height of the grid in rows of dots
     */
    /**
     * Set the height of the Grid in rows of dots.
     *
     * @param rows
     * The new height of the grid in rows of dots
     */
    var rows: Int

    /**
     * Get the number of rows of dots displayed at the top of the grid which are
     * not involved in the display of [Glyphs][Glyph].
     *
     * @return The number of rows of dots displayed at the top of the grid which
     * are not involved in the display of [Glyphs][Glyph]
     */
    val paddingRowsTop: Int

    /**
     * Get the number of columns of dots displayed to the left of the grid which
     * are not involved in the display of [Glyphs][Glyph].
     *
     * @return The number of columns of dots displayed to the left of the grid
     * which are not involved in the display of [Glyphs][Glyph]
     */
    val paddingColumnsLeft: Int

    /**
     * Get the number of rows of dots displayed at the bottom of the grid which
     * are not involved in the display of [Glyphs][Glyph].
     *
     * @return The number of rows of dots displayed at the bottom of the grid
     * which are not involved in the display of [Glyphs][Glyph]
     */
    val paddingRowsBottom: Int

    /**
     * Get the number of columns of dots displayed to the right of the grid
     * which are not involved in the display of [Glyphs][Glyph].
     *
     * @return The number of columns of dots displayed to the right of the grid
     * which are not involved in the display of [Glyphs][Glyph]
     */
    val paddingColumnsRight: Int

    /**
     * Set the number of rows and columns around the edges of the grid that are
     * not involved in the display of [Glyphs][Glyph].
     *
     * @param top
     * The number of rows at the top
     * @param left
     * The number of columns to the left
     * @param bottom
     * The number of rows at the bottom
     * @param right
     * The number of columns to the right
     */
    fun setPaddingDots(top: Int, left: Int, bottom: Int, right: Int)

    /**
     * Switch on or off the specified dot.
     *
     * @param x
     * The zero-indexed column of the dot, beginning from the left
     * @param y
     * The zero-indexed row of the dot, beginning from the top
     * @param on
     * The new state - true for on, false for off
     */
    fun changeDot(x: Int, y: Int, on: Boolean)

    /**
     * Set the format to be expected for values that will be displayed by the
     * Grid. It is expected that calling this will have the side-effect that the
     * grid will set its width and height in columns to accommodate the format.
     *
     * @param format
     * A format string. Currently supported characters are 0, :
     * (colon) and ' ' (space) e.g. "0 0 : 0 0" could display the
     * value "12:34" with spacing sufficient that no glyph was
     * directly connected to the next.
     */
    fun setFormat(format: String?)

    /**
     * Set the value that should be displayed on the Grid. This will be parsed
     * according to the current format set for the Grid.
     *
     * @param value
     * The new value for the Grid to display
     */
    fun setValue(value: String)
    /**
     * Get the current state regarding whether the Grid is active or inactive
     *
     * @return the current state - true for active, false for inactive
     */
}