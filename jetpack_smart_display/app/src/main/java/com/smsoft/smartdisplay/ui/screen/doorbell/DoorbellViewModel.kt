package com.smsoft.smartdisplay.ui.screen.doorbell

import android.net.Uri
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import com.smsoft.smartdisplay.utils.getDoorbellStreamUrl
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun onStart() {
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

    fun onStop() {
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
}