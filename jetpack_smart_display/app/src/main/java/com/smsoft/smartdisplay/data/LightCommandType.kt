package com.smsoft.smartdisplay.data

import android.content.Context
import com.smsoft.smartdisplay.R

enum class LightCommandType(
    val onCommandIds: List<Int>,
    val offCommandIds: List<Int>,
    val asrCommand: AsrCommand,
    val setPrefixCommandIds: List<Int> = emptyList(),
    val setAsrCommand: AsrCommand? = null,
    val stepUpCommandIds: List<Int> = emptyList(),
    val stepDownCommandIds: List<Int> = emptyList(),
    val stepAsrCommand: AsrCommand? = null
) {
    LIGHT1(
        onCommandIds = listOf(R.string.light_on_command, R.string.light_on2_command),
        offCommandIds = listOf(R.string.light_off_command, R.string.light_off2_command),
        asrCommand = AsrCommand.LIGHT1
    ),
    LIGHT2(
        onCommandIds = listOf(R.string.light2_on_command, R.string.light2_on2_command),
        offCommandIds = listOf(R.string.light2_off_command, R.string.light2_off2_command),
        asrCommand = AsrCommand.LIGHT2
    ),
    DIMMER_LIGHT(
        onCommandIds = listOf(R.string.dimmer_light_on_command, R.string.dimmer_light_on2_command),
        offCommandIds = listOf(R.string.dimmer_light_off_command, R.string.dimmer_light_off2_command),
        asrCommand = AsrCommand.DIMMER_LIGHT,
        setPrefixCommandIds = listOf(R.string.dimmer_light_set_command),
        setAsrCommand = AsrCommand.DIMMER_LIGHT_SET,
        stepUpCommandIds = listOf(R.string.dimmer_light_brighter_command),
        stepDownCommandIds = listOf(R.string.dimmer_light_darker_command),
        stepAsrCommand = AsrCommand.DIMMER_LIGHT_STEP
    );

    companion object {
        private var onOffCache: Map<String, Pair<AsrCommand, Boolean>> = emptyMap()
        private var stepCache: Map<String, Pair<AsrCommand, Int>> = emptyMap()
        private var setPrefixCache: Map<String, AsrCommand> = emptyMap()

        fun match(context: Context, command: String): Pair<AsrCommand, Boolean>? {
            if (onOffCache.isEmpty()) {
                onOffCache = entries.flatMap { type ->
                    type.onCommandIds.map { context.getString(it) to (type.asrCommand to true) } +
                            type.offCommandIds.map { context.getString(it) to (type.asrCommand to false) }
                }.toMap()
            }
            return onOffCache[command]
        }

        fun matchStep(context: Context, command: String): Pair<AsrCommand, Int>? {
            if (stepCache.isEmpty()) {
                stepCache = entries.filter { it.stepAsrCommand != null }.flatMap { type ->
                    type.stepUpCommandIds.map { context.getString(it) to (type.stepAsrCommand!! to 1) } +
                            type.stepDownCommandIds.map { context.getString(it) to (type.stepAsrCommand!! to -1) }
                }.toMap()
            }
            return stepCache[command]
        }

        fun matchSetPrefix(context: Context, command: String): Pair<AsrCommand, String>? {
            if (setPrefixCache.isEmpty()) {
                setPrefixCache = entries.filter { it.setAsrCommand != null }
                    .flatMap { type -> type.setPrefixCommandIds.map { context.getString(it) to type.setAsrCommand!! } }
                    .toMap()
            }
            for ((prefix, asrCommand) in setPrefixCache) {
                if (command.startsWith(prefix)) {
                    return asrCommand to command.removePrefix(prefix).trim()
                }
            }
            return null
        }
    }
}