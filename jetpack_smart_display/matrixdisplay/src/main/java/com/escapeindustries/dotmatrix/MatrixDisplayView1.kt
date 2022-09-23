/**
 * A [View][android.view.View] that displays digits, spaces and colons by
 * lighting and dimming dots on a 2D grid of dots. The dot colors animate to
 * give a sense of phopher persistence on an old-school LED clock display.
 * @author Mark Roberts
 */
package com.escapeindustries.dotmatrix

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.compose.ui.graphics.toArgb

internal class MatrixDisplayView1 : View {
    private val paddingRowsTop = DEFAULT_PADDING
    private val paddingColumnsLeft = DEFAULT_PADDING
    private val paddingRowsBottom = DEFAULT_PADDING
    private val paddingColumnsRight = DEFAULT_PADDING
    private val dotRadius = DEFAULT_DOT_RADIUS
    private val dotSpacing = DEFAULT_DOT_SPACING
    private val backgroundColor = DEFAULT_BACKGROUND_COLOR
    private var litColor = DEFAULT_DIGIT_COLOR
    private val dimColor = DEFAULT_BACKGROUND_COLOR
    private val format = DEFAULT_FORMAT
    private var model = ModelGrid()
    private var coordsX: Array<IntArray>? = null
    private var coordsY: Array<IntArray>? = null
    private var viewWidth = 0
    private var viewHeight = 0

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initialize()
    }

    private fun initialize() {
        model.apply {
            setPaddingDots(
                paddingRowsTop, paddingColumnsLeft,
                paddingRowsBottom, paddingColumnsRight
            )
            setFormat(format)
        }
        initCoords(model.rows, model.columns, dotRadius, dotSpacing)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            getAppropriateSize(widthMeasureSpec, viewWidth),
            getAppropriateSize(heightMeasureSpec, viewHeight)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(backgroundColor)
        for (row in 0 until model.rows) {
            for (column in 0 until model.columns) {
                val state = model.getDotState(column, row)
                if (state == 1) {
                    continue
                }
                canvas.drawCircle(
                    coordsX!![row][column].toFloat(),
                    coordsY!![row][column].toFloat(),
                    dotRadius.toFloat(),
                    getPaint(model.getDotState(column, row))
                )
            }
        }
    }

    private fun getPaint(dotState: Int): Paint {
        val color = if (dotState == 0) {
            dimColor
        } else {
            litColor
        }
        val paint = Paint()
        paint.color = color
        return paint
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
                coordsX!![row][column] = x
                coordsY!![row][column] = y
                x += centerSpacing
            }
            y += centerSpacing
            x = rowStart
        }
        viewWidth = (coordsX!![model.rows - 1][model.columns - 1] + dotRadius + dotSpacing)
        viewHeight = (coordsY!![model.rows - 1][model.columns - 1] + dotRadius + dotSpacing)
    }

    private fun getAppropriateSize(sizeMeasureSpec: Int, sizePreferred: Int): Int {
        var size = sizePreferred
        val mode = MeasureSpec.getMode(sizeMeasureSpec)
        if (mode == MeasureSpec.EXACTLY || mode == MeasureSpec.AT_MOST && sizePreferred > MeasureSpec.getSize(sizeMeasureSpec)) {
            size = MeasureSpec.getSize(sizeMeasureSpec)
        }
        return size
    }

    fun setPrimaryColor(color: androidx.compose.ui.graphics.Color) {
        litColor = color.toArgb()
    }

    fun setTime(
        hour: Int,
        minute: Int,
        second: Int) {
       // val value = hour.toString() + ":"+ "%02d".format(minute) + ":" + "%02d".format(second)
        val value = hour.toString() + ":"+ "%02d".format(minute)
        model.setValue(value)
        invalidate()
    }

    companion object {
        private const val DEFAULT_PADDING = 0
        private const val DEFAULT_DOT_SPACING = 7
        private const val DEFAULT_DOT_RADIUS = 14
        private const val DEFAULT_DIGIT_COLOR = Color.WHITE
        private const val DEFAULT_BACKGROUND_COLOR = Color.BLACK
        //private const val DEFAULT_FORMAT = "0 0 : 0 0 : 0 0"
        private const val DEFAULT_FORMAT = "0 0 : 0 0"
    }
}