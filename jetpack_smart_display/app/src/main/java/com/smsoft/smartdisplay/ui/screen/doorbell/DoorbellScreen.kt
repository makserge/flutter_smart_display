package com.smsoft.smartdisplay.ui.screen.doorbell

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    val backTimerState = viewModel.backTimerState.collectAsStateWithLifecycle()
    if (isBackTimerEnabled && backTimerState.value) {
        viewModel.resetBackTimerState()
        LaunchedEffect(Unit) {
            onBack?.let { it() }
        }
        return
    }

    DisposableEffect(
        viewModel.onStart(
            isBackTimerEnabled = isBackTimerEnabled
        )
    ) {
        onDispose {
            viewModel.onStop()
        }
    }
    Box (
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit){
                detectTapGestures(
                    onTap = {
                        onBack?.let { it() }
                    },
                    onLongPress = {
                        onSettingsClick()
                    }
                )
            }
    ) {
        AndroidView(factory = {
            VLCVideoLayout(context).apply {
                viewModel.attachView(this)
            }
        })
    }
}