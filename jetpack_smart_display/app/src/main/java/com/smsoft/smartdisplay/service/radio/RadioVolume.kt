package com.smsoft.smartdisplay.service.radio

import com.smsoft.smartdisplay.utils.LoudnessScale
import kotlin.math.roundToInt

/**
 * The radio volume as the radio page and the voice commands see it: a slider position 0..1 that
 * is even in loudness, shown and stepped in whole percent (the page's slider runs 0..100).
 *
 * - Internal player: Player.volume is a linear gain on the samples, so the position goes through
 *   [INTERNAL_SCALE]: 0 is silence, then -39.6 dB (1 %) to 0 dB (100 %), 0.4 dB per percent. Over the
 *   raw gain the voice step of 5 % was +6 dB near the bottom and +0.4 dB near the top.
 * - MPD: the position is MPD's `setvol` percent / 100, as before. MPD's software, ALSA (with dB
 *   info), PulseAudio and PipeWire mixers already turn that percent into a loudness curve (the
 *   software mixer is within 2 dB of [INTERNAL_SCALE] over the upper three quarters), so applying
 *   [INTERNAL_SCALE] on top would squeeze the slider into its upper end (50 % would be -40 dB or
 *   less instead of about -18 dB).
 *
 * Player.volume stays the raw gain (or MPD's percent), so the media session, audio-focus ducking
 * and MPDPlayer are unchanged. The internal volume is not saved: every new ExoPlayer starts at 1.
 * Pure functions, no Android.
 */
object RadioVolume {
    /** The position of a player without a volume: MPD without a mixer, or before it answers. */
    const val UNKNOWN = -1F

    /** The radio page's slider runs 0..[PERCENT_MAX] (ComposeVerticalSlider). */
    private const val PERCENT_MAX = 100

    /** A voice volume step (INTERNET_RADIO_VOL_UP/_DOWN): 5 %, 2 dB on the internal player. */
    const val VOICE_STEP_PERCENT = 5

    // 40 dB like the alarm fade (FadeCurves) and AOSP DeskClock; 1 % is -39.6 dB, the lowest
    // level the raw-gain slider reached too (gain 0.01).
    private val INTERNAL_SCALE = LoudnessScale(minVolume = 0.01F, isMuteAtZero = true)

    /** The Player.volume for slider [position] (0..1). */
    fun toPlayerVolume(position: Float, isInternal: Boolean): Float =
        if (isInternal) INTERNAL_SCALE.volume(position) else position.coerceIn(0F, 1F)

    /** The slider position (0..1) of the Player.volume [playerVolume]. */
    fun toPosition(playerVolume: Float, isInternal: Boolean): Float =
        if (isInternal) INTERNAL_SCALE.position(playerVolume) else playerVolume.coerceIn(0F, 1F)

    /**
     * [position] as the slider shows it, 0..100. Rounded: truncation showed MPD's 53 % as 52, and
     * read about a fifth of the internal positions back one step low, so the thumb jumped back.
     * [UNKNOWN] shows as 0 (an empty slider).
     */
    fun toPercent(position: Float): Int =
        (position * PERCENT_MAX).roundToInt().coerceIn(0, PERCENT_MAX)

    /** The position of the slider value [percent] (0..100). */
    fun fromPercent(percent: Int): Float =
        percent.coerceIn(0, PERCENT_MAX).toFloat() / PERCENT_MAX

    /**
     * The position one voice step up or down from [position], on the slider's percent grid, so
     * repeated steps land on whole percents (95, 90, ...) instead of adding up float errors.
     * Down to 0 is silence, as before.
     */
    fun step(position: Float, isUp: Boolean): Float =
        fromPercent(toPercent(position) + (if (isUp) VOICE_STEP_PERCENT else -VOICE_STEP_PERCENT))
}
