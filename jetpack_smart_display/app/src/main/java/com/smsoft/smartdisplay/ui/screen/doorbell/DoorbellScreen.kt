package com.smsoft.smartdisplay.ui.screen.doorbell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import org.videolan.libvlc.util.VLCVideoLayout

@UnstableApi
@Composable
fun DoorbellScreen (
    modifier: Modifier = Modifier,
    viewModel: DoorbellViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    DisposableEffect(
        viewModel.onStart()
    ) {
        onDispose {
            viewModel.onStop()
        }
    }
    AndroidView(factory = {
        VLCVideoLayout(context).apply {
            viewModel.attachView(this)
        }
    })
}