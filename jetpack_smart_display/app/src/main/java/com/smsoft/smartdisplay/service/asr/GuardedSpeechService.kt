package com.smsoft.smartdisplay.service.asr

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import java.io.IOException
import kotlin.math.roundToInt

/**
 * Microphone loop for a Vosk [Recognizer], in place of org.vosk.android.SpeechService.
 *
 * Vosk's recognizer thread throws RuntimeException("error reading audio buffer") when
 * AudioRecord.read() returns an error, e.g. ERROR_DEAD_OBJECT after the audio server restarted.
 * Nothing catches it on that thread, so it killed the whole app. Here every failure ends the
 * loop and is reported once through [RecognitionListener.onError] on the main thread; the owner
 * then restarts recognition.
 *
 * Same contract as Vosk's class: the constructor throws IOException when the microphone cannot
 * be opened, stop() joins the loop thread (the recognizer is unused afterwards), shutdown()
 * releases the recorder. Callbacks not delivered yet when stop() runs are dropped.
 */
@SuppressLint("MissingPermission") // The service only runs with RECORD_AUDIO granted.
class GuardedSpeechService @Throws(IOException::class) constructor(
    private val recognizer: Recognizer,
    sampleRate: Float,
) {
    // Same audio settings as Vosk: 0.2 s buffers, mono 16-bit PCM, voice recognition source.
    private val bufferSize = (sampleRate * 0.2f).roundToInt()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val recorder: AudioRecord
    @Volatile
    private var thread: Thread? = null
    @Volatile
    private var isStopped = false

    init {
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            sampleRate.toInt(),
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize * 2
        )
        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord.release()
            throw IOException("Failed to initialize recorder. Microphone might be already in use.")
        }
        recorder = audioRecord
    }

    fun startListening(listener: RecognitionListener): Boolean {
        if (thread != null) {
            return false
        }
        isStopped = false
        thread = Thread({ runLoop(listener) }, THREAD_NAME).also { it.start() }
        return true
    }

    fun stop(): Boolean {
        val loop = thread ?: return false
        isStopped = true
        loop.interrupt()
        try {
            // Wakes a read() that is blocked in native code.
            recorder.stop()
        } catch (_: IllegalStateException) {
        }
        try {
            loop.join()
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        thread = null
        return true
    }

    fun shutdown() {
        recorder.release()
    }

    private fun runLoop(listener: RecognitionListener) {
        try {
            listen(listener)
        } catch (t: Throwable) {
            // Nothing may escape this thread: an uncaught exception would kill the app.
            if (!isStopped) {
                Log.w(TAG, "Audio loop failed", t)
                val error = t as? Exception ?: IOException(t)
                post { listener.onError(error) }
            }
        } finally {
            try {
                recorder.stop()
            } catch (_: IllegalStateException) {
            }
        }
    }

    private fun listen(listener: RecognitionListener) {
        recorder.startRecording()
        if (recorder.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
            // Vosk reported this and kept reading; here it ends the loop.
            throw IOException("Failed to start recording. Microphone might be already in use.")
        }
        val buffer = ShortArray(bufferSize)
        var emptyReads = 0
        while (!isStopped && !Thread.currentThread().isInterrupted) {
            val count = recorder.read(buffer, 0, buffer.size)
            if (isStopped) {
                return
            }
            if (count < 0) {
                // -6 is ERROR_DEAD_OBJECT: the audio server went away and could not be reached.
                throw IOException("AudioRecord.read() failed: $count")
            }
            if (count == 0) {
                // A blocking read returns no data only when recording stopped; do not spin.
                if (++emptyReads >= MAX_EMPTY_READS) {
                    throw IOException("AudioRecord delivers no audio")
                }
                Thread.sleep(EMPTY_READ_PAUSE_MS)
                continue
            }
            emptyReads = 0
            if (recognizer.acceptWaveForm(buffer, count)) {
                val result = recognizer.result
                post { listener.onResult(result) }
            }
            // Partial results are not used by the service, so they are not computed.
        }
    }

    private fun post(callback: () -> Unit) {
        mainHandler.post {
            if (!isStopped) {
                callback()
            }
        }
    }

    private companion object {
        const val TAG = "GuardedSpeechService"
        const val THREAD_NAME = "VoskRecognizer"
        const val MAX_EMPTY_READS = 50
        const val EMPTY_READ_PAUSE_MS = 20L
    }
}
