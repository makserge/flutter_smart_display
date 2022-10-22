package com.smsoft.smartdisplay.ui.composable.clock.digitalmatrixclock

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.getStateFromFlow
import com.smsoft.smartdisplay.ui.screen.clock.ClockViewModel

@Composable
fun DigitalMatrixClock(
    modifier: Modifier = Modifier
        .fillMaxSize(),
    viewModel: ClockViewModel,
    scale: Float,
    primaryColor: Color,
    secondaryColor: Color,
    hour: Int,
    minute: Int,
    second: Int
) {
    val dotStyle = getStateFromFlow(
        flow = viewModel.dotStyleMC,
        defaultValue = DotStyle.getDefault()
    ) as DotStyle

    val isShowSeconds = getStateFromFlow(
        flow = viewModel.isShowSecondsMC,
        defaultValue = DEFAULT_SHOW_SECONDS_MC
    ) as Boolean

    val dotRadiusRound = scale * getStateFromFlow(
        flow = viewModel.dotRadiusRoundMC,
        defaultValue = DEFAULT_DOT_RADIUS_ROUND_MC
    ) as Float

    val dotSpacingRound = (scale * getStateFromFlow(
        flow = viewModel.dotSpacingRoundMC,
        defaultValue = DEFAULT_DOT_SPACING_ROUND_MC
    ) as Float).toInt()

    val dotRadiusRoundSec = scale * getStateFromFlow(
        flow = viewModel.dotRadiusRoundSecMC,
        defaultValue = DEFAULT_DOT_RADIUS_ROUND_SECONDS_MC
    ) as Float

    val dotSpacingRoundSec = (scale * getStateFromFlow(
        flow = viewModel.dotSpacingRoundSecMC,
        defaultValue = DEFAULT_DOT_SPACING_ROUND_SECONDS_MC
    ) as Float).toInt()

    val dotRadiusSquare = scale * getStateFromFlow(
        flow = viewModel.dotRadiusSquareMC,
        defaultValue = DEFAULT_DOT_RADIUS_SQUARE_MC
    ) as Float

    val dotSpacingSquare = (scale * getStateFromFlow(
        flow = viewModel.dotSpacingSquareMC,
        defaultValue = DEFAULT_DOT_SPACING_SQUARE_MC
    ) as Float).toInt()

    val dotRadiusSquareSec = scale * getStateFromFlow(
        flow = viewModel.dotRadiusSquareSecMC,
        defaultValue = DEFAULT_DOT_RADIUS_SQUARE_SECONDS_MC
    ) as Float

    val dotSpacingSquareSec = (scale * getStateFromFlow(
        flow = viewModel.dotSpacingSquareSecMC,
        defaultValue = DEFAULT_DOT_SPACING_SQUARE_SECONDS_MC
    ) as Float).toInt()

    val configuration = LocalConfiguration.current

    val width = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }.toInt()
    val height = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }.toInt()

    val dotRadius = when(dotStyle) {
        DotStyle.SQUARE -> {
            if (isShowSeconds) dotRadiusSquareSec else dotRadiusSquare
        }
        DotStyle.ROUND -> {
            if (isShowSeconds) dotRadiusRoundSec else dotRadiusRound
        }
    }
    val dotSpacing = when(dotStyle) {
        DotStyle.SQUARE -> {
            if (isShowSeconds) dotSpacingSquareSec else dotSpacingSquare
        }
        DotStyle.ROUND -> {
            if (isShowSeconds) dotSpacingRoundSec else dotSpacingRound
        }
    }

    initialize(
        width = (width * scale).toInt(),
        height = height,
        dotRadius = dotRadius,
        dotSpacing = dotSpacing,
        dotStyle = dotStyle,
        isShowSeconds = isShowSeconds
    )

    val value = if (isShowSeconds) {
        hour.toString() + ":" + "%02d".format(minute) + ":" + "%02d".format(second)
    }
    else {
        hour.toString() + ":" + "%02d".format(minute)
    }

    model.setValue(value)

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        OnDraw(
            modifier = Modifier,
            color = primaryColor,
            backgroundColor = secondaryColor,
            radius = dotRadius,
            dotStyle = dotStyle
        )
    }
}

