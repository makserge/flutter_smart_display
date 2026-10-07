package com.smsoft.smartdisplay.ui.composable.clock.digitalclock2

import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/**
 * One seven-segment digit that morphs from the digit it shows to the next one.
 *
 * The digits and the morph progress are snapshot state that is read only while drawing, so each
 * animation frame redraws the Canvas without recomposing the clock. The geometry is passed in on
 * every draw, so nothing depends on the screen size the digit was created for.
 */
@Stable
class Number(value: Int) {
    private var fromDigit by mutableIntStateOf(segmentsOf(value))
    private var toDigit by mutableIntStateOf(segmentsOf(value))

    // 0..MORPH_END; the second half of each segment follows MORPH_LAG behind the first half
    private val progress = Animatable(MORPH_END)

    /**
     * Morphs to [value] (0-9 or [BLANK_DIGIT]); a morph that was cut short is finished first,
     * then the new one starts.
     */
    suspend fun morphTo(
        value: Int,
        durationMillis: Int
    ) {
        val digit = segmentsOf(value)
        if (digit != toDigit) {
            fromDigit = toDigit
            toDigit = digit
            progress.snapTo(0F)
        }
        val remaining = (MORPH_END - progress.value) / MORPH_END
        if (remaining > 0F) {
            // Fast start, so the new digit is readable right after the change
            progress.animateTo(
                targetValue = MORPH_END,
                animationSpec = tween(
                    durationMillis = (durationMillis * remaining).toInt(),
                    easing = LinearOutSlowInEasing
                )
            )
        }
    }

    /**
     * Draws the digit [length] wide and 2 * [length] tall with its top-left corner at [x], [y]
     * in the colour [argb], with a [glow] radius in px (0: none).
     */
    fun draw(
        canvas: Canvas,
        paint: Paint,
        x: Float,
        y: Float,
        length: Float,
        argb: Int,
        glow: Float
    ) {
        val alpha = argb ushr 24
        val from = SEGMENTS[fromDigit]
        val to = SEGMENTS[toDigit]
        val process = progress.value
        // Gaps and the shift of unlit segments were px tuned for a 426 px digit
        val unit = length / REFERENCE_LENGTH
        val halfLength = length / 2F
        for (segment in SEGMENT_X.indices) {
            val segmentX = x + SEGMENT_X[segment] * length
            val segmentY = y + SEGMENT_Y[segment] * length
            for (half in 0..1) {
                val t = (process - half * MORPH_LAG).coerceIn(0F, 1F)
                val lit = from[segment] + (to[segment] - from[segment]) * t
                paint.alpha = ((35F + 220F * lit) * alpha / 255F).toInt()
                // Only lit segments glow, fading with the morph. An opaque shadow colour would
                // give the dim unlit segments the full glow of a lit one.
                val glowAlpha = (lit * alpha).toInt()
                if ((glow > 0F) && (glowAlpha > 0)) {
                    paint.setShadowLayer(glow, 0F, 0F, (argb and 0xFFFFFF) or (glowAlpha shl 24))
                } else {
                    paint.clearShadowLayer()
                }
                val margin = unit * (2F + (1F - lit) * 10F)
                // Both halves are offset by the same amount, so an unlit segment stays straight;
                // the second half used to move half as far, which looked staggered
                val shift = unit * UNLIT_SHIFT * (1F - lit)
                val start = half * halfLength + margin
                val end = (half + 1) * halfLength - margin
                if (SEGMENT_HORIZONTAL[segment]) {
                    canvas.drawLine(segmentX + start, segmentY + shift, segmentX + end, segmentY + shift, paint)
                } else {
                    canvas.drawLine(segmentX + shift, segmentY + start, segmentX + shift, segmentY + end, paint)
                }
            }
        }
    }
}

/** A digit with every segment unlit, e.g. the tens of the hour before 10:00 in 12-hour format. */
internal const val BLANK_DIGIT = 10

// Index into SEGMENTS; BLANK_DIGIT must not become 0 like any other value % 10
private fun segmentsOf(value: Int) = if (value == BLANK_DIGIT) BLANK_DIGIT else value % 10

private const val MORPH_LAG = 0.2F
private const val MORPH_END = 1F + MORPH_LAG
private const val REFERENCE_LENGTH = 426F
// How far an unlit segment moves in, in px of the 426 px reference digit
private const val UNLIT_SHIFT = 7.5F

// Segments: top, upper left, upper right, middle, lower left, lower right, bottom.
// Start point in digit widths and whether the segment is horizontal.
private val SEGMENT_X = floatArrayOf(0F, 0F, 1F, 0F, 0F, 1F, 0F)
private val SEGMENT_Y = floatArrayOf(0F, 0F, 0F, 1F, 1F, 1F, 2F)
private val SEGMENT_HORIZONTAL = booleanArrayOf(true, false, false, true, false, false, true)

// Lit segments of the digits 0 to 9, then of BLANK_DIGIT
private val SEGMENTS = arrayOf(
    intArrayOf(1, 1, 1, 0, 1, 1, 1),
    intArrayOf(0, 0, 1, 0, 0, 1, 0),
    intArrayOf(1, 0, 1, 1, 1, 0, 1),
    intArrayOf(1, 0, 1, 1, 0, 1, 1),
    intArrayOf(0, 1, 1, 1, 0, 1, 0),
    intArrayOf(1, 1, 0, 1, 0, 1, 1),
    intArrayOf(1, 1, 0, 1, 1, 1, 1),
    intArrayOf(1, 0, 1, 0, 0, 1, 0),
    intArrayOf(1, 1, 1, 1, 1, 1, 1),
    intArrayOf(1, 1, 1, 1, 0, 1, 1),
    intArrayOf(0, 0, 0, 0, 0, 0, 0)
)
