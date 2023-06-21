package com.smsoft.smartdisplay.service.asr

import android.content.Context
import com.smsoft.smartdisplay.data.VoiceCommand

fun processCommand(
    context: Context,
    command: String,
    onPageChanged: (Int) -> Unit
) {
    val item = VoiceCommand.getDashboardItem(
        context = context,
        command = command
    )
    onPageChanged(item.ordinal)
}