package com.smsoft.smartdisplay.ui.screen.doorbell

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.IOException

@UnstableApi
@Composable
fun DoorbellScreen (
    modifier: Modifier = Modifier,
    viewModel: DoorbellViewModel = hiltViewModel(),
    uri: Uri
) {
    val context = LocalContext.current
    val libVlc = LibVLC(context, ArrayList<String>().apply {
        add("--rtsp-tcp")
        add("--verbose=-1")
    })
    val listener = MediaPlayer.EventListener {
        Log.d("DoorbellScreen", it.toString())
        if (it.type == MediaPlayer.Event.Buffering && it.buffering == 100f) {
        } else if (it.type == MediaPlayer.Event.EndReached) {
        }
    }
    val mediaPlayer = MediaPlayer(libVlc)
    mediaPlayer.setEventListener(listener)

    try {
        val media = Media(libVlc, uri)

        media.apply {
            setHWDecoderEnabled(true, false)
            addOption(":network-caching=500")

            mediaPlayer.media = this
        }.release()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    DisposableEffect(
        mediaPlayer.play()
    ) {
        onDispose {
            mediaPlayer.apply {
                stop()
                detachViews()
                release()
            }
            libVlc.release()
        }
    }

    AndroidView(factory = {
        VLCVideoLayout(context).apply {
            mediaPlayer.attachViews(this, null, false, false)
        }
    })
}