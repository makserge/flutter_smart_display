package com.smsoft.smartdisplay.ui.composable.clock.digitalmatrixclock

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.ui.screen.clock.ClockViewModel
import com.smsoft.smartdisplay.ui.screen.clock.displayHour
import com.smsoft.smartdisplay.ui.theme.teal200
import com.smsoft.smartdisplay.utils.getStateFromFlow
import kotlin.math.min

@Composable
fun DigitalMatrixClock(
    modifier: Modifier = Modifier
        .fillMaxSize(),
    viewModel: ClockViewModel,
    scale: Float,
    hour: Int,
    minute: Int,
    second: Int,
    is24Hour: Boolean
) {
    val dotStyle = getStateFromFlow(
        flow = viewModel.dotStyleMC,
        defaultValue = DotStyle.getDefault()
    ) as DotStyle

    val isShowSeconds = getStateFromFlow(
        flow = viewModel.isShowSecondsMC,
        defaultValue = DEFAULT_SHOW_SECONDS_MC
    ) as Boolean

    // The matrix has its own dot color (turquoise green by default) instead of the shared clock colors.
    val dotColorValue = getStateFromFlow(
        flow = viewModel.dotColorMC,
        defaultValue = null
    ) as String?
    val dotColor = remember(dotColorValue) {
        dotColorValue?.let {
            runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
        } ?: DEFAULT_DOT_COLOR_MC
    }

    val isBlinkSeparator = getStateFromFlow(
        flow = viewModel.isBlinkSeparatorMC,
        defaultValue = DEFAULT_BLINK_SEPARATOR_MC
    ) as Boolean

    val dotRadiusRound = scale * getStateFromFlow(
        flow = viewModel.dotRadiusRoundMC,
        defaultValue = DEFAULT_DOT_RADIUS_ROUND_MC
    ) as Float

    val dotSpacingRound = scale * getStateFromFlow(
        flow = viewModel.dotSpacingRoundMC,
        defaultValue = DEFAULT_DOT_SPACING_ROUND_MC
    ) as Float

    val dotRadiusRoundSec = scale * getStateFromFlow(
        flow = viewModel.dotRadiusRoundSecMC,
        defaultValue = DEFAULT_DOT_RADIUS_ROUND_SECONDS_MC
    ) as Float

    val dotSpacingRoundSec = scale * getStateFromFlow(
        flow = viewModel.dotSpacingRoundSecMC,
        defaultValue = DEFAULT_DOT_SPACING_ROUND_SECONDS_MC
    ) as Float

    val dotRadiusSquare = scale * getStateFromFlow(
        flow = viewModel.dotRadiusSquareMC,
        defaultValue = DEFAULT_DOT_RADIUS_SQUARE_MC
    ) as Float

    val dotSpacingSquare = scale * getStateFromFlow(
        flow = viewModel.dotSpacingSquareMC,
        defaultValue = DEFAULT_DOT_SPACING_SQUARE_MC
    ) as Float

    val dotRadiusSquareSec = scale * getStateFromFlow(
        flow = viewModel.dotRadiusSquareSecMC,
        defaultValue = DEFAULT_DOT_RADIUS_SQUARE_SECONDS_MC
    ) as Float

    val dotSpacingSquareSec = scale * getStateFromFlow(
        flow = viewModel.dotSpacingSquareSecMC,
        defaultValue = DEFAULT_DOT_SPACING_SQUARE_SECONDS_MC
    ) as Float

    // Square: dot side; round: dot radius. Both in dp.
    val dotSize = when (dotStyle) {
        DotStyle.SQUARE -> if (isShowSeconds) dotRadiusSquareSec else dotRadiusSquare
        DotStyle.ROUND -> if (isShowSeconds) dotRadiusRoundSec else dotRadiusRound
    }
    val dotSpacing = when (dotStyle) {
        DotStyle.SQUARE -> if (isShowSeconds) dotSpacingSquareSec else dotSpacingSquare
        DotStyle.ROUND -> if (isShowSeconds) dotSpacingRoundSec else dotSpacingRound
    }

    // One grid per format, kept between seconds. It used to be a global rebuilt on every
    // recomposition and positioned from the screen size, not from the page.
    val grid = remember(isShowSeconds) {
        Grid().apply {
            setPaddingDots(
                top = paddingRowsTop,
                left = paddingColumnsLeft,
                bottom = paddingRowsBottom,
                right = paddingColumnsRight
            )
            setFormat(matrixFormat(isShowSeconds))
        }
    }
    grid.setDigits(*matrixDigits(hour, minute, second, isShowSeconds, is24Hour))

    // The separator is lit in the first half of every second and dark in the second half.
    // Read only while drawing, so the blink redraws the Canvas without recomposing anything.
    val clockState = viewModel.uiState.collectAsStateWithLifecycle()
    val isFirstHalfOfSecond = remember(clockState) {
        derivedStateOf { clockState.value.milliSecond < HALF_SECOND_MS }
    }
    val isBlinkSeparatorState by rememberUpdatedState(isBlinkSeparator)

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        OnDraw(
            grid = grid,
            color = dotColor,
            dotSize = dotSize,
            dotSpacing = dotSpacing,
            dotStyle = dotStyle,
            second = second,
            isSeparatorVisible = { !isBlinkSeparatorState || isFirstHalfOfSecond.value }
        )
    }
}

