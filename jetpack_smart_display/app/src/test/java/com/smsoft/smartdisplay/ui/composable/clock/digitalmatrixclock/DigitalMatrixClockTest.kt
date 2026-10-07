package com.smsoft.smartdisplay.ui.composable.clock.digitalmatrixclock

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

/**
 * Drives the real Grid/Digit code with DigitalMatrixClock's digits (matrixFormat/matrixDigits)
 * through whole days in 24- and 12-hour format: the lit dots must match the digit patterns,
 * Grid.litColumns() must match the lit dots, and matrixLayout() must centre the lit dots on every
 * page size.
 */
class DigitalMatrixClockTest {

    // Lit dots of 0..9 (as in Digit.kt) as indices into a 7 x 13 cell, then BLANK_DIGIT.
    private val patterns = arrayOf(
        intArrayOf(
            1, 2, 3, 4, 5, 7, 13, 14, 20, 21, 27, 28, 34, 35, 41, 49, 55, 56, 62, 63, 69, 70, 76,
            77, 83, 85, 86, 87, 88, 89
        ),
        intArrayOf(13, 20, 27, 34, 41, 55, 62, 69, 76, 83),
        intArrayOf(
            1, 2, 3, 4, 5, 13, 20, 27, 34, 41, 43, 44, 45, 46, 47, 49, 56, 63, 70, 77, 85, 86, 87,
            88, 89
        ),
        intArrayOf(
            1, 2, 3, 4, 5, 13, 20, 27, 34, 41, 43, 44, 45, 46, 47, 55, 62, 69, 76, 83, 85, 86, 87,
            88, 89
        ),
        intArrayOf(7, 13, 14, 20, 21, 27, 28, 34, 35, 41, 43, 44, 45, 46, 47, 55, 62, 69, 76, 83),
        intArrayOf(
            1, 2, 3, 4, 5, 7, 14, 21, 28, 35, 43, 44, 45, 46, 47, 55, 62, 69, 76, 83, 85, 86, 87,
            88, 89
        ),
        intArrayOf(
            1, 2, 3, 4, 5, 7, 14, 21, 28, 35, 43, 44, 45, 46, 47, 49, 55, 56, 62, 63, 69, 70, 76,
            77, 83, 85, 86, 87, 88, 89
        ),
        intArrayOf(1, 2, 3, 4, 5, 13, 20, 27, 34, 41, 55, 62, 69, 76, 83),
        intArrayOf(
            1, 2, 3, 4, 5, 7, 13, 14, 20, 21, 27, 28, 34, 35, 41, 43, 44, 45, 46, 47, 49, 55, 56,
            62, 63, 69, 70, 76, 77, 83, 85, 86, 87, 88, 89
        ),
        intArrayOf(
            1, 2, 3, 4, 5, 7, 13, 14, 20, 21, 27, 28, 34, 35, 41, 43, 44, 45, 46, 47, 55, 62, 69,
            76, 83, 85, 86, 87, 88, 89
        ),
        intArrayOf()
    )

    private class Layout(
        val digitColumns: List<Int>,
        val separatorColumns: List<Int>,
        val columns: Int
    )

    // Where the format's glyphs land: a digit is 7 columns wide, a separator and a space 1.
    private fun layoutOf(format: String): Layout {
        val digits = ArrayList<Int>()
        val separators = ArrayList<Int>()
        var column = 0
        for (c in format) {
            when (c) {
                '0' -> { digits += column; column += 7 }
                ':' -> { separators += column; column += 1 }
                ' ' -> column += 1
            }
        }
        return Layout(digits, separators, column)
    }

    private fun newGrid(format: String) = Grid().apply {
        setPaddingDots(
            top = paddingRowsTop,
            left = paddingColumnsLeft,
            bottom = paddingRowsBottom,
            right = paddingColumnsRight
        )
        setFormat(format)
    }

