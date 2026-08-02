package com.smsoft.smartdisplay.ui.screen.dashboard.controller

import android.content.Context
import android.content.Intent
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
import com.smsoft.smartdisplay.utils.playAssetSound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@UnstableApi
class AsrController(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val speechRecognitionHandler: SpeechRecognitionHandler,
    private val player: ExoPlayer,
    private val scope: CoroutineScope,
    private val onCommand: (command: String, type: AsrCommand, params: Any?) -> Unit,
) {
    private val permissionsStateInt = MutableStateFlow(false)
    val permissionsState = permissionsStateInt.asStateFlow()

    private val recognitionStateInt = MutableStateFlow<String?>(null)
    val recognitionState = recognitionStateInt.asStateFlow()

    private var isSoundEnabled = ASR_SOUND_ENABLED_DEFAULT
    private var soundVolume = ASR_SOUND_VOLUME_DEFAULT

    fun start() {
        scope.launch {
            loadPreferences()
        }
        scope.launch {
            speechRecognitionHandler.isServiceStarted.asStateFlow().collect { isStarted ->
                if (isStarted) {
                    observeRecognitionState()
                }
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

    private suspend fun loadPreferences() {
        val data = dataStore.data.first()
        val isEnabled = data[booleanPreferencesKey(PreferenceKey.ASR_ENABLED.key)] ?: false
        if (isEnabled) {
            permissionsStateInt.value = true
        } else {
            permissionsStateInt.value = false
            context.stopService(Intent(context, SpeechRecognitionService::class.java))
        }
        data[booleanPreferencesKey(PreferenceKey.ASR_SOUND_ENABLED.key)]?.let {
            isSoundEnabled = it
        }
        data[floatPreferencesKey(PreferenceKey.ASR_SOUND_VOLUME.key)]?.let {
            soundVolume = it
        }
    }

    private suspend fun observeRecognitionState() {
        val speechRecognitionState = speechRecognitionHandler.speechRecognitionState!!.stateIn(
            initialValue = SpeechRecognitionState.Initial,
            scope = scope,
            started = WhileSubscribed(5000)
        )
        speechRecognitionState.collect { state ->
            when (state) {
                is SpeechRecognitionState.Initial,
                    SpeechRecognitionState.Ready -> {
                    recognitionStateInt.value = null
                }
                is SpeechRecognitionState.Result -> {
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
                    if (isSoundEnabled) {
                        playAssetSound(
                            player = player,
                            audioType = AudioType.WAKE_WORD,
                            soundVolume = soundVolume
                        )
                    }
                }
                is SpeechRecognitionState.Error -> {
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
}
