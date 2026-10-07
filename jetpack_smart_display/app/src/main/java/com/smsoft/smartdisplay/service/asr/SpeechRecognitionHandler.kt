package com.smsoft.smartdisplay.service.asr

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

interface SpeechRecognitionHandler {
    /** True while a SpeechRecognitionService instance has its recognizer listening. */
    val isServiceStarted: MutableStateFlow<Boolean>

    /**
     * Recognition events of whichever SpeechRecognitionService instance is running.
     *
     * Hot and app-wide: collect it once. It keeps working when the service is stopped and started
     * again (ASR switched off and on in Settings) and when the collector is recreated. The old
     * per-service cold flow left the dashboard attached to a dead service after a restart.
     */
    val speechRecognitionState: SharedFlow<SpeechRecognitionState>

    /** Called by the service for every recognition event. */
    fun onRecognitionState(state: SpeechRecognitionState)
}