    /** Checks every dot against the patterns and returns the lit column range. */
    private fun check(grid: Grid, layout: Layout, digits: IntArray, label: () -> String): IntRange {
        val expected = Array(layout.columns) { BooleanArray(13) }
        digits.forEachIndexed { i, value ->
            for (index in patterns[value]) {
                expected[layout.digitColumns[i] + index % 7][index / 7] = true
            }
        }
        for (column in layout.separatorColumns) {
            expected[column][4] = true
            expected[column][8] = true
        }
        if ((grid.columns != layout.columns) || (grid.rows != 13)) {
            fail("${label()}: grid ${grid.columns} x ${grid.rows}")
        }
        for (x in 0 until layout.columns) {
            for (y in 0 until 13) {
                if (grid.isLit(x, y) != expected[x][y]) {
                    fail("${label()}: dot ($x, $y) should be lit: ${expected[x][y]}")
                }
            }
        }
        val litX = (0 until layout.columns).filter { x -> expected[x].any { it } }
        val range = litX.first()..litX.last()
        if (grid.litColumns() != range) {
            fail("${label()}: litColumns ${grid.litColumns()}, lit dots $range")
        }
        return range
    }

    private data class Page(val width: Float, val height: Float, val density: Float)

    // Page sizes (screen minus the 40 dp page dots) and densities of the test resolutions.
    private val pages = listOf(
        Page(2560f, 1520f, 2f), Page(1280f, 746.7f, 1.33125f), Page(1024f, 560f, 1f),
        Page(1920f, 1020f, 1.5f), Page(800f, 1226.7f, 1.33125f), Page(800f, 440f, 1f)
    )

    // The default dot size and spacing (dp) of each style and format.
    private fun defaultDot(dotStyle: DotStyle, isShowSeconds: Boolean): Pair<Float, Float> =
        when (dotStyle) {
            DotStyle.SQUARE -> if (isShowSeconds) {
                DEFAULT_DOT_RADIUS_SQUARE_SECONDS_MC to DEFAULT_DOT_SPACING_SQUARE_SECONDS_MC
            } else {
                DEFAULT_DOT_RADIUS_SQUARE_MC to DEFAULT_DOT_SPACING_SQUARE_MC
            }
            DotStyle.ROUND -> if (isShowSeconds) {
                DEFAULT_DOT_RADIUS_ROUND_SECONDS_MC to DEFAULT_DOT_SPACING_ROUND_SECONDS_MC
            } else {
                DEFAULT_DOT_RADIUS_ROUND_MC to DEFAULT_DOT_SPACING_ROUND_MC
            }
        }

    // The layout OnDraw uses for the grid, by default with the grid's own lit columns.
    private fun layoutOn(
        grid: Grid,
        page: Page,
        dotStyle: DotStyle,
        isShowSeconds: Boolean,
        litColumns: IntRange? = grid.litColumns()
    ): MatrixLayout {
        val (dotSize, dotSpacing) = defaultDot(dotStyle, isShowSeconds)
        return matrixLayout(
            width = page.width,
            height = page.height,
            density = page.density,
            columns = grid.columns,
            rows = grid.rows,
            litColumns = litColumns,
            dotSize = dotSize,
            dotSpacing = dotSpacing,
            dotStyle = dotStyle
        )
    }

    // Outer edges (px) of the dots in [columns] or the first [rows], as MatrixLayout places them.
    private fun MatrixLayout.horizontal(columns: IntRange) =
        Pair(left + columns.first * pitch, left + columns.last * pitch + dot)

    private fun MatrixLayout.vertical(rows: Int) = Pair(top, top + (rows - 1) * pitch + dot)

    /**
     * On every page and in both dot styles the lit dots are centred horizontally and the grid
     * vertically, all on the page, with the dot size of the whole grid.
     */
    private fun checkCentred(grid: Grid, isShowSeconds: Boolean, label: () -> String) {
        val lit = checkNotNull(grid.litColumns()) { "${label()}: nothing lit" }
        for (page in pages) {
            for (dotStyle in DotStyle.entries) {
                val layout = layoutOn(grid, page, dotStyle, isShowSeconds)
                val (left, right) = layout.horizontal(lit)
                val (top, bottom) = layout.vertical(grid.rows)
                if ((abs((left + right) / 2f - page.width / 2f) >= 0.01f) ||
                    (abs((top + bottom) / 2f - page.height / 2f) >= 0.01f)
                ) {
                    fail("${label()}, $page, $dotStyle: lit dots $left..$right x $top..$bottom")
                }
                if ((left < 0f) || (right > page.width) || (top < 0f) || (bottom > page.height)) {
                    fail("${label()}, $page, $dotStyle: off page: $left..$right x $top..$bottom")
                }
                val wholeGrid = layoutOn(grid, page, dotStyle, isShowSeconds, litColumns = null)
                if ((layout.pitch != wholeGrid.pitch) || (layout.dot != wholeGrid.dot)) {
                    fail("${label()}, $page, $dotStyle: dot size $layout, whole grid $wholeGrid")
                }
            }
        }
    }

