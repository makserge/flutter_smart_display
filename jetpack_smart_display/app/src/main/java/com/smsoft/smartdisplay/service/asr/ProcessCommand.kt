package com.smsoft.smartdisplay.service.asr

import android.content.Context
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.AsrCommand
import com.smsoft.smartdisplay.data.VoiceCommandType

fun processCommand(
    context: Context,
    command: String,
    onCommand: (AsrCommand, Any?) -> Unit
) {
    when (command) {
        context.getString(R.string.light_on_command), context.getString(R.string.light_on2_command) -> {
            onCommand(AsrCommand.LIGHT1, true)
            return
        }
        context.getString(R.string.light_off_command), context.getString(R.string.light_off2_command) -> {
            onCommand(AsrCommand.LIGHT1, false)
            return
        }
        context.getString(R.string.light2_on_command), context.getString(R.string.light2_on2_command) -> {
            onCommand(AsrCommand.LIGHT2, true)
            return
        }
        context.getString(R.string.light2_off_command), context.getString(R.string.light2_off2_command) -> {
            onCommand(AsrCommand.LIGHT2, false)
            return
        }
    }
    if (command.startsWith(context.getString(R.string.timer_on_command))) {
        onCommand(AsrCommand.TIMER, command.replace(context.getString(R.string.timer_on_command), "").trim())
        return
    }
    if (command.startsWith(context.getString(R.string.timer2_on_command))) {
        onCommand(AsrCommand.TIMER, command.replace(context.getString(R.string.timer2_on_command), "").trim())
        return
    }
    val item = VoiceCommandType.getDashboardItem(
        context = context,
        command = command
    )
    onCommand(AsrCommand.PAGE, item)
}