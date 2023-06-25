package com.smsoft.smartdisplay.service.asr

import android.content.Context
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.DashboardItem
import com.smsoft.smartdisplay.data.VoiceCommand
import com.smsoft.smartdisplay.ui.screen.dashboard.MQTT_PRESS_BUTTON_OFF_MESSAGE
import com.smsoft.smartdisplay.ui.screen.dashboard.MQTT_PRESS_BUTTON_ON_MESSAGE

fun processCommand(
    context: Context,
    command: String,
    onPageChanged: (Int, VoiceCommand?) -> Unit,
    onError: () -> Unit,
    onPressButton: (String) -> Unit
) {
    if ((command == context.getString(R.string.light_on_command))
        || (command == context.getString(R.string.light_on2_command))
        ) {
        onPressButton(MQTT_PRESS_BUTTON_ON_MESSAGE)
        return
    }
    if ((command == context.getString(R.string.light_off_command))
        || (command == context.getString(R.string.light_off2_command))
    ) {
        onPressButton(MQTT_PRESS_BUTTON_OFF_MESSAGE)
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