    private fun label(
        hour: Int,
        minute: Int,
        second: Int,
        is24Hour: Boolean,
        isShowSeconds: Boolean
    ) = "%02d:%02d:%02d (24 h: %b, seconds: %b)"
        .format(hour, minute, second, is24Hour, isShowSeconds)

    // Steps one grid through whole days, as the clock does; the lit columns may only move at the
    // hour.
    private fun sweep(is24Hour: Boolean, isShowSeconds: Boolean, stepSeconds: Int, days: Int) {
        val layout = layoutOf(matrixFormat(isShowSeconds))
        val grid = newGrid(matrixFormat(isShowSeconds))
        val litByHour = HashMap<Int, IntRange>()
        for (t in 0 until days * 86400 step stepSeconds) {
            val s = t % 86400
            val hour = s / 3600
            val minute = s / 60 % 60
            val second = s % 60
            val label = { label(hour, minute, second, is24Hour, isShowSeconds) }
            val digits = matrixDigits(hour, minute, second, isShowSeconds, is24Hour)
            grid.setDigits(*digits)
            val range = check(grid, layout, digits, label)
            val previous = litByHour.put(hour, range)
            if ((previous != null) && (previous != range)) {
                fail("${label()}: lit columns changed within the hour ($previous -> $range)")
            }
            checkCentred(grid, isShowSeconds, label)
        }
    }

    @Test
    fun everyMinute_24Hour() =
        sweep(is24Hour = true, isShowSeconds = false, stepSeconds = 60, days = 2)

    @Test
    fun everyMinute_12Hour() =
        sweep(is24Hour = false, isShowSeconds = false, stepSeconds = 60, days = 2)

    @Test
    fun everySecondWithSeconds_24Hour() =
        sweep(is24Hour = true, isShowSeconds = true, stepSeconds = 1, days = 1)

    @Test
    fun everySecondWithSeconds_12Hour() =
        sweep(is24Hour = false, isShowSeconds = true, stepSeconds = 1, days = 1)

    @Test
    fun randomJumps() {
        // Time set by hand, a time zone change, a 12/24-hour switch or the screen coming back:
        // any time follows any time.
        val random = Random(42)
        for (isShowSeconds in listOf(false, true)) {
            val layout = layoutOf(matrixFormat(isShowSeconds))
            val grid = newGrid(matrixFormat(isShowSeconds))
            repeat(20000) {
                val s = random.nextInt(86400)
                val is24Hour = random.nextBoolean()
                val label = { label(s / 3600, s / 60 % 60, s % 60, is24Hour, isShowSeconds) }
                val digits = matrixDigits(s / 3600, s / 60 % 60, s % 60, isShowSeconds, is24Hour)
                grid.setDigits(*digits)
                check(grid, layout, digits, label)
                checkCentred(grid, isShowSeconds, label)
            }
        }
    }

    @Test
    fun digits_hourHasNoLeadingZero() {
        val b = BLANK_DIGIT
        assertArrayEquals(
            intArrayOf(b, 0, 0, 0),
            matrixDigits(0, 0, 0, isShowSeconds = false, is24Hour = true)
        )
        assertArrayEquals(
            intArrayOf(b, 9, 0, 5),
            matrixDigits(9, 5, 0, isShowSeconds = false, is24Hour = true)
        )
        assertArrayEquals(
            intArrayOf(2, 3, 5, 9, 4, 1),
            matrixDigits(23, 59, 41, isShowSeconds = true, is24Hour = true)
        )
        assertArrayEquals(
            intArrayOf(1, 2, 0, 0),
            matrixDigits(0, 0, 0, isShowSeconds = false, is24Hour = false)
        )
        assertArrayEquals(
            intArrayOf(b, 9, 0, 5),
            matrixDigits(21, 5, 0, isShowSeconds = false, is24Hour = false)
        )
        assertArrayEquals(
            intArrayOf(1, 2, 3, 0, 0, 7),
            matrixDigits(12, 30, 7, isShowSeconds = true, is24Hour = false)
        )
        // One value per digit of the format.
        for (isShowSeconds in listOf(false, true)) {
            assertEquals(
                matrixFormat(isShowSeconds).count { it == '0' },
                matrixDigits(12, 34, 56, isShowSeconds, is24Hour = true).size
            )
        }
    }

