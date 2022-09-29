package com.escapeindustries.dotmatrix

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MatrixDisplay(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    color: Color = MaterialTheme.colors.primary,
    backgroundColor: Color = MaterialTheme.colors.background,
    hour: Int,
    minute: Int,
    second: Int
) {
    initialize()

    val value = hour.toString() + ":"+ "%02d".format(minute)
    //val value = hour.toString() + ":"+ "%02d".format(minute) + ":" + "%02d".format(second)

    Row (
        modifier = modifier
           .padding(
               horizontal = 20.dp,
               vertical = 100.dp
           )
           .fillMaxSize()
    ) {
        OnDraw(
            modifier = modifier
                .background(backgroundColor),
            color = color,
            backgroundColor = backgroundColor,
            value = value
        )
    }
}

@Composable
fun OnDraw(
    modifier: Modifier,
    color: Color,
    backgroundColor: Color,
    value: String
) {
    Canvas(
        modifier = Modifier
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
                    y = coordsY[row][column].toFloat(),
                )
                drawCircle(
                    color = getColor(
                        color = color,
                        backgroundColor = backgroundColor,
                        dotState = model.getDotState(column, row)),
                    radius = dotRadius.toFloat(),
                    center = offset
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

private var model = Grid()
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
    initCoords(
        rows = model.rows,
        columns = model.columns,
        dotRadius = DEFAULT_DOT_RADIUS,
        spaceBetweenDots = DEFAULT_DOT_SPACING
    )
}

private fun initCoords(
    rows: Int,
    columns: Int,
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
            coordsX[row][column] = x
            coordsY[row][column] = y
            x += centerSpacing
        }
        y += centerSpacing
        x = rowStart
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