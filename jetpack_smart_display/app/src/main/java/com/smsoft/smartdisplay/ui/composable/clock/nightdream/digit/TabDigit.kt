package com.smsoft.smartdisplay.ui.composable.clock.nightdream.digit

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.digit.MatrixHelper.rotateX
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.digit.MatrixHelper.translate
import kotlin.math.ceil
import kotlin.math.max

/**
 * Created by Eugeni on 16/10/2016.
 */
class TabDigit : View, Runnable {
    private val DEFAULT_BACKGROUND_COLOR = "#2C2C2C"

    /*
     * false: rotate upwards
     * true: rotate downwards
     */
    var reverseRotation = true
        set (value) {
            if (field == value) return
            field = value
            // The direction is built into the animation object, so it has to be replaced
            tabAnimation = createTabAnimation()
            setChar(shown)
        }

    var cornerSize = 0F
        set (value) {
            if (field == value) return
            field = value
            invalidate()
        }
    var background = Color.parseColor(DEFAULT_BACKGROUND_COLOR)
        set (value) {
            if (field == value) return
            backgroundPaint.color = value
            field = value
            invalidate()
        }
    var dividerColor = Color.WHITE
        set (value) {
            if (field == value) return
            dividerPaint.color = value
            field = value
            invalidate()
        }
    var padding = 16F
        set (value) {
            if (field == value) return
            field = value
            requestLayout()
            invalidate()
        }
    var textSize: Float
        get() = numberPaint.textSize
        set(size) {
            if (numberPaint.textSize == size) return
            numberPaint.textSize = size
            requestLayout()
            invalidate()
        }

    var textColor = 0
        set(value) {
            numberPaint.color = value
            field = value
            invalidate()
        }

    // Own camera: its distance is set from the card height, see onMeasure
    private val camera = Camera()
    private var topTab = Tab()
    private var bottomTab = Tab()
    private var middleTab = Tab()
    private val tabs: MutableList<Tab> = ArrayList(3)
    private lateinit var tabAnimation: AbstractTabAnimation
    private val projectionMatrix = Matrix()
    private var numberPaint = Paint()
    private var dividerPaint = Paint()
    private var backgroundPaint = Paint()
    private val textMeasured = Rect()

    // Index of the character shown, or being flipped to
    private var shown = 0

