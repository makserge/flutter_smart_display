package com.smsoft.smartdisplay.ui.screen.doorbell

import android.content.Context
import android.view.ViewStub
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import org.videolan.libvlc.util.VLCVideoLayout

@UnstableApi
@Composable
fun DoorbellScreen (
    modifier: Modifier = Modifier,
    viewModel: DoorbellViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit,
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current

    val isBackTimerEnabled = (onBack != null)
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnSettingsClick by rememberUpdatedState(onSettingsClick)

    // Started by an effect once per visit. onStart() used to be called from the composition
    // itself, so every recomposition restarted the stream and started another back timer.
    // Only the screen in front (resumed) streams: during the fade between the dashboard and the
    // doorbell screen both are composed, and the doorbell page of the dashboard that is fading
    // out could otherwise take the one player away from the doorbell screen fading in.
    val videoLayout = remember(context) { createVideoLayout(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(viewModel, videoLayout, lifecycleOwner) {
        var visit: DoorbellStreamPlayer.Visit? = null
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> if (visit == null) {
                    visit = viewModel.onStart(
                        videoLayout = videoLayout,
                        isBackTimerEnabled = isBackTimerEnabled
                    )
                }
                Lifecycle.Event.ON_PAUSE -> {
                    visit?.let { viewModel.onStop(it) }
                    visit = null
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            visit?.let { viewModel.onStop(it) }
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.backRequests.collect {
            currentOnBack?.invoke()
        }
    }
    Box (
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit){
                detectTapGestures(
                    onTap = {
                        currentOnBack?.invoke()
                    },
                    onLongPress = {
                        currentOnSettingsClick()
                    }
                )
            }
    ) {
        AndroidView(factory = { videoLayout })
    }
}

/**
 * libVLC inflates the video SurfaceView only in attachViews(). On the screen that a ring opens
 * that happens after the screen has been laid out; Compose then lays the new view out while it
 * draws, after the point where a SurfaceView creates its surface, and on a still screen no further
 * frame follows. The surface never came, the player waited for it, and a ring showed a black
 * screen. Inflated here, the SurfaceView is part of the first layout; libVLC finds and uses it.
 */
private fun createVideoLayout(context: Context) = VLCVideoLayout(context).apply {
    findViewById<ViewStub>(org.videolan.R.id.surface_stub)?.inflate()
}