    @Test
    fun litColumns_followTheHour() {
        // "0 0 : 0 0": hour digits at columns 0 and 8, minutes at 18 and 26; every digit lights its
        // right column, a "1" only that one.
        val grid = newGrid(matrixFormat(isShowSeconds = false))
        fun litAt(hour: Int, is24Hour: Boolean): IntRange? {
            grid.setDigits(*matrixDigits(hour, 0, 0, isShowSeconds = false, is24Hour = is24Hour))
            return grid.litColumns()
        }
        assertEquals(8..32, litAt(0, is24Hour = true))
        assertEquals(14..32, litAt(1, is24Hour = true))
        assertEquals(6..32, litAt(10, is24Hour = true))
        assertEquals(0..32, litAt(20, is24Hour = true))
        assertEquals(6..32, litAt(0, is24Hour = false))
        assertEquals(14..32, litAt(13, is24Hour = false))
        assertEquals(8..32, litAt(14, is24Hour = false))
        assertEquals(6..32, litAt(22, is24Hour = false))
    }

    @Test
    fun litColumns_nullWhenNothingIsLit() {
        // The separator is always lit, so a fully dark grid only exists without one.
        val grid = newGrid("0 0")
        assertNull(grid.litColumns())
        grid.setDigits(BLANK_DIGIT, BLANK_DIGIT)
        assertNull(grid.litColumns())
        grid.setDigits(1, BLANK_DIGIT)
        assertEquals(6..6, grid.litColumns())
    }

    @Test
    fun separatorColumns_onlyTheColons() {
        // Only these columns blink.
        for ((isShowSeconds, colons) in listOf(false to listOf(16), true to listOf(16, 34))) {
            val grid = newGrid(matrixFormat(isShowSeconds))
            assertEquals(colons, layoutOf(matrixFormat(isShowSeconds)).separatorColumns)
            assertEquals(colons, (0 until grid.columns).filter { grid.isSeparatorColumn(it) })
        }
    }

    @Test
    fun layout_dotSizeDoesNotChangeWithTheTime() {
        // 1:05 lights columns 14..32 (the "1" only its right column), 20:08 all of 0..32. Both get
        // the dot size of the whole grid; 1:05 only sits 7 columns (half of the dark ones) further
        // left, so that its lit dots are centred too.
        val grid = newGrid(matrixFormat(isShowSeconds = false))
        for (page in pages) {
            for (dotStyle in DotStyle.entries) {
                val label = "$page, $dotStyle"
                grid.setDigits(*matrixDigits(1, 5, 0, isShowSeconds = false, is24Hour = true))
                assertEquals(14..32, grid.litColumns())
                val early = layoutOn(grid, page, dotStyle, isShowSeconds = false)
                grid.setDigits(*matrixDigits(20, 8, 0, isShowSeconds = false, is24Hour = true))
                assertEquals(0..32, grid.litColumns())
                val late = layoutOn(grid, page, dotStyle, isShowSeconds = false)
                assertEquals(label, late.pitch, early.pitch, 0f)
                assertEquals(label, late.dot, early.dot, 0f)
                assertEquals(label, late.top, early.top, 0f)
                assertEquals(label, late.left - 7 * late.pitch, early.left, 0.01f)
            }
        }
    }

