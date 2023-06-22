package com.smsoft.smartdisplay.service.asr

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.utils.getForegroundNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class SpeechRecognitionService : Service() {
    @Inject
    lateinit var speechRecognitionHandler: SpeechRecognitionHandler

    private val STICKY_NOTIFICATION_ID = 77

    private var isInRecognizingMode = false

    private lateinit var speechService: SpeechService
    private lateinit var wakeWord: String

    override fun onCreate() {
        super.onCreate()
        startForeground()
        wakeWord = resources.getString(R.string.asr_wakeword)

        CoroutineScope(Dispatchers.IO).launch {
            initVosk()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        speechService.apply {
            stop()
            shutdown()
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
        val outputPath = StorageService.sync(
            this,
            MODEL,
            TARGET_PATH
        )
        val model = Model(outputPath)
        val rec = Recognizer(model, SAMPLE_RATE)
        speechService = SpeechService(rec, SAMPLE_RATE)

        var listener: RecognitionListener? = null
        speechRecognitionHandler.speechRecognitionState = callbackFlow {
            listener = object: RecognitionListener {
                override fun onPartialResult(hypothesis: String?) {
                }

                override fun onResult(hypothesis: String?) {
                    //Log.d(TAG, "onResult: " + hypothesis!!)
                    val word = getRecognizedWord(hypothesis)
                    if (word == null) {
                        if (isInRecognizingMode) {
                            isInRecognizingMode = false
                            trySend(SpeechRecognitionState.Ready)
                        }
                        return
                    }
                    if (isInRecognizingMode) {
                        isInRecognizingMode = false

                        trySend(SpeechRecognitionState.Result(word = word))
                    } else if (wakeWord.equals(
                            word,
                            ignoreCase = true
                        )) {
                        isInRecognizingMode = true

                        trySend(SpeechRecognitionState.WakeWordDetected)
                    }
                }

                override fun onFinalResult(hypothesis: String?) {
                    isInRecognizingMode = false
                    trySend(SpeechRecognitionState.Ready)
                }

                override fun onError(exception: Exception?) {
                    isInRecognizingMode = false
                    trySend(SpeechRecognitionState.Error(message = exception!!.message.toString()))
                }

                override fun onTimeout() {
                    speechService.cancel()
                    speechService.startListening(listener)
                    trySend(SpeechRecognitionState.Error(message = "onTimeout"))
                }
            }
            speechService.startListening(listener)
            trySend(SpeechRecognitionState.Ready)
            awaitClose {
            }
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

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
    }
}