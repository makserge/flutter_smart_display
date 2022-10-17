package com.smsoft.smartdisplay.ui.composable.clock.digitalmatrixclock

/**
 * A [Glyph] representing a colon.
 * @author Mark Roberts
 */
internal class Separator(
    grid: Grid,
    column: Int,
    row: Int
) : Glyph() {
    init {
        width = 1
        height = 13
        leftMostColumn = column
        topRow = row
        this.grid = grid
    }

    override fun draw() {
        changeDot(
            index = 5,
            on = true
        )
        changeDot(
            index = 9,
            on = true
        )
    }
}