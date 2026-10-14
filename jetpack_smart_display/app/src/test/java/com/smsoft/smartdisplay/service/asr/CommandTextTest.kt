package com.smsoft.smartdisplay.service.asr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The wake word and the command in one breath, and the words that no command phrase has. */
class CommandTextTest {

    @Test
    fun commandAfterTheWakeWordInOneBreath() {
        assertEquals("включи свет", commandAfterWakeWord("джарвис включи свет", "джарвис"))
        assertEquals("включи свет", commandAfterWakeWord("Джарвис включи свет", "джарвис"))
    }

    @Test
    fun noCommandWithoutWakeWordOrWithoutCommand() {
        assertNull(commandAfterWakeWord("джарвис", "джарвис"))
        assertNull(commandAfterWakeWord("джарвис   ", "джарвис"))
        assertNull(commandAfterWakeWord("включи свет", "джарвис"))
        // A word that only starts like the wake word is not the wake word
        assertNull(commandAfterWakeWord("джарвисс включи свет", "джарвис"))
        assertNull(commandAfterWakeWord("джарвис включи свет", ""))
    }

    @Test
    fun aRepeatedWakeWordIsDroppedBeforeTheCommand() {
        assertEquals(
            "подсветка на восемьдесят процентов",
            commandAfterWakeWord("компьютер компьютер подсветка на восемьдесят процентов", "компьютер")
        )
        assertEquals("включи свет", commandAfterWakeWord("джарвис джарвис включи свет", "джарвис"))
        // Only wake words and nothing after them is no command
        assertNull(commandAfterWakeWord("компьютер компьютер", "компьютер"))
    }

    @Test
    fun theWakeWordAloneIsWakeWordOnlyHoweverOftenItIsSaid() {
        assertTrue(isWakeWordOnly("джарвис", "джарвис"))
        assertTrue(isWakeWordOnly("Джарвис Джарвис", "джарвис"))
        assertFalse(isWakeWordOnly("джарвис включи свет", "джарвис"))
        assertFalse(isWakeWordOnly("джарвисс", "джарвис"))
        assertFalse(isWakeWordOnly("", "джарвис"))
        assertFalse(isWakeWordOnly("джарвис", ""))
    }

    @Test
    fun oneUnknownWordRightAfterTheWakeWordIsDropped() {
        // From the panel's log: [unk] between the wake word and the command
        assertEquals("датчики", commandAfterWakeWord("компьютер [unk] датчики", "компьютер"))
        assertEquals(
            "таймер на двадцать минут",
            commandAfterWakeWord("компьютер [unk] таймер на двадцать минут", "компьютер")
        )
        // The wake word said again after the [unk]
        assertEquals("датчики", commandAfterWakeWord("компьютер [unk] компьютер датчики", "компьютер"))
        assertEquals("датчики", commandAfterWakeWord("компьютер компьютер [unk] датчики", "компьютер"))
        // Only one: longer unknown speech before a command keeps its second [unk], so it is no command
        assertEquals("[unk] датчики", commandAfterWakeWord("компьютер [unk] [unk] датчики", "компьютер"))
        // An [unk] inside the command stays
        assertEquals(
            "подсветка [unk] десять процентов",
            commandAfterWakeWord("компьютер подсветка [unk] десять процентов", "компьютер")
        )
        // Without the wake word nothing is dropped, and the wake word with an [unk] only is no command
        assertNull(commandAfterWakeWord("[unk] датчики", "компьютер"))
        assertNull(commandAfterWakeWord("как [unk] датчики", "компьютер"))
        assertNull(commandAfterWakeWord("компьютер [unk]", "компьютер"))
        // A word that only starts like the marker is not the marker
        assertEquals("[unk]x датчики", commandAfterWakeWord("компьютер [unk]x датчики", "компьютер"))
    }

    @Test
    fun theWakeWordWithOneUnknownWordIsWakeWordOnly() {
        assertTrue(isWakeWordOnly("компьютер [unk]", "компьютер"))
        assertTrue(isWakeWordOnly("компьютер [unk] компьютер", "компьютер"))
        assertFalse(isWakeWordOnly("компьютер [unk] [unk]", "компьютер"))
        assertFalse(isWakeWordOnly("[unk] компьютер", "компьютер"))
        assertFalse(isWakeWordOnly("[unk]", "компьютер"))
    }

