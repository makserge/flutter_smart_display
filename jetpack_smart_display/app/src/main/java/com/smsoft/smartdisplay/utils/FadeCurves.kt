package com.smsoft.smartdisplay.utils

import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

/*
 * Fade curves for the alarm: the sound fade-in (VolumeFader) and the wake-up light (AlarmWakeLight).
 * Both used to be linear in the raw value, which neither ears nor eyes perceive as even: a linear
 * ramp seems to jump up in its first seconds and then hardly change. These curves are even in
 * what is perceived, so equal time steps give equal perceived steps. Pure functions, no Android.
 */

/** How far a fade of [durationMs] is, [elapsedMs] after its start: 0..1. */
fun fadeProgress(elapsedMs: Long, durationMs: Long): Float =
    if (durationMs <= 0L) 1F else (elapsedMs.toFloat() / durationMs).coerceIn(0F, 1F)

/**
 * The player volume [progress] (0..1) into a fade from [fromVolume] to [toVolume].
 *
 * Player.volume is a linear gain on the samples, but loudness follows the level in decibels, so
 * the level changes by the same number of dB per time step (the crescendo of AOSP DeskClock).
 * Silence has no level in dB: it counts as [VOLUME_FADE_RANGE_DB] below the louder end. A fade in
 * from silence is therefore silent at 0, jumps to -40 dB of the target at its first step, about
 * the noise floor of a quiet bedroom for an alarm-loud target, and rises by 40 dB / 15 s =
 * 0.27 dB per 100 ms step, well below the ~1 dB a listener can tell apart. The shape in dB does
 * not depend on the target volume. A fade down (or a fade to silence) is the same curve backwards.
 * The ends are exact: [fromVolume] at 0, [toVolume] at 1.
 */
fun fadeVolume(fromVolume: Float, toVolume: Float, progress: Float): Float {
    if (progress <= 0F) {
        return fromVolume
    }
    if (progress >= 1F) {
        return toVolume
    }
    val loudest = max(fromVolume, toVolume)
    if (loudest <= 0F) {
        return 0F
    }
    val fromDb = levelDb(fromVolume / loudest)
    val toDb = levelDb(toVolume / loudest)
    return loudest * 10F.pow((fromDb + (toDb - fromDb) * progress) / 20F)
}

/** [gain] relative to the louder end of a fade in dB, no lower than -[VOLUME_FADE_RANGE_DB]. */
private fun levelDb(gain: Float): Float =
    if (gain <= 0F) -VOLUME_FADE_RANGE_DB else max(20F * log10(gain), -VOLUME_FADE_RANGE_DB)

/**
 * The relative luminance 0..1 of the wake-up light [progress] (0..1) into its fade.
 *
 * A dimmer level is a PWM duty cycle, i.e. luminance, but perceived brightness follows CIE 1976
 * lightness L* (0..100, made so that equal L* steps look equal; roughly the cube root of
 * luminance). So L* goes up linearly with the progress and the luminance is its inverse:
 * ((L* + 16) / 116)^3 above L* = 8, and L* / 903.3 below, where the CIE curve is linear.
 */
fun lightFadeLuminance(progress: Float): Float {
    val lightness = 100F * progress.coerceIn(0F, 1F)
    return if (lightness > CIE_LIGHTNESS_KNEE) {
        ((lightness + 16F) / 116F).pow(3)
    } else {
        lightness / CIE_KAPPA
    }
}

/**
 * The dimmer level for [progress] (0..1) into the wake-up light fade: an integer percent, the
 * format the dimmer gets everywhere else (DimmerLightController, LightBrightnessType). Along the
 * L* curve it is 1 % at 4.5 % of the fade, 18 % at half and 100 % at the end.
 */
fun lightFadePercent(progress: Float): Int =
    (100F * lightFadeLuminance(progress)).roundToInt().coerceIn(0, 100)

// The range of the volume fade. AOSP DeskClock uses 40 dB too. A wider range spends the start of
// the fade inaudible (60 dB: the first third of 15 s is more than 40 dB below the target); a
// narrower one starts audibly loud.
private const val VOLUME_FADE_RANGE_DB = 40F

// CIE 1976: kappa = 24389/27 (~903.3) is the slope of the linear part of L*, and L* = 8
// (= kappa * epsilon, epsilon = 216/24389) is where it meets the cube-root part.
private const val CIE_KAPPA = 24389F / 27F
private const val CIE_LIGHTNESS_KNEE = 8F
