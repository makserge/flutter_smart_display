package com.smsoft.smartdisplay.ui.composable.clock.digitalclock2

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas

class Number(
    startX: Int,
    startY: Int,
    private val lineLength: Int,
    private val initialColor: Int = Color.parseColor("#66CCFF"),
    private val shadowRadius: Int,
    private val animationDuration: Int
) {
    private var margin = 2
    private val offset = 10

    private var currentStatus = 0
    private var nextStatus = currentStatus
    private var process = 0F
    private var animator: ValueAnimator? = null
    private val paint = Paint()

    private var lines = arrayOf(
        intArrayOf(startX, startY, 0),
        intArrayOf(startX, startY, 1),
        intArrayOf(startX + lineLength, startY, 1),
        intArrayOf(startX, startY + lineLength, 0),
        intArrayOf(startX, startY + lineLength, 1),
        intArrayOf(startX + lineLength, startY + lineLength, 1),
        intArrayOf(startX, startY + 2 * lineLength, 0)
    )

    init {
        paint.apply{
            isAntiAlias = true
            setShadowLayer(shadowRadius.toFloat(), 0f, 0f, color)
            color = initialColor
            strokeWidth = (lineLength / 25).toFloat()
        }
    }

    fun onDraw(
        scope: DrawScope,
    ) {
        scope.drawIntoCanvas {
            val canvas = it.nativeCanvas

            for (i in lines.indices) {
                val line = lines[i]
                if (line[2] == 0) {
                    setDrawPaint(getProcessIndex(i, 0))
                    canvas.drawLine(
                        (line[0] + margin).toFloat(),
                        line[1] + offset * (1 - getProcessIndex(i, 0)),
                        (line[0] + lineLength / 2 - margin).toFloat(),
                        line[1] + offset * (1 - getProcessIndex(i, 0)),
                        paint
                    )
                    setDrawPaint(getProcessIndex(i, 1))
                    canvas.drawLine(
                        (line[0] + lineLength / 2 + margin).toFloat(),
                        line[1] + offset * (1 - getProcessIndex(i, 1)) / 2,
                        (line[0] + lineLength - margin).toFloat(),
                        line[1] + offset * (1 - getProcessIndex(i, 1)) / 2,
                        paint
                    )
                } else {
                    setDrawPaint(getProcessIndex(i, 0))
                    canvas.drawLine(
                        line[0] + offset * (1 - getProcessIndex(i, 0)),
                        (line[1] + margin).toFloat(),
                        line[0] + offset * (1 - getProcessIndex(i, 0)),
                        (line[1] + lineLength / 2 - margin).toFloat(),
                        paint
                    )
                    setDrawPaint(getProcessIndex(i, 1))
                    canvas.drawLine(
                        line[0] + offset * (1 - getProcessIndex(i, 1)) / 2,
                        (line[1] + lineLength / 2 + margin).toFloat(),
                        line[0] + offset * (1 - getProcessIndex(i, 1)) / 2,
                        (line[1] + lineLength - margin).toFloat(),
                        paint
                    )
                }
            }
        }
    }

    private fun setDrawPaint(process: Float) {
        paint.alpha = (35 + 220 * process).toInt()
        margin = 2 + ((1 - process) * 10).toInt()
    }

    private fun getProcessIndex(lineIndex: Int, subLineIndex: Int): Float {
        var trueProcess = if (subLineIndex == 1) process - 0.2F else process
        if (trueProcess > 1.0F) trueProcess = 1.0F
        if (trueProcess < 0F) trueProcess = 0F
        val from = numbers[currentStatus][lineIndex]
        val to = numbers[nextStatus][lineIndex]
        trueProcess = from + (to - from) * trueProcess
        return trueProcess
    }

    fun updateNumber(status: Int) {
        var newStatus = status
        newStatus %= 10
        nextStatus = newStatus
        if (nextStatus != currentStatus) {
            animator = if ((animator != null) && (animator!!.isRunning)) {
                animator!!.end()
                ValueAnimator.ofFloat(1.2F - process, 1.2F).setDuration(animationDuration.toLong())
            } else {
                ValueAnimator.ofFloat(0F, 1.2F).setDuration(animationDuration.toLong())
            }
            animator!!.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    currentStatus = nextStatus
                }
            })
            animator!!.addUpdateListener { valueAnimator: ValueAnimator ->
                process = valueAnimator.animatedValue as Float
            }
            animator!!.start()
        }
    }
}