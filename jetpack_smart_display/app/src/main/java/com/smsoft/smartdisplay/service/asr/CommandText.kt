package com.smsoft.smartdisplay.service.asr

/**
 * The command in [text] when the wake word starts it, said in one breath ("джарвис включи свет"),
 * or null when [text] is not such an utterance. A wake word said twice ("компьютер компьютер
 * подсветка на восемьдесят процентов") counts once, and one [UNKNOWN_WORD] right after the wake
 * word is left out (see [dropWakeWordPrefix]). Pure function, no Android.
 */
fun commandAfterWakeWord(text: String, wakeWord: String): String? {
    val word = wakeWord.trim()
    if (word.isEmpty() || !text.startsWith("$word ", ignoreCase = true)) {
        return null
    }
    return dropWakeWordPrefix(text, wakeWord).takeIf { it.isNotEmpty() }
}

/**
 * True when [text] is the wake word and nothing else, said once or several times in a row, or
 * with one [UNKNOWN_WORD] after it: "компьютер [unk]" is the wake word and a sound the recognizer
 * did not know (a breath, a noise), so the panel listens for the command.
 */
fun isWakeWordOnly(text: String, wakeWord: String): Boolean {
    val word = wakeWord.trim()
    return word.isNotEmpty() &&
        text.trim().startsWith(word, ignoreCase = true) &&
        dropWakeWordPrefix(text, wakeWord).isEmpty()
}

/**
 * [text] without the wake word at its start and without one [UNKNOWN_WORD] right after it, then
 * without the wake word said again: "компьютер [unk] датчики" and "компьютер [unk] компьютер
 * датчики" give "датчики". On the panel the recognizer often wrote [unk] between the wake word and
 * the command, for a breath or a word it does not know ("компьютер покажи датчики"); the rest is
 * the command as said. Only one [UNKNOWN_WORD] is left out, so longer unknown speech between the
 * wake word and a command still does not run it. Text that does not start with the wake word is
 * returned trimmed.
 */
fun dropWakeWordPrefix(text: String, wakeWord: String): String {
    val word = wakeWord.trim()
    val trimmed = text.trim()
    if (word.isEmpty() || !(trimmed.equals(word, ignoreCase = true) || trimmed.startsWith("$word ", ignoreCase = true))) {
        return trimmed
    }
    return dropWakeWordsAndOneUnknownWord(trimmed, wakeWord)
}

/**
 * The command readings of an utterance said after the wake word on its own (in two steps), best
 * first. In each reading a wake word said again at the start is left out, with one [UNKNOWN_WORD]
 * right after it or at the start ("компьютер [unk] таймер на пять минут" and "[unk] датчики" give
 * "таймер на пять минут" and "датчики"): the wake word and the chime are just over, so these are
 * the wake word repeated and a sound the recognizer did not know. Other readings with nothing left
 * and repeats are left out. As in one breath, the best reading decides: when it has nothing left (a
 * noise heard as [UNKNOWN_WORD]), the result is the best reading as heard, which is no command (the
 * app shows it and plays the error chime), and no other reading can turn that noise into a
 * command. The wake word said again keeps the panel listening and never gets here (see
 * [interpretUtterance]).
 */
fun twoStepCandidates(hypotheses: List<String>, wakeWord: String): List<String> {
    val best = hypotheses.firstOrNull() ?: return emptyList()
    if (dropWakeWordsAndOneUnknownWord(best.trim(), wakeWord).isEmpty()) {
        return listOf(best)
    }
    return hypotheses
        .map { dropWakeWordsAndOneUnknownWord(it.trim(), wakeWord) }
        .filter { it.isNotEmpty() }
        .distinct()
}

/** What an utterance means to the panel; see [interpretUtterance]. */
sealed interface Utterance {
    /** Not for the panel: no wake word at the start of the best reading. */
    data object Ignored : Utterance

    /** The wake word alone: the panel chimes and listens for the command (two steps). */
    data object WakeWord : Utterance

    /** A command, its readings best first (processCommand runs the first that is a command). */
    data class Command(val candidates: List<String>) : Utterance
}

