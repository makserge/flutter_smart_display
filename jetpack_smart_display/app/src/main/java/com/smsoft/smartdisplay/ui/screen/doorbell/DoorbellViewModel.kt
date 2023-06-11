package com.smsoft.smartdisplay.ui.screen.doorbell

import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.screen.settings.DOORBELL_BACK_TIMER_DEFAULT_DELAY
import com.smsoft.smartdisplay.ui.screen.settings.DOORBELL_STREAM_DEFAULT_URL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class DoorbellViewModel @Inject constructor(
    private val libVlc: LibVLC,
    private val mediaPlayer: MediaPlayer,
    val dataStore: DataStore<Preferences>
) : ViewModel() {

    private var backTimer: CountDownTimer? = null
    private val backTimerStateInt = MutableStateFlow(false)
    val backTimerState = backTimerStateInt.asStateFlow()

    fun onStart(
        isBackTimerEnabled: Boolean
    ) {
        startVideo()
        if (isBackTimerEnabled) {
            startBackTimer()
        }
    }

    fun onStop() {
        backTimer?.cancel()

        mediaPlayer.apply {
            stop()
            detachViews()
        }
    }

    fun attachView(
        vlcVideoLayout: VLCVideoLayout
    ) {
        mediaPlayer.attachViews(vlcVideoLayout, null, false, false)
    }

    fun resetBackTimerState() {
        backTimerStateInt.value = false
    }

    private fun startVideo() {
        mediaPlayer.setEventListener {
            Log.d("DoorbellScreen", "VLC state:" + it.type.toString())
        }
        try {
            val uri = Uri.parse(getDoorbellStreamUrl(dataStore))
            val media = Media(libVlc, uri)

            media.apply {
                setHWDecoderEnabled(true, false)
                addOption(":file-caching=0")
                addOption(":network-caching=500")
                addOption(":live-caching=0")
                addOption(":clock-jitter=0")
                addOption(":clock-synchro=0")
                addOption(":drop-late-frames")
                addOption(":skip-frames")
                addOption(":low-delay")

                mediaPlayer.media = this
            }.release()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mediaPlayer.play()
    }

    private fun startBackTimer() {
        var countDownInterval = DOORBELL_BACK_TIMER_DEFAULT_DELAY
        runBlocking {
            val data = dataStore.data.first()
            data[floatPreferencesKey(PreferenceKey.DOORBELL_BACK_TIMER_DELAY.key)]?.let {
                countDownInterval = it
            }
        }
        backTimer = object : CountDownTimer((countDownInterval * 1000L).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                backTimerStateInt.value = true
            }
        }
        backTimer!!.start()
    }

    private fun getDoorbellStreamUrl(dataStore: DataStore<Preferences>): String {
        var url = DOORBELL_STREAM_DEFAULT_URL
        runBlocking {
            val data = dataStore.data.first()
            data[stringPreferencesKey(PreferenceKey.DOORBELL_STREAM_URL.key)]?.let {
                if (it.trim().isNotEmpty()) {
                    url = it.trim()
                }
            }
        }
        return url
    }

}