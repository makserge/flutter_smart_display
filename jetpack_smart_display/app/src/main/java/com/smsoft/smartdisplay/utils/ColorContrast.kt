package com.smsoft.smartdisplay.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

/**
 * Clock colors must stand out from what they are drawn on. The defaults come from the theme, but a
 * color picked in Settings at night (white hands, say) would vanish on the light day background,
 * and the matrix clock's teal default nearly does. Pure functions, no Android.
 */

/** The WCAG contrast ratio of two colors: 1 (same luminance) to 21 (black on white). */
fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (max(la, lb) + 0.05F) / (min(la, lb) + 0.05F)
}

/**
 * [color] if it has at least [minRatio] contrast with [background]; else the nearest mix of it
 * with black or white (whichever stands out more from [background]) that has. Alpha is kept.
 */
fun ensureContrast(color: Color, background: Color, minRatio: Float = MIN_CLOCK_CONTRAST): Color {
    val opaque = color.copy(alpha = 1F)
    if (contrastRatio(opaque, background) >= minRatio) {
        return color
    }
    val extreme = if (contrastRatio(Color.Black, background) >= contrastRatio(Color.White, background)) {
        Color.Black
    } else {
        Color.White
    }
    var fraction = 0F
    var mixed = opaque
    while ((fraction < 1F) && (contrastRatio(mixed, background) < minRatio)) {
        fraction = (fraction + MIX_STEP).coerceAtMost(1F)
        mixed = lerp(opaque, extreme, fraction)
    }
    return mixed.copy(alpha = color.alpha)
}

// WCAG's minimum for graphics and large text: hands, ticks and big digits.
const val MIN_CLOCK_CONTRAST = 3F
private const val MIX_STEP = 0.05F
