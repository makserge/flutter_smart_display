package com.smsoft.smartdisplay.ui.composable.clock.nightdream

import com.smsoft.smartdisplay.ui.screen.clock.displayHour

// The hour cards of the flip clock. A file of its own, without Android calls, so the mapping can
// be checked in a JVM unit test.

/** A flip card: its characters and the index of the one it shows. */
internal class FlipCard(
    val chars: CharArray,
    val index: Int
)

/**
 * The tens and the units card of [hour] (0-23). From one hour to the next both cards stay on
 * their index or step to the next one, so the change flips instead of jumping.
 */
internal fun hourCards(
    hour: Int,
    is24Hour: Boolean
): Pair<FlipCard, FlipCard> =
    if (is24Hour) {
        FlipCard(HOURS, hour / 10) to FlipCard(LOW_HOURS24, hour)
    } else {
        val tens = if (displayHour(hour, is24Hour = false) >= 10) 1 else 0
        FlipCard(HIGH_HOURS12, tens) to FlipCard(LOW_HOURS12, hour % 12)
    }

private val HOURS = charArrayOf('0', '1', '2')
// Indexed by the hour itself (0..23), so 23 -> 0 is a single forward step
private val LOW_HOURS24 = charArrayOf(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3'
)
// 12-hour format: a blank tens card below 10 (the card stays, so nothing moves); the units card
// is indexed by hour % 12 (12, 1 .. 11), so 11 -> 12 and 12 -> 1 are single forward steps, like
// 23 -> 0 in 24-hour format
private val HIGH_HOURS12 = charArrayOf(' ', '1')
private val LOW_HOURS12 = charArrayOf('2', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1')
