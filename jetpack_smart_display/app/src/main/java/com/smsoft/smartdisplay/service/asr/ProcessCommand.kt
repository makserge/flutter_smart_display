package com.smsoft.smartdisplay.service.asr

import android.content.Context
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.DashboardItem
import com.smsoft.smartdisplay.data.VoiceCommand

fun processCommand(
    context: Context,
    command: String,
    onPageChanged: (Int, VoiceCommand?) -> Unit,
    onError: () -> Unit,
    onPressButton: (Boolean) -> Unit,
    onPressButton2: (Boolean) -> Unit
) {
    if ((command == context.getString(R.string.light_on_command))
        || (command == context.getString(R.string.light_on2_command))
        ) {
        onPressButton(true)
        return
    } else if ((command == context.getString(R.string.light_off_command))
        || (command == context.getString(R.string.light_off2_command))
    ) {
        onPressButton(false)
        return
    } else if ((command == context.getString(R.string.light2_on_command))
        || (command == context.getString(R.string.light2_on2_command))
    ) {
        onPressButton2(true)
        return
    } else if ((command == context.getString(R.string.light2_off_command))
        || (command == context.getString(R.string.light2_off2_command))
    ) {
        onPressButton2(false)
        return
    }
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