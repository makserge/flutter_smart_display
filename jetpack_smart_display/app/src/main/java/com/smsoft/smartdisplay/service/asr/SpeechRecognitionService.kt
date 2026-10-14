package com.smsoft.smartdisplay.service.asr

import com.smsoft.smartdisplay.BuildConfig
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.media3.common.util.UnstableApi
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.data.AsrWakeWord
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.composable.settings.ASR_SOUND_ENABLED_DEFAULT
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import com.smsoft.smartdisplay.utils.getForegroundNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.StorageService
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class SpeechRecognitionService : Service() {
    @Inject
    lateinit var speechRecognitionHandler: SpeechRecognitionHandler

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    // Not 77: that id belongs to SensorService's foreground notification.
    private val STICKY_NOTIFICATION_ID = 78

    private var isInRecognizingMode = false

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Guards the recognizer objects and isDestroyed: the model loads on an IO thread (several
    // seconds) and the service can be destroyed meanwhile (ASR switched off right after on).
    private val lock = Any()
    private var speechService: GuardedSpeechService? = null
    // Native Vosk objects: only close() frees their memory, so every restart must release them.
    private var model: Model? = null
    private var recognizer: Recognizer? = null
    @Volatile
    private var isDestroyed = false

    // Follows Settings; a new wake word used to apply only after voice control was switched off
    // and on again.
    @Volatile
    private var wakeWord = ""

    // Follows Settings: the wake-word chime is played only when this is on.
    @Volatile
    private var soundEnabled = ASR_SOUND_ENABLED_DEFAULT

    // Start/restart bookkeeping (main and IO threads): one start attempt at a time, a growing
    // delay between failed attempts, and one error chime per failure series.
    private var startJob: Job? = null
    @Volatile
    private var retryDelayMs = INIT_RETRY_INITIAL_MS
    @Volatile
    private var hasReportedFailure = false
    // When the current recognizer started listening (elapsedRealtime).
    @Volatile
    private var startedAtMs = 0L

    private val listener = object: RecognitionListener {
        override fun onPartialResult(hypothesis: String?) {
        }

        override fun onResult(hypothesis: String?) {
            val hypotheses = getHypotheses(hypothesis)
            val word = hypotheses.firstOrNull()
            // What the recognizer heard, for testing recognition quality (debug builds only: speech)
            if (BuildConfig.DEBUG && (word != null)) {
                Log.d(TAG, "Recognized: $word")
                if (hypotheses.size > 1) {
                    Log.d(TAG, "Alternatives: ${hypotheses.drop(1).joinToString(" | ")}")
                }
            }
            if (word == null) {
                if (isInRecognizingMode) {
                    isInRecognizingMode = false
                    emit(SpeechRecognitionState.Ready)
                }
                return
            }
            // Recognition works: the next failure gets a chime again and retries start fast.
            retryDelayMs = INIT_RETRY_INITIAL_MS
            hasReportedFailure = false
            // The wake word alone, a command (in one breath with the wake word, or in two steps
            // after it) or nothing for the panel: the text rules are in CommandText.kt. The
            // other readings go along with a command, so one that the best reading spoils with
            // an [unk] can still be found in them.
            val utterance = interpretUtterance(hypotheses, wakeWord, isInRecognizingMode) {
                isCommand(this@SpeechRecognitionService, it)
            }
            when (utterance) {
                is Utterance.Command -> {
                    isInRecognizingMode = false
                    val candidates = utterance.candidates
                    emit(SpeechRecognitionState.Result(word = candidates.first(), alternatives = candidates.drop(1)))
                }
                Utterance.WakeWord -> {
                    isInRecognizingMode = true

                    emit(SpeechRecognitionState.WakeWordDetected)
                    // The app now plays its chime, which the microphone would hear as speech
                    if (soundEnabled) {
                        synchronized(lock) {
                            speechService?.mutedUntilMs = SystemClock.elapsedRealtime() + CHIME_MUTE_MS
                        }
                    }
                }
                Utterance.Ignored -> {}
            }
        }

        override fun onFinalResult(hypothesis: String?) {
            isInRecognizingMode = false
            emit(SpeechRecognitionState.Ready)
        }

        override fun onError(exception: Exception?) {
            isInRecognizingMode = false
            Log.w(TAG, "Recognizer error", exception)
            if (isDestroyed) {
                return
            }
            // The microphone loop ended: AudioRecord.read() failed (the audio server restarted)
            // or recording could not start (on Android 8/9 while another app records).
            if (SystemClock.elapsedRealtime() - startedAtMs > STABLE_RUN_MS) {
                // After a long healthy run this is a new failure series: restart fast, with a
                // chime. Before, the delay only reset when a word was recognised, so on a quiet
                // panel every later audio server restart waited longer, up to 5 minutes.
                retryDelayMs = INIT_RETRY_INITIAL_MS
                hasReportedFailure = false
            }
            synchronized(lock) {
                releaseRecognizer()
            }
            reportFailure(exception?.message)
            // Closes a "listening" dialog; Vosk used to post a final result after stop().
            emit(SpeechRecognitionState.Ready)
            scheduleStart(retryDelayMs)
            retryDelayMs = nextRetryDelay(retryDelayMs)
        }

        override fun onTimeout() {
            // Not used: recognition runs without a timeout.
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground()
        serviceScope.launch {
            dataStore.data
                .map { it[stringPreferencesKey(PreferenceKey.ASR_WAKE_WORD.key)] }
                .distinctUntilChanged()
                .collect {
                    wakeWord = wakeWordText(it)
                    Log.i(TAG, "Wake word: $wakeWord")
                }
        }
        serviceScope.launch {
            dataStore.data
                .map { it[booleanPreferencesKey(PreferenceKey.ASR_SOUND_ENABLED.key)] ?: ASR_SOUND_ENABLED_DEFAULT }
                .distinctUntilChanged()
                .collect { soundEnabled = it }
        }
        scheduleStart(0L)
    }

    /** Starts the recognizer after [delayMs] unless a start attempt is already running. */
    private fun scheduleStart(delayMs: Long) {
        synchronized(lock) {
            if (isDestroyed || (startJob?.isActive == true)) {
                return
            }
            startJob = serviceScope.launch {
                delay(delayMs)
                startRecognizer()
            }
        }
    }

    /**
     * Loads the model and starts listening. A failure (microphone busy, model copy failed) is
     * retried with backoff for as long as the service lives; before, the service stayed in the
     * foreground without a recognizer and voice control stayed dead.
     */
    private suspend fun startRecognizer() {
        while (!isDestroyed) {
            try {
                wakeWord = wakeWordText(
                    dataStore.data.first()[stringPreferencesKey(PreferenceKey.ASR_WAKE_WORD.key)]
                )
                initVosk()
                return
            } catch (e: Exception) {
                Log.e(TAG, "Speech recognition could not start", e)
                reportFailure(e.message)
            }
            delay(retryDelayMs)
            retryDelayMs = nextRetryDelay(retryDelayMs)
        }
    }

    /** One error chime per failure series; the retries that follow stay silent. */
    private fun reportFailure(message: String?) {
        if (!hasReportedFailure) {
            hasReportedFailure = true
            emit(SpeechRecognitionState.Error(message = message.toString()))
        }
    }

    private fun nextRetryDelay(current: Long) = (current * 2).coerceAtMost(INIT_RETRY_MAX_MS)

    /** An unknown id (e.g. from an older version) falls back to the default wake word. */
    private fun wakeWordText(id: String?): String {
        val word = AsrWakeWord.entries.firstOrNull { it.id == id } ?: AsrWakeWord.getDefault()
        return resources.getString(word.titleId)
    }

    /**
     * Stops and frees the current recognizer. Must hold [lock]. stop() joins the recognizer
     * thread, so the native objects are no longer in use when they are closed.
     */
    private fun releaseRecognizer() {
        speechService?.apply {
            stop()
            shutdown()
        }
        speechService = null
        recognizer?.close()
        recognizer = null
        model?.close()
        model = null
    }

    override fun onDestroy() {
        serviceScope.cancel()
        synchronized(lock) {
            isDestroyed = true
            // Nothing to release when the model was still loading (this used to crash on a
            // lateinit access).
            releaseRecognizer()
        }
        speechRecognitionHandler.isServiceStarted.value = false
        // Clears a "listening" dialog that was open when ASR was switched off. Sent directly:
        // emit() is muted from here on.
        speechRecognitionHandler.onRecognitionState(SpeechRecognitionState.Initial)
        super.onDestroy()
    }

    /** Events of a destroyed instance must not reach the app-wide flow of the next one. */
    private fun emit(state: SpeechRecognitionState) {
        if (!isDestroyed) {
            speechRecognitionHandler.onRecognitionState(state)
        }
    }

    private fun startForeground() {
        val notification = getForegroundNotification(
            context = this
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(
                STICKY_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(
                STICKY_NOTIFICATION_ID,
                notification
            )
        }
    }

    private fun initVosk() {
        if (isDestroyed) {
            return
        }
        val outputPath = StorageService.sync(
            this,
            MODEL,
            TARGET_PATH
        )
        if (isDestroyed) {
            return
        }
        val newModel = Model(outputPath)
        var newRecognizer: Recognizer? = null
        try {
            newRecognizer = Recognizer(newModel, SAMPLE_RATE)
            // The best few readings of each utterance, not just the best one: see getHypotheses
            newRecognizer.setMaxAlternatives(MAX_ALTERNATIVES)
            val service = GuardedSpeechService(newRecognizer, SAMPLE_RATE)
            synchronized(lock) {
                if (isDestroyed) {
                    // Switched off while the model was loading: never open the microphone.
                    service.shutdown()
                    newRecognizer.close()
                    newModel.close()
                    return
                }
                model = newModel
                recognizer = newRecognizer
                speechService = service
                startedAtMs = SystemClock.elapsedRealtime()
                service.startListening(listener)
                // This start attempt is over: if the new loop fails at once, its onError must be
                // able to schedule the next start (the check in scheduleStart skipped it).
                startJob = null
            }
        } catch (e: Exception) {
            // E.g. "Failed to initialize recorder. Microphone might be already in use."
            newRecognizer?.close()
            newModel.close()
            throw e
        }
        emit(SpeechRecognitionState.Ready)
        speechRecognitionHandler.isServiceStarted.value = true
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * What the recognizer heard, its best reading first, or empty when it heard nothing. With
     * alternatives switched on Vosk answers {"alternatives": [{"confidence": .., "text": ".."}, ..]},
     * best first; without, {"text": ".."}. Empty readings (an alternative can be silence) and
     * repeats are dropped.
     */
    private fun getHypotheses(hypothesis: String?): List<String> {
        if (hypothesis == null) {
            return emptyList()
        }
        try {
            val jObject = JSONObject(hypothesis)
            val alternatives = jObject.optJSONArray("alternatives")
            if (alternatives != null) {
                return (0 until alternatives.length())
                    .mapNotNull { alternatives.optJSONObject(it)?.optString("text")?.trim { c -> c <= ' ' } }
                    .filter { it.isNotEmpty() }
                    .distinct()
            }
            val text = jObject.getString("text")
            if (text.isNotEmpty()) {
                return listOf(text.trim { it <= ' ' })
            }
        } catch (ignored: JSONException) {
        }
        return emptyList()
    }

    companion object {
        const val MODEL = "model-small-ru" /*"model-en-us"*/
        const val TARGET_PATH = "model"
        const val SAMPLE_RATE = 16000.0F
        private const val TAG = "SpeechRecognitionService"
        private const val INIT_RETRY_INITIAL_MS = 5_000L
        private const val INIT_RETRY_MAX_MS = 5 * 60_000L
        private const val STABLE_RUN_MS = 60_000L
        // The wake-word chime (sounds/ding.wav) lasts 0.49 s and takes a few hundred ms to start
        // playing: no audio is recognised for this long after the wake word, so the chime is not
        // taken for speech. A command started earlier than this loses its first words.
        private const val CHIME_MUTE_MS = 900L
        // Readings of each utterance the recognizer reports: the best, and this many minus one
        // others. More would make a command out of speech the best reading does not support.
        private const val MAX_ALTERNATIVES = 3
    }
}
