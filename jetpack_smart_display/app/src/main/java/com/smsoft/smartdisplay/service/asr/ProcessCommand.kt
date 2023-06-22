package com.smsoft.smartdisplay.service.asr

import android.content.Context
import com.smsoft.smartdisplay.data.VoiceCommand

fun processCommand(
    context: Context,
    command: String,
    onPageChanged: (Int) -> Unit,
    onError: () -> Unit
) {
    val item = VoiceCommand.getDashboardItem(
        context = context,
        command = command
    )
    if (item == null) {
        onError()
        return
    }
    onPageChanged(item.ordinal)
}