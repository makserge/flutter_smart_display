package com.smsoft.smartdisplay.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager

/**
 * Holds a transient "may duck" audio-focus request for as long as something short needs to be
 * heard over the radio (the voice-command listening window, an MQTT message alert).
 *
 * While it is held, the radio ExoPlayer (which handles audio focus) gets
 * AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK and lowers its volume; when it is released the radio gets
 * AUDIOFOCUS_GAIN back and restores it. Unlike starting a second focus-handling ExoPlayer, this
 * never pauses the radio. Calls are idempotent and must be made on the main thread.
 */
class TransientAudioDucking(context: Context) {
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .setOnAudioFocusChangeListener { }
        .build()
    private var isHeld = false

    fun duck() {
        if (isHeld) {
            return
        }
        isHeld = audioManager?.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    fun release() {
        if (!isHeld) {
            return
        }
        isHeld = false
        audioManager?.abandonAudioFocusRequest(request)
    }
}