    @Test
    fun twoStepCandidatesDropTheWakeWordSaidAgainAndOneUnknownWord() {
        // From the panel's log: after "компьютер" alone, the wake word was said again with the command
        assertEquals(
            listOf("таймер на пять минут"),
            twoStepCandidates(listOf("компьютер [unk] таймер на пять минут"), "компьютер")
        )
        assertEquals(listOf("датчики"), twoStepCandidates(listOf("компьютер датчики"), "компьютер"))
        assertEquals(listOf("датчики"), twoStepCandidates(listOf("Компьютер компьютер датчики"), "компьютер"))
        // An [unk] at the start (a sound after the chime)
        assertEquals(listOf("датчики"), twoStepCandidates(listOf("[unk] датчики"), "компьютер"))
        assertEquals(listOf("датчики"), twoStepCandidates(listOf("[unk] компьютер датчики"), "компьютер"))
        // Never more than one [unk]
        assertEquals(
            listOf("[unk] датчики"),
            twoStepCandidates(listOf("[unk] компьютер [unk] датчики"), "компьютер")
        )
        // A command as said, with its other readings, is unchanged
        assertEquals(listOf("выключи", "выключить"), twoStepCandidates(listOf("выключи", "выключить"), "компьютер"))
        assertEquals(
            listOf("подсветка [unk] десять процентов"),
            twoStepCandidates(listOf("подсветка [unk] десять процентов"), "компьютер")
        )
        // Other readings with nothing left are left out, repeats too
        assertEquals(
            listOf("датчики"),
            twoStepCandidates(listOf("компьютер датчики", "компьютер", "датчики"), "компьютер")
        )
        // The best reading decides: with nothing left in it (the wake word again, or a noise), it
        // is returned as heard, which is no command, and no other reading turns it into one
        assertEquals(
            listOf("компьютер"),
            twoStepCandidates(listOf("компьютер", "компьютер датчики", "датчики"), "компьютер")
        )
        assertEquals(listOf("[unk]"), twoStepCandidates(listOf("[unk]", "[unk] радио"), "компьютер"))
        assertEquals(listOf("компьютер"), twoStepCandidates(listOf("компьютер", "[unk]"), "компьютер"))
        assertEquals(emptyList<String>(), twoStepCandidates(emptyList(), "компьютер"))
        // No wake word set: only the [unk] is dropped
        assertEquals(listOf("датчики"), twoStepCandidates(listOf("[unk] датчики"), ""))
    }

    @Test
    fun oneBreathCandidatesComeFromEveryReadingButTheBestDecides() {
        // The best reading has an [unk] right after the wake word: the command without it
        assertEquals(
            listOf("датчики"),
            oneBreathCandidates(listOf("компьютер [unk] датчики", "компьютер датчики"), "компьютер")
        )
        // The best reading has an [unk] inside the command; the second reading of the same audio is clean
        assertEquals(
            listOf("подсветка [unk] десять процентов", "подсветка на десять процентов"),
            oneBreathCandidates(
                listOf("компьютер подсветка [unk] десять процентов", "компьютер подсветка на десять процентов"),
                "компьютер"
            )
        )
        // From the panel's log: the wake word heard as "как" in the best reading is no wake word,
        // even when another reading has it
        assertNull(
            oneBreathCandidates(
                listOf("как [unk] подсветка [unk] на восемьдесят процентов", "компьютер подсветка на восемьдесят процентов"),
                "компьютер"
            )
        )
        // The wake word with an [unk] only is no one-breath command (it is the wake word alone)
        assertNull(oneBreathCandidates(listOf("компьютер [unk]", "компьютер датчики"), "компьютер"))
        // Readings without the wake word or without a command are left out, repeats too
        assertEquals(
            listOf("включи свет"),
            oneBreathCandidates(listOf("компьютер включи свет", "включи свет", "компьютер", "компьютер включи свет"), "компьютер")
        )
        // No wake word in the best reading: not a command, whatever the others say
        assertNull(oneBreathCandidates(listOf("датчики", "компьютер датчики"), "компьютер"))
        // The wake word alone is not a one-breath command
        assertNull(oneBreathCandidates(listOf("компьютер", "компьютер датчики"), "компьютер"))
        assertNull(oneBreathCandidates(emptyList(), "компьютер"))
    }

