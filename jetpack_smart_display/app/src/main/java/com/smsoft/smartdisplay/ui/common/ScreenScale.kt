package com.smsoft.smartdisplay.ui.common

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import java.util.Locale
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Adapts the UI to a small physical screen. A 4 inch square 480 by 480 panel (120 px per inch, a
 * diagonal of about 5.7 inch) has controls that are too small to touch: on such a screen the density
 * is scaled up, so every dp and sp is larger and the layout is as wide as a
 * [SMALL_SCREEN_DESIGN_WIDTH_DP] screen. The same design is used everywhere, only larger. Screens
 * with a diagonal of [SMALL_SCREEN_MAX_INCHES] or more, such as the 8 inch Lenovo ThinkSmart, are
 * not changed. The scale is applied to the activity's context (see [scaledContext]), so it reaches
 * the dialogs as well as the activity's own content.
 */
const val SMALL_SCREEN_MAX_INCHES = 6.5F
const val SMALL_SCREEN_DESIGN_WIDTH_DP = 360F

/**
 * A screen whose smaller side has this many pixels or more is never scaled, whatever diagonal its
 * pixels per inch give. Panels report those as they like: an 8 inch 1280 by 800 panel that reports
 * 240 px per inch (its density instead of its 189) would come out as 6.3 inch and get the 4 inch
 * layout, far too large. The 4 inch panel has 480 px.
 */
const val SMALL_SCREEN_MAX_PX = 720

/**
 * Screens narrower than this (in dp, after the scaling above) get the compact layouts of the lists
 * and dialogs: the 4 inch panel is 360 dp wide, the 8 inch one 800 dp.
 */
const val COMPACT_SCREEN_DP = 600

/**
 * The scale of the UI: 1 on big screens, otherwise the factor that makes the width the design
 * width. [smallerSidePx] is the smaller side of the display in pixels (see [SMALL_SCREEN_MAX_PX]).
 */
fun uiScaleFor(diagonalInches: Float, widthDp: Float, smallerSidePx: Int): Float =
    if ((diagonalInches < SMALL_SCREEN_MAX_INCHES) && (smallerSidePx < SMALL_SCREEN_MAX_PX)) {
        max(1F, widthDp / SMALL_SCREEN_DESIGN_WIDTH_DP)
    } else {
        1F
    }

/**
 * The scale of the UI on the display of [context], see [uiScaleFor]. The width is the smaller
 * side of the display, so the scale is the same in both orientations.
 */
fun uiScaleOf(context: Context): Float {
    val metrics = context.resources.displayMetrics
    val (widthPx, heightPx) = realSizePx(context)
    val density = metrics.densityDpi.coerceAtLeast(1) / DisplayMetrics.DENSITY_DEFAULT.toFloat()
    val smallerSidePx = min(widthPx, heightPx)
    val diagonal = hypot(widthPx / metrics.xdpi, heightPx / metrics.ydpi)
    return uiScaleFor(diagonal, smallerSidePx / density, smallerSidePx)
}

/**
 * The whole display in pixels, system bars included: the app hides the bars and uses all of it.
 * The display metrics leave the navigation bar out on Android 8 (480 by 432 on the 480 by 480
 * panel), which gave that panel a different scale than Android 11 and later.
 */
private fun realSizePx(context: Context): Pair<Int, Int> {
    val windowManager = context.getSystemService(WindowManager::class.java)
    val metrics = context.resources.displayMetrics
    return when {
        windowManager == null -> metrics.widthPixels to metrics.heightPixels
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
            windowManager.maximumWindowMetrics.bounds.let { it.width() to it.height() }
        else -> DisplayMetrics().let { real ->
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(real)
            real.widthPixels to real.heightPixels
        }
    }
}

/**
 * One line about the display of [context] and the scale it gets, for the log: what a panel reports
 * (its density and its pixels per inch) decides the whole layout, and the panels differ.
 */
fun describeDisplay(context: Context): String {
    val metrics = context.resources.displayMetrics
    val configuration = context.resources.configuration
    val (widthPx, heightPx) = realSizePx(context)
    return "%dx%d px (app area %dx%d), %d dpi, %.1fx%.1f px/in, %dx%d dp, scale %.2f".format(
        Locale.ROOT,
        widthPx, heightPx, metrics.widthPixels, metrics.heightPixels, metrics.densityDpi, metrics.xdpi,
        metrics.ydpi, configuration.screenWidthDp, configuration.screenHeightDp, uiScaleOf(context)
    )
}

/**
 * [base] with its density scaled by [uiScaleOf], for an activity's base context. On a big screen
 * it is [base] itself.
 */
fun scaledContext(base: Context): Context {
    val scale = uiScaleOf(base)
    if (!scale.isFinite() || (scale == 1F)) {
        return base
    }
    val current = base.resources.configuration
    // Only the changed fields, so every other field keeps following the system (fontScale 0
    // leaves the user's font scale alone, as AppCompat does)
    val override = Configuration().apply {
        fontScale = 0F
        densityDpi = (current.densityDpi * scale).roundToInt()
        // The screen keeps its pixels, so it holds fewer of the larger dp
        screenWidthDp = (current.screenWidthDp / scale).roundToInt()
        screenHeightDp = (current.screenHeightDp / scale).roundToInt()
        smallestScreenWidthDp = (current.smallestScreenWidthDp / scale).roundToInt()
    }
    return base.createConfigurationContext(override)
}
