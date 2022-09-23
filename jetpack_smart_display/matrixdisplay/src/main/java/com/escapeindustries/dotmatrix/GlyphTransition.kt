package com.escapeindustries.dotmatrix

/**
 * A utility class to hold the logic by which the Grid makes the transition from
 * one [Glyph] to the next.
 *
 * @author Mark Roberts
 */
internal class GlyphTransition
/**
 * Construct and configure a GlyphTransition.
 *
 * @param action
 * action will be informed of all changes that need to be made
 * when transitions are calculated.
 */(private val action: DotChangeAction) {
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
    fun makeTransition(from: IntArray, to: IntArray) {
        var f = 0
        var t = 0
        while (f < from.size && t < to.size) {
            if (from[f] == to[t]) {
                // Dot should stay on - no change
                f++
                t++
            } else if (from[f] > to[t]) {
                // Dot should be lit
                action.dotHasChanged(to[t], true)
                t++
            } else {
                // Dot should be dimmed
                action.dotHasChanged(from[f], false)
                f++
            }
        }
        if (f < from.size) {
            // Reached the end of to before the end of from - remaining from
            // must be dimmed
            while (f < from.size) {
                action.dotHasChanged(from[f], false)
                f++
            }
        } else if (t < to.size) {
            // Reached the end of from before the end of to - remaining to must
            // be lit
            while (t < to.size) {
                action.dotHasChanged(to[t], true)
                t++
            }
        }
    }
}