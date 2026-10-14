package com.smsoft.smartdisplay.ui.composable.alarms

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.pow


@Composable
fun DayOfWeek(
    modifier: Modifier,
    days: Int
) {
    val daysLabel = mutableListOf<String>()
    for (index in 1..7 step 1) {
        val pow = 2.0.pow(index.toDouble()).toInt()
        if (days > 0 && days and pow != 0) {
            daysLabel.add(DayOfWeek.of(index).getDisplayName(TextStyle.SHORT, Locale.UK).take(2))
        }
    }
    // The whole week fits on one line beside the edit buttons on the 4 inch panel, with a few dp to
    // spare. Two lines are the fallback, broken evenly (not "Su" alone on the second line).
    Text(
        modifier = Modifier,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        text = daysLabel.joinToString(", "),
        style = MaterialTheme.typography.h6.copy(lineBreak = LineBreak.Heading),
        color = MaterialTheme.colors.primary
    )
}