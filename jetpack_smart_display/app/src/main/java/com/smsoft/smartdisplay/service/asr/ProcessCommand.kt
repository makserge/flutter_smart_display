package com.smsoft.smartdisplay.service.asr

import android.content.Context
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.AsrCommand
import com.smsoft.smartdisplay.data.LightCommandType
import com.smsoft.smartdisplay.data.VoiceCommandType

private val TIMER_SET_PREFIX_COMMAND_IDS = listOf(
    R.string.timer_on_command,
    R.string.timer2_on_command
)

fun processCommand(
    context: Context,
    command: String,
    onCommand: (AsrCommand, Any?) -> Unit
) {
    LightCommandType.match(context, command)?.let { (asrCommand, isOn) ->
        onCommand(asrCommand, isOn)
        return
    }

    LightCommandType.matchStep(context, command)?.let { (asrCommand, direction) ->
        onCommand(asrCommand, direction)
        return
    }

    LightCommandType.matchSetPrefix(context, command)?.let { (asrCommand, remainder) ->
        onCommand(asrCommand, remainder)
        return
    }

    for (prefixId in TIMER_SET_PREFIX_COMMAND_IDS) {
        val prefix = context.getString(prefixId)
        if (command.startsWith(prefix)) {
            onCommand(AsrCommand.TIMER, command.removePrefix(prefix).trim())
            return
        }
    }

    val item = VoiceCommandType.getDashboardItem(
        context = context,
        command = command
    )
    onCommand(AsrCommand.PAGE, item)
}
