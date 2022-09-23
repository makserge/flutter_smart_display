package com.escapeindustries.dotmatrix

/**
 * A factory for creating [Glyph]s.
 *
 * @author Mark Roberts
 * @param grid
 * The [Grid] that all created [Glyph]s will drawn in
*/
internal class GlyphFactory(private val grid: Grid) {
    /**
     * @return A [Digit] set to draw to the configured [Grid]
     */
    fun createDigit(): Digit {
        return Digit(grid, 0, 0)
    }

    /**
     * @return A [Separator] set to draw to the configured [Grid]
     */
    fun createSeparator(): Separator {
        return Separator(grid, 0, 0)
    }

    /**
     * @return A [Space] set to draw to the configured [Grid]
     */
    fun createSpace(): Space {
        return Space()
    }
}