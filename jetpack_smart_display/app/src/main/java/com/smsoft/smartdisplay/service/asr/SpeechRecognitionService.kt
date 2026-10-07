package com.smsoft.smartdisplay.service.asr

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
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.data.AsrWakeWord
import com.smsoft.smartdisplay.data.PreferenceKey
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
            val word = getRecognizedWord(hypothesis)
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
            if (isInRecognizingMode) {
                isInRecognizingMode = false

                emit(SpeechRecognitionState.Result(word = word))
            } else if (wakeWord.equals(
                    word,
                    ignoreCase = true
                )) {
                isInRecognizingMode = true

                emit(SpeechRecognitionState.WakeWordDetected)
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

    private fun getRecognizedWord(hypothesis: String?): String? {
        if (hypothesis == null) {
            return null
        }
        try {
            val jObject = JSONObject(hypothesis)
            val text = jObject.getString("text")
            if (text.isNotEmpty()) {
                return text.trim { it <= ' ' }
            }
        } catch (ignored: JSONException) {
        }
        return null
    }

    companion object {
        const val MODEL = "model-small-ru" /*"model-en-us"*/
        const val TARGET_PATH = "model"
        const val SAMPLE_RATE = 16000.0F
        private const val TAG = "SpeechRecognitionService"
        private const val INIT_RETRY_INITIAL_MS = 5_000L
        private const val INIT_RETRY_MAX_MS = 5 * 60_000L
        private const val STABLE_RUN_MS = 60_000L
    }
}
