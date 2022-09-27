package com.xenione.digit

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import com.xenione.digit.MatrixHelper.rotateX
import com.xenione.digit.MatrixHelper.translate

/**
 * Created by Eugeni on 16/10/2016.
 */
class TabDigit : View, Runnable {
    /*
     * false: rotate upwards
     * true: rotate downwards
     */
    private var reverseRotation = true
    private var topTab = Tab()
    private var bottomTab = Tab()
    private var middleTab = Tab()
    private val tabs: MutableList<Tab> = ArrayList(3)
    private lateinit var tabAnimation: AbstractTabAnimation
    private val projectionMatrix = Matrix()
    private var cornerSize = -1
    private var numberPaint = Paint()
    private var dividerPaint = Paint()
    private var backgroundPaint = Paint()
    private val textMeasured = Rect()
    private var padding = 16
    private var BACKGROUND_COLOR = "#2C2C2C"

    var chars = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
            super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        initPaints()
        numberPaint.color = -1
        backgroundPaint.color = Color.parseColor(BACKGROUND_COLOR)
        initTabs()
    }

    private fun initPaints() {
        numberPaint.apply {
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            color = Color.WHITE
        }
        dividerPaint.apply {
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            color = Color.WHITE
            strokeWidth = 1F
        }
        backgroundPaint.apply {
            isAntiAlias = true
            color = Color.BLACK
        }
    }

    private fun initTabs() {
        // top Tab
        topTab.rotate(180)
        tabs.add(topTab)

        // bottom Tab
        tabs.add(bottomTab)

        // middle Tab
        middleTab = Tab()
        tabs.add(middleTab)
        tabAnimation =
            if (reverseRotation) TabAnimationDown(topTab, bottomTab, middleTab)
            else TabAnimationUp(topTab, bottomTab, middleTab)
        tabAnimation.initMiddleTab()
        setInternalChar(0)
    }

    fun setChar(index: Int) {
        setInternalChar(index)
        invalidate()
    }

    private fun setInternalChar(index: Int) {
        for (tab in tabs) {
            tab.setChar(index)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        calculateTextSize(textMeasured)
        measureTabs(textMeasured.width() + padding, textMeasured.height() + padding)
        val resolvedWidth = resolveSize(middleTab.maxWith(), widthMeasureSpec)
        val resolvedHeight = resolveSize(2 * middleTab.maxHeight(), heightMeasureSpec)
        setMeasuredDimension(resolvedWidth, resolvedHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw || h != oldh) {
            setupProjectionMatrix()
        }
    }

    private fun setupProjectionMatrix() {
        projectionMatrix.reset()
        translate(projectionMatrix, width / 2F, -height / 2F, 0F)
    }

    private fun measureTabs(width: Int, height: Int) {
        for (tab in tabs) {
            tab.measure(width, height)
        }
    }

    private fun drawTabs(canvas: Canvas) {
        for (tab in tabs) {
            tab.draw(canvas)
        }
    }

    private fun drawDivider(canvas: Canvas) {
        canvas.apply{
            save()
            concat(projectionMatrix)
            drawLine(-width / 2F, 0F, width / 2F, 0F, dividerPaint)
            restore()
        }
    }

    private fun calculateTextSize(rect: Rect) {
        numberPaint.getTextBounds("8", 0, 1, rect)
    }

    var textSize: Int
        get() = numberPaint.textSize.toInt()
        set(size) {
            numberPaint.textSize = size.toFloat()
            requestLayout()
        }

    fun setPadding(padding: Int) {
        this.padding = padding
        requestLayout()
    }

    fun setDividerColor(color: Int) {
        dividerPaint.color = color
    }

    fun getPadding(): Int {
        return padding
    }

    var textColor = 0
        set(value) {
            numberPaint.color = value
            field = value
        }

    fun setCornerSize(cornerSize: Int) {
        this.cornerSize = cornerSize
        invalidate()
    }

    fun getCornerSize(): Int {
        return cornerSize
    }

    override fun setBackgroundColor(color: Int) {
        backgroundPaint.color = color
    }

    fun getBackgroundColor(): Int {
        return backgroundPaint.color
    }

    fun start() {
        tabAnimation.start()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        drawTabs(canvas)
        drawDivider(canvas)
        ViewCompat.postOnAnimationDelayed(this, this, 40)
    }

    override fun run() {
        tabAnimation.run()
        invalidate()
    }

    fun sync() {
        tabAnimation.sync()
        invalidate()
    }

    inner class Tab {
        private val modelViewMatrix = Matrix()
        private val modelViewProjectionMatrix = Matrix()
        private val rotationModelViewMatrix = Matrix()
        private val startBounds = RectF()
        private val endBounds = RectF()
        private var currIndex = 0
        private var alpha = 0
        private val measuredMatrixHeight = Matrix()
        private val measuredMatrixWidth = Matrix()

        fun measure(width: Int, height: Int) {
            val area = Rect(-width / 2, 0, width / 2, height / 2)
            startBounds.set(area)
            endBounds.set(area)
            endBounds.offset(0F, -height / 2F)
        }

        fun maxWith(): Int {
            val rect = RectF(startBounds)
            val projectionMatrix = Matrix()
            translate(projectionMatrix, startBounds.left, -startBounds.top, 0F)
            measuredMatrixWidth.apply {
                reset()
                setConcat(projectionMatrix, MatrixHelper.ROTATE_X_90)
                mapRect(rect)
            }
            return rect.width().toInt()
        }

        fun maxHeight(): Int {
            val rect = RectF(startBounds)
            val projectionMatrix = Matrix()
            measuredMatrixHeight.apply {
                reset()
                setConcat(projectionMatrix, MatrixHelper.ROTATE_X_0)
                mapRect(rect)
            }
            return rect.height().toInt()
        }

        fun setChar(index: Int) {
            currIndex = if (index > chars.size) 0 else index
        }

        operator fun next() {
            currIndex++
            if (currIndex >= chars.size) {
                currIndex = 0
            }
        }

        fun rotate(alpha: Int) {
            this.alpha = alpha
            rotateX(rotationModelViewMatrix, alpha)
        }

        fun draw(canvas: Canvas) {
            drawBackground(canvas)
            drawText(canvas)
        }

        private fun drawBackground(canvas: Canvas) {
            canvas.save()
            modelViewMatrix.set(rotationModelViewMatrix)
            applyTransformation(canvas, modelViewMatrix)
            canvas.apply{
                drawRoundRect(startBounds, cornerSize.toFloat(), cornerSize.toFloat(), backgroundPaint)
                restore()
            }
        }

        private fun drawText(canvas: Canvas) {
            canvas.save()
            modelViewMatrix.set(rotationModelViewMatrix)
            var clip = startBounds
            if (alpha > 90) {
                modelViewMatrix.setConcat(modelViewMatrix, MatrixHelper.MIRROR_X)
                clip = endBounds
            }
            applyTransformation(canvas, modelViewMatrix)
            canvas.apply{
                clipRect(clip)
                drawText(
                    chars[currIndex].toString(),
                    0,
                    1,
                    -textMeasured.centerX().toFloat(),
                    -textMeasured.centerY().toFloat(),
                    numberPaint
                )
                restore()
            }
        }

        private fun applyTransformation(canvas: Canvas, matrix: Matrix) {
            modelViewProjectionMatrix.apply {
                reset()
                setConcat(projectionMatrix, matrix)
                canvas.concat(this)
            }
        }
    }
}