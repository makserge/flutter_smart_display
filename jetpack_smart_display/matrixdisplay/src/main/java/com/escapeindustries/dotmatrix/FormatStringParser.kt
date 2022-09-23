package com.escapeindustries.dotmatrix

/**
 * A utility class to parse format strings.
 *
 * @author Mark Roberts
 */
internal class FormatStringParser
/**
 * Construct an instance, configuring it to use a specific
 * [GlyphFactory].
 *
 * @param factory
 * The [GlyphFactory] that will be used when parsing format
 * strings into sets of [Glyph]s
 */(private val factory: GlyphFactory) {
    /**
     * Parse a format string into a set of [Glyph]s
     *
     * @param format
     * The format to be parsed. The format can be made up of '0' (a
     * digit), ':" (a colon) and ' ' (space - a gap between other
     * [Glyph]s)
     * @return All the [Glyph]s that were found, working from left to
     * right.
     */
    fun parse(format: String): Array<Glyph> {
        val glyphs: MutableList<Glyph> = ArrayList()
        for (element in format) {
            when (element) {
                '0' -> glyphs.add(factory.createDigit())
                ':' -> glyphs.add(factory.createSeparator())
                ' ' -> glyphs.add(factory.createSpace())
            }
        }
        return glyphs.toTypedArray()
    }
}