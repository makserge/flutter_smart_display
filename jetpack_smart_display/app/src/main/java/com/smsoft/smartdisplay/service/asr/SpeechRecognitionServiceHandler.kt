package com.smsoft.smartdisplay.service.asr

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SpeechRecognitionServiceHandler: SpeechRecognitionHandler {
    override var isServiceStarted: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override var speechRecognitionState: Flow<SpeechRecognitionState>? = null
}

sealed class SpeechRecognitionState {
    object Initial: SpeechRecognitionState()
    object Ready: SpeechRecognitionState()
    object WakeWordDetected: SpeechRecognitionState()
    data class Result(val word: String) : SpeechRecognitionState()
    data class Error(val message: String) : SpeechRecognitionState()
}