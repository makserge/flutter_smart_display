package com.smsoft.smartdisplay.service.asr

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface SpeechRecognitionHandler {
    var isServiceStarted: MutableStateFlow<Boolean>
    var speechRecognitionState: Flow<SpeechRecognitionState>?
}