    @Test
    fun anUtteranceIsIgnoredAWakeWordOrACommand() {
        val wake = "компьютер"
        // From the panel's log, one breath: the [unk] after the wake word is left out
        assertEquals(
            Utterance.Command(listOf("таймер на двадцать минут")),
            interpretUtterance(listOf("компьютер [unk] таймер на двадцать минут"), wake, isListening = false)
        )
        // From the panel's log, two steps: the wake word alone, then the wake word again with the command
        assertEquals(Utterance.WakeWord, interpretUtterance(listOf("компьютер"), wake, isListening = false))
        assertEquals(
            Utterance.Command(listOf("таймер на пять минут")),
            interpretUtterance(listOf("компьютер [unk] таймер на пять минут"), wake, isListening = true)
        )
        // From the panel's log: the wake word heard as "как" in the best reading is not for the panel
        assertEquals(
            Utterance.Ignored,
            interpretUtterance(
                listOf("как [unk] подсветка [unk] на восемьдесят процентов", "компьютер подсветка на восемьдесят процентов"),
                wake,
                isListening = false
            )
        )
        assertEquals(Utterance.Ignored, interpretUtterance(listOf("включи свет"), wake, isListening = false))
        assertEquals(Utterance.Ignored, interpretUtterance(emptyList(), wake, isListening = false))
        // The wake word with an [unk]: listening, unless another reading heard a command
        val commands = setOf("датчики", "включи свет")
        val isCommand = { text: String -> text in commands }
        assertEquals(Utterance.WakeWord, interpretUtterance(listOf("компьютер [unk]"), wake, isListening = false))
        assertEquals(
            Utterance.Command(listOf("датчики")),
            interpretUtterance(listOf("компьютер [unk]", "компьютер датчики"), wake, isListening = false, isCommand)
        )
        // Another reading that is no command leaves the panel listening: an [unk] in it, or a stray word
        assertEquals(
            Utterance.WakeWord,
            interpretUtterance(listOf("компьютер [unk]", "компьютер [unk] [unk]"), wake, isListening = false, isCommand)
        )
        assertEquals(
            Utterance.WakeWord,
            interpretUtterance(
                listOf("компьютер [unk]", "компьютер подсветка [unk] десять процентов"),
                wake,
                isListening = false
            )
        )
        assertEquals(
            Utterance.WakeWord,
            interpretUtterance(listOf("компьютер [unk]", "компьютер в"), wake, isListening = false, isCommand)
        )
        // Only the readings that are commands go along
        assertEquals(
            Utterance.Command(listOf("включи свет")),
            interpretUtterance(listOf("компьютер [unk]", "компьютер в", "компьютер включи свет"), wake, isListening = false, isCommand)
        )
        // The wake word alone always means listening, whatever the other readings say
        assertEquals(
            Utterance.WakeWord,
            interpretUtterance(listOf("компьютер", "компьютер датчики"), wake, isListening = false)
        )
        // Listening: a noise is no command, even when another reading has one
        assertEquals(
            Utterance.Command(listOf("[unk]")),
            interpretUtterance(listOf("[unk]", "[unk] радио"), wake, isListening = true)
        )
    }

    @Test
    fun theWakeWordSaidAgainWhileListeningKeepsListening() {
        val wake = "компьютер"
        val isCommand = { text: String -> text in setOf("датчики", "погода") }
        // From the panel's log (2026-10-13 19:35): "компьютер", "компьютер" again, then "погода". The
        // second wake word ended the listening as a failed command, so "погода" was ignored.
        assertEquals(Utterance.WakeWord, interpretUtterance(listOf("компьютер"), wake, isListening = true))
        assertEquals(Utterance.WakeWord, interpretUtterance(listOf("компьютер компьютер"), wake, isListening = true))
        assertEquals(Utterance.WakeWord, interpretUtterance(listOf("компьютер [unk]"), wake, isListening = true, isCommand))
        // ...also after a sound (a breath, the end of the chime)
        assertEquals(Utterance.WakeWord, interpretUtterance(listOf("[unk] компьютер"), wake, isListening = true))
        assertEquals(Utterance.WakeWord, interpretUtterance(listOf("[unk] компьютер компьютер"), wake, isListening = true))
        // A sound alone, or two, is still no wake word
        assertEquals(Utterance.Command(listOf("[unk]")), interpretUtterance(listOf("[unk]"), wake, isListening = true))
        assertEquals(
            Utterance.Command(listOf("[unk]")),
            interpretUtterance(listOf("[unk] компьютер [unk]"), wake, isListening = true)
        )
        // ...unless another reading heard a command after it, as without listening
        assertEquals(
            Utterance.Command(listOf("датчики")),
            interpretUtterance(listOf("компьютер [unk]", "компьютер датчики"), wake, isListening = true, isCommand)
        )
        // The wake word with a command while listening is still that command, a noise still no command
        assertEquals(
            Utterance.Command(listOf("погода")),
            interpretUtterance(listOf("компьютер погода"), wake, isListening = true)
        )
        assertEquals(Utterance.Command(listOf("[unk]")), interpretUtterance(listOf("[unk]"), wake, isListening = true))
    }

    @Test
    fun aPrefixIsSplitOffOnlyWithAWordAfterIt() {
        assertEquals("на пять минут", commandAfterPrefix("таймер на пять минут", "таймер"))
        assertEquals("на пять минут", commandAfterPrefix("Таймер  на пять минут", "таймер"))
        // The page name alone is not a prefix match
        assertNull(commandAfterPrefix("таймер", "таймер"))
        assertNull(commandAfterPrefix("таймер ", "таймер"))
        // A longer word that starts like the prefix is not the prefix
        assertNull(commandAfterPrefix("таймеры", "таймер"))
        assertNull(commandAfterPrefix("на пять минут", "таймер"))
    }
}
