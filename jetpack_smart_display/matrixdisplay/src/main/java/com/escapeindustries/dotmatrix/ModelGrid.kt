package com.escapeindustries.dotmatrix

/**
 * A [Grid] implementation that only stores the state of all the dots.
 * Drawing to this only changes the stored state.
 *
 * @author Mark Roberts
 */
internal class ModelGrid : BaseGrid() {
    override val paddingColumnsLeft = 0
    override val paddingColumnsRight = 0
    override val paddingRowsTop = 0
    override val paddingRowsBottom = 0
    private lateinit var grid: Array<IntArray>

    /**
     * @return Are any dots in a transition state? True if dots are in
     * transition, false if all dots are fully ON or OFF.
     */
    var transitionsActive = false

    override fun changeDot(x: Int, y: Int, on: Boolean) {
        val current = grid[x][y]
        if (on) {
            if (current == 0 || current == 1) {
                grid[x][y] = 2
            }
        } else {
            if (current == 2 || current == 3) {
                grid[x][y] = 1
            }
        }
    }

    override fun initializeGrid() {
        grid = Array(columns) { IntArray(rows) }
    }

    /**
     * Update the state so that all dots marked as in transition to ON become ON
     * and those in transition to OFF become OFF.
     */
    fun clearTransitionState() {
        for (x in 0 until columns) {
            for (y in 0 until rows) {
                if (grid[x][y] == 1) {
                    grid[x][y] = 0
                } else if (grid[x][y] == 2) {
                    grid[x][y] = 3
                }
            }
        }
        transitionsActive = false
    }

    fun getDotState(x: Int, y: Int): Int {
        return grid[x][y]
    }
}