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
import com.smsoft.smartdisplay.utils.playAlarmSound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@UnstableApi
class MessageAlertController(
    private val dataStore: DataStore<Preferences>,
    private val player: ExoPlayer,
    private val scope: CoroutineScope,
) {
    private val messageStateInt = MutableStateFlow<String?>(null)
    val messageState = messageStateInt.asStateFlow()

    private var enabled = MESSAGE_ENABLED_DEFAULT
    private var timeout = MESSAGE_TIMEOUT_DEFAULT
    private var soundVolume = MESSAGE_SOUND_VOLUME_DEFAULT
    private var topic = MESSAGE_DEFAULT_TOPIC

    private var offTimer: CountDownTimer? = null

    fun start() {
        scope.launch(Dispatchers.IO) {
            val data = dataStore.data.first()
            data[booleanPreferencesKey(PreferenceKey.MESSAGE_ENABLED.key)]?.let {
                enabled = it
            }
            data[floatPreferencesKey(PreferenceKey.MESSAGE_TIMEOUT.key)]?.let {
                timeout = it
            }
            data[floatPreferencesKey(PreferenceKey.MESSAGE_SOUND_VOLUME.key)]?.let {
                soundVolume = it
            }
            data[stringPreferencesKey(PreferenceKey.MESSAGE_TOPIC.key)]?.let {
                topic = it.trim()
            }
        }
    }

    fun onMqttMessage(message: DashboardMqttMessage) {
        if (message.topic != topic || !enabled) {
            return
        }
        messageStateInt.value = message.payload.trim()
        scope.launch {
            playMessageSound()
            restartOffTimer { cancel() }
        }
    }

    fun cancel() {
        player.stop()
        offTimer?.cancel()
        messageStateInt.value = null
    }

    fun release() {
        offTimer?.cancel()
    }

    private fun playMessageSound() {
        playAlarmSound(
            player = player,
            soundToneType = AlarmSoundToneType.BARIUM,
            soundVolume = soundVolume,
            isFadeIn = false,
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
