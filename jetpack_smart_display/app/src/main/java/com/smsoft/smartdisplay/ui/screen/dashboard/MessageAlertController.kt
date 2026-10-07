package com.smsoft.smartdisplay.ui.screen.dashboard.controller

import android.os.CountDownTimer
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.smsoft.smartdisplay.data.AlarmSoundToneType
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.composable.settings.MESSAGE_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.composable.settings.MESSAGE_ENABLED_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.MESSAGE_SOUND_VOLUME_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.MESSAGE_TIMEOUT_DEFAULT
import com.smsoft.smartdisplay.ui.screen.dashboard.mqtt.DashboardMqttMessage
import com.smsoft.smartdisplay.utils.TransientAudioDucking
import com.smsoft.smartdisplay.utils.playAlarmSound
import com.smsoft.smartdisplay.utils.observe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@UnstableApi
class MessageAlertController(
    private val dataStore: DataStore<Preferences>,
    private val player: ExoPlayer,
    private val ducking: TransientAudioDucking,
    private val scope: CoroutineScope,
) {
    private val messageStateInt = MutableStateFlow<String?>(null)
    val messageState = messageStateInt.asStateFlow()

    private var enabled = MESSAGE_ENABLED_DEFAULT
    private var timeout = MESSAGE_TIMEOUT_DEFAULT
    private var soundVolume = MESSAGE_SOUND_VOLUME_DEFAULT
    private var topic = MESSAGE_DEFAULT_TOPIC

    private var offTimer: CountDownTimer? = null

    /** Follows the message settings: they used to apply only after a restart. */
    fun start() {
        dataStore.observe(scope, ::readMessageConfig) {
            enabled = it.enabled
            timeout = it.timeout
            soundVolume = it.soundVolume
            topic = it.topic
        }
    }

    fun onMqttMessage(message: DashboardMqttMessage) {
        // A retained message is replayed on every (re)subscribe; show only new messages.
        if (message.topic != topic || !enabled || message.isRetained) {
            return
        }
        messageStateInt.value = message.payload.trim()
        scope.launch {
            // Lower the radio while the alert sounds instead of pausing it for good.
            ducking.duck()
            playMessageSound()
            restartOffTimer { cancel() }
        }
    }

    fun cancel() {
        player.stop()
        offTimer?.cancel()
        messageStateInt.value = null
        ducking.release()
    }

    fun release() {
        offTimer?.cancel()
        ducking.release()
    }

    private fun playMessageSound() {
        playAlarmSound(
            player = player,
            soundToneType = AlarmSoundToneType.BARIUM,
            soundVolume = soundVolume,
            isRepeat = true
        )
    }

    private fun restartOffTimer(
        onEnd: () -> Unit
    ) {
        offTimer?.cancel()
        offTimer = object : CountDownTimer((timeout * 10000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                onEnd()
            }
        }
        offTimer!!.start()
    }
}

private data class MessageConfig(
    val enabled: Boolean,
    val timeout: Float,
    val soundVolume: Float,
    val topic: String
)

private fun readMessageConfig(data: Preferences) = MessageConfig(
    enabled = data[booleanPreferencesKey(PreferenceKey.MESSAGE_ENABLED.key)] ?: MESSAGE_ENABLED_DEFAULT,
    timeout = data[floatPreferencesKey(PreferenceKey.MESSAGE_TIMEOUT.key)] ?: MESSAGE_TIMEOUT_DEFAULT,
    soundVolume = data[floatPreferencesKey(PreferenceKey.MESSAGE_SOUND_VOLUME.key)] ?: MESSAGE_SOUND_VOLUME_DEFAULT,
    topic = data[stringPreferencesKey(PreferenceKey.MESSAGE_TOPIC.key)]?.trim() ?: MESSAGE_DEFAULT_TOPIC
)
