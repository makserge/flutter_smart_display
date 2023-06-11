package com.smsoft.smartdisplay.ui.screen.doorbell

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import org.videolan.libvlc.util.VLCVideoLayout

@UnstableApi
@Composable
fun DoorbellScreen (
    modifier: Modifier = Modifier,
    viewModel: DoorbellViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(
        viewModel.onStart()
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