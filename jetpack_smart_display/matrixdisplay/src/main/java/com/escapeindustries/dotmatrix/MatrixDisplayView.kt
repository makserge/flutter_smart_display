package com.escapeindustries.dotmatrix

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun MatrixDisplayView(
    modifier: Modifier = Modifier,
    color: Color,
    hour: Int,
    minute: Int,
    second: Int
) {
    initialize()

    val value = hour.toString() + ":"+ "%02d".format(minute)
    //val value = hour.toString() + ":"+ "%02d".format(minute) + ":" + "%02d".format(second)

    OnDraw(
        modifier = modifier,
        color = color,
        value = value
    )
}

@Composable
fun OnDraw(
    modifier: Modifier,
    color: Color,
    value: String
) {
    Canvas(
        modifier = modifier
    ) {
        model.setValue(value)

        drawCircle(backgroundColor)
        for (row in 0 until model.rows) {
            for (column in 0 until model.columns) {
                val state = model.getDotState(column, row)
                if (state == 1) {
                    continue
                }
                val offset = Offset(
                    x = coordsX[row][column].toFloat(),
                    y= coordsY[row][column].toFloat(),
                )
                drawCircle(
                    getColor(color, model.getDotState(column, row)),
                    dotRadius.toFloat(),
                    offset
                )
            }
        }
    }
}

//private const val FORMAT = "0 0 : 0 0 : 0 0"
//private const val X_OFFSET = 700
//private const val Y_OFFSET = 150
//private const val DEFAULT_DOT_SPACING = 5
//private const val DEFAULT_DOT_RADIUS = 10

private const val FORMAT = "0 0 : 0 0"
private const val X_OFFSET = 590
private const val Y_OFFSET = 250
private const val DEFAULT_DOT_SPACING = 7
private const val DEFAULT_DOT_RADIUS = 14

private const val dotRadius = DEFAULT_DOT_RADIUS

private var model = ModelGrid()
private val backgroundColor = Color.Black
private lateinit var coordsX: Array<IntArray>
private lateinit var coordsY: Array<IntArray>

private fun initialize() {
    model.apply {
        setPaddingDots(
            paddingRowsTop, paddingColumnsLeft,
            paddingRowsBottom, paddingColumnsRight
        )
        setFormat(FORMAT)
    }
    initCoords(model.rows, model.columns, dotRadius, DEFAULT_DOT_SPACING)
}

private fun initCoords(
    rows: Int, columns: Int,
    dotRadius: Int,
    spaceBetweenDots: Int
) {
    coordsX = Array(rows) { IntArray(columns) }
    coordsY = Array(rows) { IntArray(columns) }
    val rowStart = dotRadius + spaceBetweenDots
    var x = rowStart
    var y = rowStart
    val centerSpacing = spaceBetweenDots + dotRadius * 2
    for (row in 0 until rows) {
        for (column in 0 until columns) {
            coordsX[row][column] = x - X_OFFSET
            coordsY[row][column] = y - Y_OFFSET
            x += centerSpacing
        }
        y += centerSpacing
        x = rowStart
    }
}

private fun getColor(color: Color, dotState: Int): Color {
    return if (dotState == 0) {
        backgroundColor
    } else {
        color
    }
}