@Composable
fun OnDraw(
    modifier: Modifier,
    color: Color,
    backgroundColor: Color,
    radius: Float,
    dotStyle: DotStyle
) {
    Canvas(
        modifier = Modifier
    ) {
        for (row in 0 until model.rows) {
            for (column in 0 until model.columns) {
                val state = model.getDotState(column, row)
                if (state == 1) {
                    continue
                }
                val offset = Offset(
                    x = coordsX[row][column].toFloat(),
                    y = coordsY[row][column].toFloat(),
                )
                when (dotStyle) {
                    DotStyle.SQUARE -> {
                        drawRect(
                            color = getColor(
                                color = color,
                                backgroundColor = backgroundColor,
                                dotState = model.getDotState(column, row)
                            ),
                            size = Size(
                                width = radius,
                                height = radius
                            ),
                            topLeft = offset
                        )
                    }
                    DotStyle.ROUND -> {
                        drawCircle(
                            color = getColor(
                                color = color,
                                backgroundColor = backgroundColor,
                                dotState = model.getDotState(column, row)
                            ),
                            radius = radius,
                            center = offset
                        )
                    }
                }
            }
        }
    }
}

private fun initialize(
    width: Int,
    height: Int,
    dotRadius: Float,
    dotSpacing: Int,
    dotStyle: DotStyle,
    isShowSeconds: Boolean,
) {
    model.apply {
        setPaddingDots(
            top = paddingRowsTop,
            left = paddingColumnsLeft,
            bottom = paddingRowsBottom,
            right = paddingColumnsRight
        )
        setFormat(if (isShowSeconds) FORMAT_SECONDS else FORMAT)
    }

    val dotSize =
        when(dotStyle) {
            DotStyle.SQUARE -> dotRadius + dotSpacing
            DotStyle.ROUND -> dotRadius * 2 + dotSpacing
        }
    val maxRows = (height / dotSize).toInt()
    val maxCols = (width / dotSize).toInt()

    initCoords(
        rows = model.rows,
        columns = model.columns,
        dotRadius = dotRadius,
        dotSpacing = dotSpacing,
        dotStyle = dotStyle,
        xOffset = (dotSize * (maxCols - model.columns) / 2).toInt(),
        yOffset = (dotSize * (maxRows - model.rows) / 2).toInt()
    )
}

private fun initCoords(
    rows: Int,
    columns: Int,
    dotRadius: Float,
    dotSpacing: Int,
    dotStyle: DotStyle,
    xOffset: Int,
    yOffset: Int
) {
    coordsX = Array(rows) { IntArray(columns) }
    coordsY = Array(rows) { IntArray(columns) }
    val rowStart = dotRadius.toInt() + dotSpacing
    var x = rowStart + xOffset
    var y = rowStart + yOffset
    val centerSpacing = when(dotStyle) {
        DotStyle.SQUARE -> dotSpacing + dotRadius.toInt()
        DotStyle.ROUND -> dotSpacing + dotRadius.toInt() * 2
    }
    for (row in 0 until rows) {
        for (column in 0 until columns) {
            coordsX[row][column] = x
            coordsY[row][column] = y
            x += centerSpacing
        }
        y += centerSpacing
        x = rowStart + xOffset
    }
}

private fun getColor(
    color: Color,
    backgroundColor: Color,
    dotState: Int
): Color {
    return if (dotState == 0) {
        backgroundColor
    } else {
        color
    }
}

enum class DotStyle(val value: String, val titleId: Int) {
    SQUARE("square", R.string.dot_style_square),
    ROUND("round", R.string.dot_style_round);

    companion object {
        fun toMap(context: Context): Map<String, String> {
            return values().associate {
                it.value to context.getString(it.titleId)
            }
        }

        fun getDefault(): DotStyle {
            return SQUARE
        }

        fun getDefaultId(): String {
            return getDefault().value
        }

        fun getById(id: String): DotStyle {
            val item = values().filter {
                it.value == id
            }
            return item[0]
        }
    }
}

private const val FORMAT = "0 0 : 0 0"
private const val FORMAT_SECONDS = "0 0 : 0 0 : 0 0"
private var model = Grid()
private lateinit var coordsX: Array<IntArray>
private lateinit var coordsY: Array<IntArray>

const val DEFAULT_SHOW_SECONDS_MC = false

const val DEFAULT_DOT_SPACING_ROUND_MC = 7F
const val DEFAULT_DOT_RADIUS_ROUND_MC = 14F
const val DEFAULT_DOT_SPACING_ROUND_SECONDS_MC = 5F
const val DEFAULT_DOT_RADIUS_ROUND_SECONDS_MC = 10F

const val DEFAULT_DOT_SPACING_SQUARE_MC = 12F
const val DEFAULT_DOT_RADIUS_SQUARE_MC = 24F
const val DEFAULT_DOT_SPACING_SQUARE_SECONDS_MC = 8F
const val DEFAULT_DOT_RADIUS_SQUARE_SECONDS_MC = 16F
