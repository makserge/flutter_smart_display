package com.escapeindustries.dotmatrix

/**
 * A [Glyph] representing a colon.
 * @author Mark Roberts
 */
internal class Separator(grid: Grid, column: Int, row: Int) : Glyph() {
    init {
        width = 1
        height = 13
        leftMostColumn = column
        topRow = row
        this.grid = grid
    }

    override fun draw() {
        changeDot(5, true)
        changeDot(9, true)
    }
}