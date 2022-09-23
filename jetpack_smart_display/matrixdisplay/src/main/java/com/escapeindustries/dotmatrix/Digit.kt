package com.escapeindustries.dotmatrix

/**
 * A [Glyph] that represents a digit (0-9).
 *
 * @author Mark Roberts
 */
internal class Digit(grid: Grid, column: Int, row: Int) : Glyph() {
    private var current = 10 // 10 represents a blank digit

    init {
        width = 7
        height = 13
        leftMostColumn = column
        topRow = row
        this.grid = grid
    }
    /**
     * Get the value of this digit.
     *
     * @return The current value
     */
    /**
     * Set the value of this digit.
     *
     * @param to
     * The new value
     */
    var number: Int
        get() = current
        set(to) {
            if (to != current) {
                val trans = GlyphTransition(NormalDotChangeAction(this))
                trans.makeTransition(
                    DigitDefinition.patterns[current],
                    DigitDefinition.patterns[to]
                )
                current = to
            }
        }

    override fun draw() {
        number = current
    }
}