// The grid format and the digits shown in it. Outside the composable so a JVM unit test can drive
// the real Grid with them.

/** "0 0 : 0 0", or "0 0 : 0 0 : 0 0" with the seconds. */
internal fun matrixFormat(isShowSeconds: Boolean): String =
    if (isShowSeconds) FORMAT_SECONDS else FORMAT

/**
 * The digits of the time for [matrixFormat], from left to right. The hour has no leading zero:
 * its tens digit stays dark ([BLANK_DIGIT]) below 10.
 */
internal fun matrixDigits(
    hour: Int,
    minute: Int,
    second: Int,
    isShowSeconds: Boolean,
    is24Hour: Boolean
): IntArray {
    val shownHour = displayHour(hour, is24Hour)
    val hourTens = if (shownHour < 10) BLANK_DIGIT else shownHour / 10
    return if (isShowSeconds) {
        intArrayOf(hourTens, shownHour % 10, minute / 10, minute % 10, second / 10, second % 10)
    } else {
        intArrayOf(hourTens, shownHour % 10, minute / 10, minute % 10)
    }
}

/**
 * Draws [grid] as large as [dotSize] and [dotSpacing] (dp) ask for, but never larger than the
 * page allows: it shrinks to fit narrow, small or portrait screens. The dot size comes from the
 * whole grid; horizontally the time is centred on its lit dots, vertically on the grid. The
 * sizes used to be raw pixels placed from the screen size, so the clock was clipped on small and
 * portrait panels and covered less than half of a large high-density one.
 */
@Suppress("UNUSED_EXPRESSION")
@Composable
private fun OnDraw(
    grid: Grid,
    color: Color,
    dotSize: Float,
    dotSpacing: Float,
    dotStyle: DotStyle,
    second: Int,
    isSeparatorVisible: () -> Boolean
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        // Redraw when the time changes; the grid itself is not snapshot state.
        second
        val columns = grid.columns
        val rows = grid.rows
        if ((columns == 0) || (rows == 0)) {
            return@Canvas
        }
        // The result's fields are read by name: all four are Float, so a positional
        // destructuring that mixed them up would still compile
        val layout = matrixLayout(
            width = size.width,
            height = size.height,
            density = density,
            columns = columns,
            rows = rows,
            litColumns = grid.litColumns(),
            dotSize = dotSize,
            dotSpacing = dotSpacing,
            dotStyle = dotStyle
        )
        val showSeparator = isSeparatorVisible()
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                // Unlit dots are not drawn, so they take the color of whatever is behind the clock.
                val isLit = grid.isLit(column, row) &&
                    (showSeparator || !grid.isSeparatorColumn(column))
                if (!isLit) {
                    continue
                }
                val x = layout.left + column * layout.pitch
                val y = layout.top + row * layout.pitch
                when (dotStyle) {
                    DotStyle.SQUARE -> drawRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(layout.dot, layout.dot)
                    )
                    DotStyle.ROUND -> drawCircle(
                        color = color,
                        radius = layout.dot / 2f,
                        center = Offset(x + layout.dot / 2f, y + layout.dot / 2f)
                    )
                }
            }
        }
    }
}

