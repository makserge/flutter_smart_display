package com.escapeindustries.dotmatrix

/**
 * The general case [DotChangeAction], which calls the configured
 * [Glyph] to change the indicated dot.
 *
 * @author Mark Roberts
 */
internal class NormalDotChangeAction(private val glyph: Glyph) : DotChangeAction {
    /**
     * Signal to the configured [Glyph] that a dot has changed.
     */
    override fun dotHasChanged(index: Int, on: Boolean) {
        glyph.changeDot(index, on)
    }
}