    @Test
    fun layout_requestedSizeWhenItFits() {
        // 2560 x 1520 px at density 1.5 has room for 33 x 13 dots of 24 + 12 dp (54 px).
        val square = matrixLayout(
            width = 2560f, height = 1520f, density = 1.5f, columns = 33, rows = 13,
            litColumns = 0..32, dotSize = 24f, dotSpacing = 12f, dotStyle = DotStyle.SQUARE
        )
        assertEquals(54f, square.pitch, 1e-4f)
        assertEquals(36f, square.dot, 1e-4f)
        // The size of a round dot is its radius: 2 x 14 + 7 dp.
        val round = matrixLayout(
            width = 2560f, height = 1520f, density = 1.5f, columns = 33, rows = 13,
            litColumns = 0..32, dotSize = 14f, dotSpacing = 7f, dotStyle = DotStyle.ROUND
        )
        assertEquals(52.5f, round.pitch, 1e-4f)
        assertEquals(42f, round.dot, 1e-4f)
    }

    @Test
    fun layout_limitedByTheWidth() {
        // Portrait: 24 + 12 dp would be 47.9 px, but the 33 columns get only 92 % of 800 px.
        val page = Page(800f, 1226.7f, 1.33125f)
        val layout = matrixLayout(
            width = page.width, height = page.height, density = page.density, columns = 33,
            rows = 13, litColumns = 0..32, dotSize = 24f, dotSpacing = 12f,
            dotStyle = DotStyle.SQUARE
        )
        assertEquals(0.92f * page.width, 33 * layout.pitch, 0.01f)
        // Dot and gap shrink together.
        assertEquals(layout.pitch * 24f / 36f, layout.dot, 1e-4f)
        val (left, right) = layout.horizontal(0..32)
        assertTrue("$left..$right", (left >= 0f) && (right <= page.width))
        assertEquals(page.width / 2f, (left + right) / 2f, 0.01f)
    }

    @Test
    fun layout_limitedByTheHeight() {
        // Wide and low: 24 + 12 dp would be 72 px, but the 13 rows get only 85 % of 300 px.
        val page = Page(2560f, 300f, 2f)
        val layout = matrixLayout(
            width = page.width, height = page.height, density = page.density, columns = 33,
            rows = 13, litColumns = 0..32, dotSize = 24f, dotSpacing = 12f,
            dotStyle = DotStyle.SQUARE
        )
        assertEquals(0.85f * page.height, 13 * layout.pitch, 0.01f)
        assertEquals(layout.pitch * 24f / 36f, layout.dot, 1e-4f)
        val (top, bottom) = layout.vertical(13)
        assertTrue("$top..$bottom", (top >= 0f) && (bottom <= page.height))
        assertEquals(page.height / 2f, (top + bottom) / 2f, 0.01f)
    }

    @Test
    fun layout_nothingLit_centresTheWholeGrid() {
        for (page in pages) {
            for (dotStyle in DotStyle.entries) {
                val (dotSize, dotSpacing) = defaultDot(dotStyle, isShowSeconds = false)
                fun layout(litColumns: IntRange?) = matrixLayout(
                    width = page.width, height = page.height, density = page.density,
                    columns = 33, rows = 13, litColumns = litColumns, dotSize = dotSize,
                    dotSpacing = dotSpacing, dotStyle = dotStyle
                )
                val dark = layout(litColumns = null)
                assertEquals("$page, $dotStyle", layout(litColumns = 0..32), dark)
                val (left, right) = dark.horizontal(0..32)
                assertEquals("$page, $dotStyle", page.width / 2f, (left + right) / 2f, 0.01f)
            }
        }
    }

    @Test
    fun gridSize() {
        assertEquals(33, newGrid(matrixFormat(isShowSeconds = false)).columns)
        assertEquals(51, newGrid(matrixFormat(isShowSeconds = true)).columns)
        assertEquals(13, newGrid(matrixFormat(isShowSeconds = true)).rows)
        assertEquals(
            layoutOf(matrixFormat(isShowSeconds = true)).columns,
            newGrid(matrixFormat(isShowSeconds = true)).columns
        )
    }

    @Test
    fun dotStyle_unknownIdFallsBack() {
        DotStyle.entries.forEach { assertEquals(it, DotStyle.getById(it.value)) }
        assertEquals(DotStyle.getDefault(), DotStyle.getById("hexagon"))
        assertEquals(DotStyle.getDefault(), DotStyle.getById(""))
    }
}
