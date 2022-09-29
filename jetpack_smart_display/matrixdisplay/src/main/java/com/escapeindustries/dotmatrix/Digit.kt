package com.escapeindustries.dotmatrix

/**
 * A [Glyph] that represents a digit (0-9).
 *
 * @author Mark Roberts
 */
internal class Digit(grid: Grid, column: Int, row: Int) : Glyph() {
    private var current = 10 // 10 represents a blank digit

    private val zero = intArrayOf(
        1, 2, 3, 4, 5, 7, 13, 14, 20, 21, 27, 28, 34,
        35, 41, 49, 55, 56, 62, 63, 69, 70, 76, 77, 83, 85, 86, 87, 88, 89
    )
    private val one = intArrayOf(13, 20, 27, 34, 41, 55, 62, 69, 76, 83)
    private val two = intArrayOf(
        1, 2, 3, 4, 5, 13, 20, 27, 34, 41, 43, 44, 45,
        46, 47, 49, 56, 63, 70, 77, 85, 86, 87, 88, 89
    )
    private val three = intArrayOf(
        1, 2, 3, 4, 5, 13, 20, 27, 34, 41, 43, 44,
        45, 46, 47, 55, 62, 69, 76, 83, 85, 86, 87, 88, 89
    )
    private val four = intArrayOf(
        7, 13, 14, 20, 21, 27, 28, 34, 35, 41, 43,
        44, 45, 46, 47, 55, 62, 69, 76, 83
    )
    private val five = intArrayOf(
        1, 2, 3, 4, 5, 7, 14, 21, 28, 35, 43, 44, 45,
        46, 47, 55, 62, 69, 76, 83, 85, 86, 87, 88, 89
    )
    private val six = intArrayOf(
        1, 2, 3, 4, 5, 7, 14, 21, 28, 35, 43, 44, 45, 46, 47, 49, 55,
        56, 62, 63, 69, 70, 76, 77, 83, 85, 86, 87, 88, 89
    )
    private val seven = intArrayOf(1, 2, 3, 4, 5, 13, 20, 27, 34, 41, 55, 62, 69, 76, 83)
    private val eight = intArrayOf(
        1, 2, 3, 4, 5, 7, 13, 14, 20, 21, 27, 28,
        34, 35, 41, 43, 44, 45, 46, 47, 49, 55, 56, 62, 63, 69, 70, 76, 77,
        83, 85, 86, 87, 88, 89
    )
    private val nine = intArrayOf(
        1, 2, 3, 4, 5, 7, 13, 14, 20, 21, 27, 28, 34,
        35, 41, 43, 44, 45, 46, 47, 55, 62, 69, 76, 83, 85, 86, 87, 88, 89
    )
    private val allDotsOff = intArrayOf()
    private var patterns = arrayOf(
        zero, one, two, three, four, five, six,
        seven, eight, nine, allDotsOff
    )

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
    var number: Int
        get() = current
        set(to) {
            if (to != current) {
                makeTransition(patterns[current], patterns[to])
                current = to
            }
        }

    override fun draw() {
        number = current
    }

    /**
     * Inform the member DotChangeAction which dots need to be change state to
     * move from the current state to the next state. The parameters are arrays
     * of one dimensional coordinates beginning in the top right corner of the
     * part of the [Grid] that the [Glyph] is being rendered in.
     *
     * @param from
     * A list of 1 dimensional co-ordinates, in ascending order, of
     * dots that are currently on.
     * @param to
     * A list of 1 dimensional co-ordinate, in ascending order, of
     * dots that will be on when the transition ends.
     */
    private fun makeTransition(from: IntArray, to: IntArray) {
        var f = 0
        var t = 0
        while (f < from.size && t < to.size) {
            if (from[f] == to[t]) {
                // Dot should stay on - no change
                f++
                t++
            } else if (from[f] > to[t]) {
                // Dot should be lit
                changeDot(to[t], true)
                t++
            } else {
                // Dot should be dimmed
                changeDot(from[f], false)
                f++
            }
        }
        if (f < from.size) {
            // Reached the end of to before the end of from - remaining from
            // must be dimmed
            while (f < from.size) {
                changeDot(from[f], false)
                f++
            }
        } else if (t < to.size) {
            // Reached the end of from before the end of to - remaining to must
            // be lit
            while (t < to.size) {
                changeDot(to[t], true)
                t++
            }
        }
    }
}