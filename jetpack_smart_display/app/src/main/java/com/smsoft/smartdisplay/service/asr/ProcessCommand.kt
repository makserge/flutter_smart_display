package com.smsoft.smartdisplay.service.asr

import android.content.Context
import com.smsoft.smartdisplay.data.DashboardItem
import com.smsoft.smartdisplay.data.VoiceCommand

fun processCommand(
    context: Context,
    command: String,
    onPageChanged: (Int, VoiceCommand?) -> Unit,
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
    if (item == DashboardItem.INTERNET_RADIO) {
        onPageChanged(
            item.ordinal,
            VoiceCommand.getByCommand(
                context = context,
                command = command
            )
        )
    } else {
        onPageChanged(
            item.ordinal,
            null
        )
    }
}