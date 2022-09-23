package com.escapeindustries.dotmatrix

/**
 * Pluggable action called by [GlyphTransition.makeTransition] when a dot
 * changes, allowing alternative actions to be swapped in. This was introduced
 * to aid testability.
 *
 * @author Mark Roberts
 */
internal interface DotChangeAction {
    /**
     * Perform an action when a dot changes
     * @param index The one dimensional coordinate of the dot that has changed
     * @param on The new state of the dot
     */
    fun dotHasChanged(index: Int, on: Boolean)
}