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

    // Rows 4 and 8: symmetric around the digits' middle bar (row 6). Rows 5 and 9 sat one row
    // above and three rows below it, which stood out once the dots blink.
    override fun draw() {
        changeDot(
            index = 4,
            on = true
        )
        changeDot(
            index = 8,
            on = true
        )
    }
}