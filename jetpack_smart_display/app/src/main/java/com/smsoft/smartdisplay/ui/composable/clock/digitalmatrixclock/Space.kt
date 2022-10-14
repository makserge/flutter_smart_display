package com.smsoft.smartdisplay.ui.composable.clock.digitalmatrixclock

/**
 * A [Glyph] representing a gap between other [Glyph]s.
 * @author Mark Roberts
 */
internal class Space : Glyph() {
    init {
        width = 1
        height = 13
    }

    override fun draw() {
        // Do nothing - this is an empty space
    }
}