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
    /**
     * A command that was heard, as the readings of the same audio, best first (interpretUtterance
     * in CommandText.kt): [word] is the first, [alternatives] the others. Usually they are the
     * command part: the wake word and one [unk] right after it are left out (in the second step of
     * a two-step command also one [unk] at the start), so they differ from the raw recognizer text
     * (the debug log "Recognized:" has that). When the best reading of a two-step command has
     * nothing left (a noise), [word] is that reading as heard ("[unk]"), which is no command; the
     * wake word said again keeps listening and gives no result. The command is the first of
     * [candidates] that is a command.
     */
    data class Result(val word: String, val alternatives: List<String> = emptyList()) : SpeechRecognitionState() {
        val candidates: List<String>
            get() = listOf(word) + alternatives
    }
    data class Error(val message: String) : SpeechRecognitionState()
}
