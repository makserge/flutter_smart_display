package com.smsoft.smartdisplay.utils

import kotlin.math.ln
import kotlin.math.pow

/**
 * A volume slider that is even in loudness: the slider position (0..1) changes the level by the
 * same number of decibels per step, from [minVolume] at 0 to full volume (gain 1) at 1. A slider
 * over the raw gain put most of its travel into the loudest few dB: from 0.1 to 1 it was already
 * at -5 dB in the middle, so the whole lower half covered 15 dB and the upper half 5 dB.
 * With [isMuteAtZero], position 0 is silence (gain 0) instead of [minVolume]; every other
 * position stays on the curve, so the first step up goes from silence to just above [minVolume].
 * Callers keep the raw gain (saved settings, the player); only the slider works in positions.
 * Pure functions, no Android.
 */
class LoudnessScale(
    private val minVolume: Float,
    private val isMuteAtZero: Boolean = false,
) {
    init {
        require((minVolume > 0F) && (minVolume < 1F)) { "minVolume must be in (0, 1): $minVolume" }
    }

    /**
     * The gain for slider [position] (0..1): [minVolume] at 0 (silence with [isMuteAtZero]),
     * 1 at 1, even in dB between.
     */
    fun volume(position: Float): Float {
        val clamped = position.coerceIn(0F, 1F)
        return if (isMuteAtZero && (clamped <= 0F)) 0F else minVolume.pow(1F - clamped)
    }

    /**
     * The slider position of [volume]; a volume outside [minVolume]..1 sits at that end. With
     * [isMuteAtZero] that end is silence, so a gain below [minVolume] (set from outside) is 0.
     */
    fun position(volume: Float): Float =
        (1F - ln(volume.coerceIn(minVolume, 1F)) / ln(minVolume)).coerceIn(0F, 1F)
}

/**
 * The volume sliders in Settings (alarm, timer, message alert, voice chime): gain 0.1 (-20 dB,
 * the old minimum of their raw-gain sliders) to 1, 2 dB per tenth. The old range 0.1..1 covers
 * the positions 0..1 exactly, so every saved volume keeps its gain.
 */
val VOLUME_SETTING_SCALE = LoudnessScale(minVolume = 0.1F)
