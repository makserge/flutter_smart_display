package com.smsoft.smartdisplay.service.asr

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SpeechRecognitionServiceHandler: SpeechRecognitionHandler {
    override val isServiceStarted: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val events = MutableSharedFlow<SpeechRecognitionState>(extraBufferCapacity = 16)
    override val speechRecognitionState: SharedFlow<SpeechRecognitionState> = events.asSharedFlow()

    override fun onRecognitionState(state: SpeechRecognitionState) {
        events.tryEmit(state)
    }
}

sealed class SpeechRecognitionState {
    data object Initial: SpeechRecognitionState()
    data object Ready: SpeechRecognitionState()
    data object WakeWordDetected: SpeechRecognitionState()
    data class Result(val word: String) : SpeechRecognitionState()
    data class Error(val message: String) : SpeechRecognitionState()
}