/**
 * Where [OnDraw] puts the dots, in px: dot (column, row) is a square of side [dot] (or the circle
 * in it) with its top-left corner at ([left] + column * [pitch], [top] + row * [pitch]).
 */
internal data class MatrixLayout(
    val pitch: Float,
    val dot: Float,
    val left: Float,
    val top: Float
)

/**
 * The layout of a grid of [columns] x [rows] (both > 0) on a page of [width] x [height] px, with
 * [dotSize] and [dotSpacing] in dp as in [OnDraw]. [litColumns] is the range of lit columns, or
 * null when nothing is lit. A pure function, so a JVM unit test can check it.
 */
internal fun matrixLayout(
    width: Float,
    height: Float,
    density: Float,
    columns: Int,
    rows: Int,
    litColumns: IntRange?,
    dotSize: Float,
    dotSpacing: Float,
    dotStyle: DotStyle
): MatrixLayout {
    // Dot extent (square side or round diameter) and the distance between dots, in dp.
    val dotExtent = if (dotStyle == DotStyle.SQUARE) dotSize else 2 * dotSize
    val unit = (dotExtent + dotSpacing).coerceAtLeast(1f)
    val fitPitch = min(
        width * MAX_WIDTH_FRACTION / columns,
        height * MAX_HEIGHT_FRACTION / rows
    )
    val pitch = min(unit * density, fitPitch)
    val k = pitch / unit
    val dot = dotExtent * k
    val gap = dotSpacing * k
    // Horizontally centred on the lit columns, not on the whole grid: the hour's tens digit
    // is dark before 10:00 and a "1" lights only its right column, which put the time
    // off-centre now that unlit dots are not drawn. The dot size above still comes from the
    // whole grid, so it never changes with the time; only the position moves, at the hours
    // that change the first lit column (e.g. 1:00, 10:00, 20:00; 1:00 and 10:00 in 12-hour
    // format). Vertically it stays on the whole grid on purpose: the digits light different
    // rows, so centring on them would make the time jump every minute.
    val lit = litColumns ?: (0 until columns)
    val litWidth = (lit.last - lit.first + 1) * pitch - gap
    val left = (width - litWidth) / 2f - lit.first * pitch
    val top = (height - (rows * pitch - gap)) / 2f
    return MatrixLayout(pitch = pitch, dot = dot, left = left, top = top)
}

enum class DotStyle(val value: String, val titleId: Int) {
    SQUARE("square", R.string.dot_style_square),
    ROUND("round", R.string.dot_style_round);

    companion object {
        fun toMap(context: Context): Map<String, String> {
            return entries.associate {
                it.value to context.getString(it.titleId)
            }
        }

        fun getDefault(): DotStyle {
            return SQUARE
        }

        fun getDefaultId(): String {
            return getDefault().value
        }

        /** An unknown id falls back to the default instead of crashing the clock page. */
        fun getById(id: String): DotStyle {
            return entries.firstOrNull { it.value == id } ?: getDefault()
        }
    }
}

private const val FORMAT = "0 0 : 0 0"
private const val FORMAT_SECONDS = "0 0 : 0 0 : 0 0"
private const val HALF_SECOND_MS = 500
// Margins around the matrix on the clock page.
private const val MAX_WIDTH_FRACTION = 0.92f
private const val MAX_HEIGHT_FRACTION = 0.85f

const val DEFAULT_SHOW_SECONDS_MC = false
const val DEFAULT_BLINK_SEPARATOR_MC = true
val DEFAULT_DOT_COLOR_MC = teal200

const val DEFAULT_DOT_SPACING_ROUND_MC = 7F
const val DEFAULT_DOT_RADIUS_ROUND_MC = 14F
const val DEFAULT_DOT_SPACING_ROUND_SECONDS_MC = 5F
const val DEFAULT_DOT_RADIUS_ROUND_SECONDS_MC = 10F

const val DEFAULT_DOT_SPACING_SQUARE_MC = 12F
const val DEFAULT_DOT_RADIUS_SQUARE_MC = 24F
const val DEFAULT_DOT_SPACING_SQUARE_SECONDS_MC = 8F
const val DEFAULT_DOT_RADIUS_SQUARE_SECONDS_MC = 16F
