package com.smsoft.smartdisplay.ui.screen.dashboard.controller

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.smsoft.smartdisplay.data.AsrCommand
import com.smsoft.smartdisplay.data.AudioType
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionHandler
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionService
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionState
import com.smsoft.smartdisplay.service.asr.processCommand
import com.smsoft.smartdisplay.ui.composable.settings.ASR_SOUND_ENABLED_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.ASR_SOUND_VOLUME_DEFAULT
import com.smsoft.smartdisplay.ui.screen.dashboard.SHOW_ASR_RESULT_TIMEOUT
import com.smsoft.smartdisplay.utils.TransientAudioDucking
import com.smsoft.smartdisplay.utils.observe
import com.smsoft.smartdisplay.utils.playAssetSound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@UnstableApi
class AsrController(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val speechRecognitionHandler: SpeechRecognitionHandler,
    private val player: ExoPlayer,
    private val listeningDucking: TransientAudioDucking,
    private val scope: CoroutineScope,
    private val onCommand: (command: String, type: AsrCommand, params: Any?) -> Unit,
) {
    private val permissionsStateInt = MutableStateFlow(false)
    val permissionsState = permissionsStateInt.asStateFlow()

    private val recognitionStateInt = MutableStateFlow<String?>(null)
    val recognitionState = recognitionStateInt.asStateFlow()

    private var isSoundEnabled = ASR_SOUND_ENABLED_DEFAULT
    private var soundVolume = ASR_SOUND_VOLUME_DEFAULT
    private var listeningTimeoutJob: Job? = null

    fun start() {
        // Both follow Settings; they used to be read once, so e.g. switching voice control on
        // without answering the permission dialog right away needed a restart.
        dataStore.observe(scope, { it[booleanPreferencesKey(PreferenceKey.ASR_ENABLED.key)] ?: false }) { isEnabled ->
            if (isEnabled) {
                permissionsStateInt.value = true
            } else {
                permissionsStateInt.value = false
                context.stopService(Intent(context, SpeechRecognitionService::class.java))
            }
        }
        dataStore.observe(scope, {
            Pair(
                it[booleanPreferencesKey(PreferenceKey.ASR_SOUND_ENABLED.key)] ?: ASR_SOUND_ENABLED_DEFAULT,
                it[floatPreferencesKey(PreferenceKey.ASR_SOUND_VOLUME.key)] ?: ASR_SOUND_VOLUME_DEFAULT
            )
        }) { (enabled, volume) ->
            isSoundEnabled = enabled
            soundVolume = volume
        }
        scope.launch {
            // Hot and app-wide: one collection keeps working across service restarts (ASR
            // switched off and on). Waiting for isServiceStarted and then collecting the first
            // service's own flow forever left voice control dead after a restart.
            speechRecognitionHandler.speechRecognitionState.collect { state ->
                onRecognitionState(state)
            }
        }
    }

    fun startAsrService() {
        permissionsStateInt.value = false
        scope.launch {
            ContextCompat.startForegroundService(
                context,
                Intent(context, SpeechRecognitionService::class.java)
            )
        }
    }

    fun disableAsr() {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(PreferenceKey.ASR_ENABLED.key)] = false
            }
        }
    }

    fun cancelAsrAction() {
        recognitionStateInt.value = null
    }

        fun playErrorSoundIfEnabled() {
        if (isSoundEnabled) {
            playAssetSound(
                player = player,
                audioType = AudioType.ERROR,
                soundVolume = soundVolume
            )
        }
    }

    /**
     * Ducks the radio while the user speaks the command, so it does not drown out the microphone.
     * The chime itself plays without audio focus and no longer pauses the radio.
     */
    private fun startListening() {
        listeningDucking.duck()
        listeningTimeoutJob?.cancel()
        listeningTimeoutJob = scope.launch {
            delay(LISTENING_DUCK_TIMEOUT)
            listeningDucking.release()
        }
    }

    /** Gives back the ducking focus if the owner goes away in the middle of listening. */
    fun release() {
        stopListening()
    }

    private fun stopListening() {
        listeningTimeoutJob?.cancel()
        listeningTimeoutJob = null
        listeningDucking.release()
    }

    private fun onRecognitionState(state: SpeechRecognitionState) {
        Log.d(TAG, "ASR event: $state")
        when (state) {
            is SpeechRecognitionState.Initial,
                SpeechRecognitionState.Ready -> {
                stopListening()
                recognitionStateInt.value = null
            }
            is SpeechRecognitionState.Result -> {
                // Restore the radio volume before the command runs (it may change the volume).
                stopListening()
                recognitionStateInt.value = state.word
                processCommand(
                    context = context,
                    command = state.word,
                    onCommand = { type, params ->
                        onCommand(state.word, type, params)
                        scope.launch {
                            delay(SHOW_ASR_RESULT_TIMEOUT)
                            recognitionStateInt.value = null
                        }
                    }
                )
            }
            SpeechRecognitionState.WakeWordDetected -> {
                recognitionStateInt.value = ""
                startListening()
                if (isSoundEnabled) {
                    playAssetSound(
                        player = player,
                        audioType = AudioType.WAKE_WORD,
                        soundVolume = soundVolume
                    )
                }
            }
            is SpeechRecognitionState.Error -> {
                stopListening()
                if (isSoundEnabled) {
                    playAssetSound(
                        player = player,
                        audioType = AudioType.ERROR,
                        soundVolume = soundVolume
                    )
                }
            }
        }
    }
}

// Safety net in case the recognizer never reports a result after the wake word.
private const val LISTENING_DUCK_TIMEOUT = 15000L
private const val TAG = "AsrController"