    var chars = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) :
            super(
                context,
                attrs,
                defStyleAttr
            ) {
        init()
    }

    private fun init() {
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
            color = Color.parseColor(DEFAULT_BACKGROUND_COLOR)
        }
        // top Tab
        topTab.rotate(180)
        tabs.add(topTab)

        // bottom Tab
        tabs.add(bottomTab)

        // middle Tab
        middleTab = Tab()
        tabs.add(middleTab)
        tabAnimation = createTabAnimation()
        setInternalChar(0)
    }

    private fun createTabAnimation(): AbstractTabAnimation {
        val animation =
            if (reverseRotation) {
                TabAnimationDown(
                    topTab = topTab,
                    bottomTab = bottomTab,
                    middleTab = middleTab
                )
            }
            else {
                TabAnimationUp(
                    topTab = topTab,
                    bottomTab = bottomTab,
                    middleTab = middleTab
                )
            }
        animation.initMiddleTab()
        return animation
    }

    /**
     * Shows the character at [index] at once, without a flip.
     */
    fun setChar(index: Int) {
        shown = if (index in chars.indices) index else 0
        tabAnimation.reset()
        setInternalChar(shown)
        invalidate()
    }

    /**
     * Shows the character at [index]. Only a single step to the next character is animated. Any
     * other change (the screen was off, the time was set, a flip is still running) jumps straight
     * to it, so the digit always ends up on the right character.
     */
    fun flipTo(index: Int) {
        if (index == shown) {
            return
        }
        if (!tabAnimation.isRunning && index == (shown + 1) % chars.size) {
            shown = index
            start()
        } else {
            setChar(index)
        }
    }

    private fun setInternalChar(index: Int) {
        for (tab in tabs) {
            tab.setChar(index)
        }
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        calculateTextSize(textMeasured)
        val tabWidth = textMeasured.width() + padding.toInt()
        val tabHeight = max(1, textMeasured.height() + padding.toInt())
        // The camera distance grows with the card, so the flip keeps the same perspective at any
        // size. With the fixed default distance big cards bulge out and from ~1150 px degenerate.
        camera.setLocation(
            0F,
            0F,
            -CAMERA_DISTANCE_RATIO * tabHeight / PIXELS_PER_INCH
        )
        dividerPaint.strokeWidth = max(1F, textSize / 400F)
        measureTabs(
            width = tabWidth,
            height = tabHeight
        )
        val resolvedWidth = resolveSize(
            ceil(tabWidth * SLOT_WIDTH_RATIO).toInt(),
            widthMeasureSpec
        )
        val resolvedHeight = resolveSize(
            ceil(tabHeight * SLOT_HEIGHT_RATIO).toInt(),
            heightMeasureSpec
        )
        setMeasuredDimension(
            resolvedWidth,
            resolvedHeight
        )
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {
        if (w != oldw || h != oldh) {
            setupProjectionMatrix()
        }
    }

    private fun setupProjectionMatrix() {
        projectionMatrix.reset()
        translate(
            projectionMatrix,
            width / 2F,
            -height / 2F,
            0F
        )
    }

    private fun measureTabs(
        width: Int,
        height: Int
    ) {
        for (tab in tabs) {
            tab.measure(
                width,
                height
            )
        }
    }

    private fun drawTabs(canvas: Canvas) {
        for (tab in tabs) {
            tab.draw(canvas)
        }
    }

    private fun drawDivider(canvas: Canvas) {
        // Across the card only: the view is wider than the card to leave room for the flip
        val halfWidth = (textMeasured.width() + padding.toInt()) / 2F
        canvas.apply{
            save()
            concat(projectionMatrix)
            drawLine(
                -halfWidth,
                0F,
                halfWidth,
                0F,
                dividerPaint
            )
            restore()
        }
    }

    private fun calculateTextSize(rect: Rect) {
        measureDigits(
            paint = numberPaint,
            rect = rect
        )
    }

    private fun start() {
        tabAnimation.start()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        drawTabs(canvas)
        drawDivider(canvas)
        // Step the animation once per frame while a flip runs; an idle digit is not redrawn at all
        if (tabAnimation.isRunning) {
            removeCallbacks(this)
            postOnAnimation(this)
        }
    }

    override fun run() {
        tabAnimation.run()
        invalidate()
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(this)
        super.onDetachedFromWindow()
    }

    inner class Tab {
        private val modelViewMatrix = Matrix()
        private val modelViewProjectionMatrix = Matrix()
        private val rotationModelViewMatrix = Matrix()
        private val startBounds = RectF()
        private val endBounds = RectF()
        private var currIndex = 0
        private var alpha = 0

        fun measure(width: Int, height: Int) {
            val area = Rect(
                -width / 2,
                0,
                width / 2,
                height / 2
            )
            startBounds.set(area)
            endBounds.set(area)
            endBounds.offset(
                0F,
                -height / 2F
            )
        }

        fun setChar(index: Int) {
            currIndex = if (index in chars.indices) index else 0
        }

        operator fun next() {
            currIndex++
            if (currIndex >= chars.size) {
                currIndex = 0
            }
        }

        fun rotate(alpha: Int) {
            this.alpha = alpha
            rotateX(
                camera,
                rotationModelViewMatrix,
                alpha
            )
        }

        // Bounds of the digit being drawn, reused for every frame
        private val charBounds = Rect()

        fun draw(canvas: Canvas) {
            drawBackground(canvas)
            drawText(canvas)
        }

        private fun drawBackground(canvas: Canvas) {
            canvas.save()
            modelViewMatrix.set(rotationModelViewMatrix)
            applyTransformation(
                canvas,
                modelViewMatrix
            )
            canvas.apply{
                drawRoundRect(
                    startBounds,
                    cornerSize,
                    cornerSize,
                    backgroundPaint
                )
                restore()
            }
        }

        private fun drawText(canvas: Canvas) {
            canvas.save()
            modelViewMatrix.set(rotationModelViewMatrix)
            var clip = startBounds
            if (alpha > 90) {
                modelViewMatrix.setConcat(
                    modelViewMatrix,
                    MatrixHelper.MIRROR_X
                )
                clip = endBounds
            }
            applyTransformation(
                canvas,
                modelViewMatrix
            )
            canvas.apply{
                clipRect(clip)
                // Centred on its own glyph across the card: placed at the centre of the box around
                // all digits, a narrow "1" sat right of centre. Vertically all digits keep the same
                // baseline.
                val char = chars[currIndex].toString()
                numberPaint.getTextBounds(char, 0, 1, charBounds)
                drawText(
                    char,
                    0,
                    1,
                    -charBounds.exactCenterX(),
                    -textMeasured.centerY().toFloat(),
                    numberPaint
                )
                restore()
            }
        }

        private fun applyTransformation(
            canvas: Canvas,
            matrix: Matrix
        ) {
            modelViewProjectionMatrix.apply {
                reset()
                setConcat(
                    projectionMatrix,
                    matrix
                )
                canvas.concat(this)
            }
        }
    }

    companion object {
        // Camera distance per px of card height: the camera's default 576 px for the 342 px card
        // of the original 440 px text, so every size keeps the original look
        private const val CAMERA_DISTANCE_RATIO = 576F / 342F

        // Camera locations are given in inches of 72 px
        private const val PIXELS_PER_INCH = 72F

        /**
         * View width per card width. Half-way through a flip the tab points at the camera, so its
         * outer edge is drawn this much wider than the card (1.42).
         */
        const val SLOT_WIDTH_RATIO = CAMERA_DISTANCE_RATIO / (CAMERA_DISTANCE_RATIO - 0.5F)

        /**
         * View height per card height. Early and late in a flip the tilted tab reaches up to about
         * 2.5 % of the card height past its top or bottom edge.
         */
        const val SLOT_HEIGHT_RATIO = 1.05F

        /**
         * Size of the box around all digits per 1 px of text size, in the typeface the digits are
         * drawn with. A card is this box plus the padding.
         */
        fun digitSizeRatio(): PointF {
            val bounds = Rect()
            measureDigits(
                paint = Paint().apply {
                    textSize = 1000F
                },
                rect = bounds
            )
            return PointF(
                bounds.width() / 1000F,
                bounds.height() / 1000F
            )
        }

        /**
         * Sets [rect] to the union of the bounds of the digits 0 to 9. Every card is this wide,
         * because in most fonts "4" is wider than "8" and was cut off at the card edges.
         */
        private fun measureDigits(
            paint: Paint,
            rect: Rect
        ) {
            val digit = Rect()
            rect.setEmpty()
            for (char in '0'..'9') {
                paint.getTextBounds(char.toString(), 0, 1, digit)
                rect.union(digit)
            }
        }
    }
}