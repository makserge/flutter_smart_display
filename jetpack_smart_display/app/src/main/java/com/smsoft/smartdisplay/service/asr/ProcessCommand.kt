package com.smsoft.smartdisplay.service.asr

import android.content.Context
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.AsrCommand
import com.smsoft.smartdisplay.data.LightCommandType
import com.smsoft.smartdisplay.data.TimerDurationType
import com.smsoft.smartdisplay.data.VoiceCommandType

private val TIMER_SET_PREFIX_COMMAND_IDS = listOf(
    R.string.timer_on_command,
    R.string.timer2_on_command
)

/** The token the recognizer writes for speech that matches no word of its vocabulary. */
const val UNKNOWN_WORD = "[unk]"

/**
 * Runs the command in [candidates], the recognizer's readings of one utterance, best first: the
 * first reading that is a command runs, and [onCommand] gets that reading with the command. A
 * reading that still has [UNKNOWN_WORD] in it can never be a command. When no reading is a
 * command, the best one goes the usual way (a page, or nothing), so the error chime and the shown
 * text say what was heard.
 */
fun processCommand(
    context: Context,
    candidates: List<String>,
    onCommand: (command: String, AsrCommand, Any?) -> Unit
) {
    for (candidate in candidates) {
        if (candidate.contains(UNKNOWN_WORD)) {
            continue
        }
        val isCommand = runCommand(context, candidate) { asrCommand, params ->
            onCommand(candidate, asrCommand, params)
        }
        if (isCommand) {
            return
        }
    }
    val best = candidates.firstOrNull() ?: return
    onCommand(best, AsrCommand.PAGE, VoiceCommandType.getDashboardItem(context, best))
}

/** True when [text] is a command processCommand would run (no [UNKNOWN_WORD] in it, and it matches). */
fun isCommand(context: Context, text: String): Boolean =
    !text.contains(UNKNOWN_WORD) && runCommand(context, text) { _, _ -> }

/** Runs [command] through [onCommand] if it is one; true when it was. */
private fun runCommand(
    context: Context,
    command: String,
    onCommand: (AsrCommand, Any?) -> Unit
): Boolean {
    LightCommandType.match(context, command)?.let { (asrCommand, isOn) ->
        onCommand(asrCommand, isOn)
        return true
    }

    LightCommandType.matchStep(context, command)?.let { (asrCommand, direction) ->
        onCommand(asrCommand, direction)
        return true
    }

    LightCommandType.matchSetPrefix(context, command)?.let { (asrCommand, remainder) ->
        onCommand(asrCommand, remainder)
        return true
    }

    // "поставь таймер на пять минут": only with a duration the timers know. With none, or another
    // one, it set nothing, yet counted as a command, so another reading of the utterance with a
    // known duration was never tried and no error chime said it was not understood.
    for (prefixId in TIMER_SET_PREFIX_COMMAND_IDS) {
        val duration = commandAfterPrefix(command, context.getString(prefixId)) ?: continue
        if (TimerDurationType.getByCommand(context, duration) != null) {
            onCommand(AsrCommand.TIMER, duration)
            return true
        }
    }

    // "таймер на пять минут": the page name followed by a known duration sets the timer, as
    // "поставь таймер на пять минут" does. Anything else after the page name keeps the page lookup.
    val timerDuration = commandAfterPrefix(command, context.getString(R.string.timer_command))
    if ((timerDuration != null) && (TimerDurationType.getByCommand(context, timerDuration) != null)) {
        onCommand(AsrCommand.TIMER, timerDuration)
        return true
    }

    val item = VoiceCommandType.getDashboardItem(context, command) ?: return false
    onCommand(AsrCommand.PAGE, item)
    return true
}