/**
 * What the recognizer's readings of one utterance ([hypotheses], best first) mean, with
 * [isListening] true after the wake word alone. The best reading decides: the wake word alone
 * (also while listening: said again, also after one [UNKNOWN_WORD], it keeps the panel listening;
 * it used to end the listening as a failed command, so the command said after it was lost), a
 * command in two steps while
 * listening ([twoStepCandidates]), the wake word and a command in one breath
 * ([oneBreathCandidates]), or nothing for the panel. One exception keeps a command that only the
 * other readings heard: when the best reading is the wake word with an [UNKNOWN_WORD] after it
 * ("компьютер [unk]") and another reading has a command after the wake word ("компьютер
 * датчики"), that command runs; the [UNKNOWN_WORD] was it, heard badly. A reading counts only
 * when [isCommand] says its text is a command (processCommand's matching in the app; without it,
 * any text without an [UNKNOWN_WORD]): "компьютер [unk] [unk]" or "компьютер в" in another
 * reading leave the panel listening. A best reading of the wake word alone, without the
 * [UNKNOWN_WORD], always means listening. Pure function, used by SpeechRecognitionService.
 */
fun interpretUtterance(
    hypotheses: List<String>,
    wakeWord: String,
    isListening: Boolean,
    isCommand: (String) -> Boolean = { !it.contains(UNKNOWN_WORD) }
): Utterance {
    val best = hypotheses.firstOrNull() ?: return Utterance.Ignored
    if (isWakeWordOnly(best, wakeWord)) {
        if (dropLeadingWakeWords(best, wakeWord).isNotEmpty()) {
            val others = hypotheses.drop(1)
                .mapNotNull { commandAfterWakeWord(it, wakeWord) }
                .filter { !it.contains(UNKNOWN_WORD) && isCommand(it) }
                .distinct()
            if (others.isNotEmpty()) {
                return Utterance.Command(others)
            }
        }
        return Utterance.WakeWord
    }
    if (isListening) {
        // The wake word said again after a sound ("[unk] компьютер": a breath, the end of the
        // chime) keeps listening too; a sound alone ("[unk]") is still a failed command
        val trimmed = best.trim()
        if (dropWakeWordsAndOneUnknownWord(trimmed, wakeWord).isEmpty() && dropLeadingUnknownWord(trimmed).isNotEmpty()) {
            return Utterance.WakeWord
        }
        return Utterance.Command(twoStepCandidates(hypotheses, wakeWord))
    }
    return oneBreathCandidates(hypotheses, wakeWord)?.let { Utterance.Command(it) } ?: Utterance.Ignored
}

/**
 * [text] without the wake words at its start, then one [UNKNOWN_WORD], then the wake words again:
 * never more than one [UNKNOWN_WORD].
 */
private fun dropWakeWordsAndOneUnknownWord(text: String, wakeWord: String): String =
    dropLeadingWakeWords(dropLeadingUnknownWord(dropLeadingWakeWords(text, wakeWord)), wakeWord)

/** [text] without one [UNKNOWN_WORD] at its start. */
private fun dropLeadingUnknownWord(text: String): String = when {
    text == UNKNOWN_WORD -> ""
    text.startsWith("$UNKNOWN_WORD ") -> text.substring(UNKNOWN_WORD.length).trim()
    else -> text
}

/** [text] without the wake word at its start, however often it is said: "компьютер компьютер свет" -> "свет". */
fun dropLeadingWakeWords(text: String, wakeWord: String): String {
    val word = wakeWord.trim()
    var rest = text.trim()
    if (word.isEmpty()) {
        return rest
    }
    while (rest.equals(word, ignoreCase = true) || rest.startsWith("$word ", ignoreCase = true)) {
        rest = rest.substring(word.length).trim()
    }
    return rest
}

/**
 * The command readings of an utterance said in one breath with the wake word, best first, or null
 * when the best reading does not start with the wake word followed by a command. The recognizer's
 * best reading alone decides whether the wake word was said; the other readings only offer other
 * readings of the command (one may lack an [unk] that the best has). Readings without a command,
 * and repeats, are left out.
 */
fun oneBreathCandidates(hypotheses: List<String>, wakeWord: String): List<String>? {
    val best = hypotheses.firstOrNull() ?: return null
    commandAfterWakeWord(best, wakeWord) ?: return null
    return hypotheses.mapNotNull { commandAfterWakeWord(it, wakeWord) }.distinct()
}

/**
 * [command] without [prefix] and the space after it, or null when [command] does not start with
 * [prefix] followed by a space, or nothing follows it. "таймер на пять минут" with the prefix
 * "таймер" gives "на пять минут"; the page name "таймер" alone gives null.
 */
fun commandAfterPrefix(command: String, prefix: String): String? {
    val word = prefix.trim()
    if (word.isEmpty() || !command.startsWith("$word ", ignoreCase = true)) {
        return null
    }
    return command.substring(word.length + 1).trim().takeIf { it.isNotEmpty() }
}
