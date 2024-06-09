package com.smsoft.smartdisplay.ui.composable.alarms

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.pow

@Composable
fun DayOfWeekChooser(
    modifier: Modifier,
    days: Int,
    onChange: (days: Int) -> Unit
) {
    var value by remember { mutableIntStateOf(days) }
    Row {
        for (index in 1..7 step 1) {
            val pow = 2.0.pow(index.toDouble()).toInt()
            LabelledCheckBox(
                modifier = Modifier,
                label = DayOfWeek.of(index).getDisplayName(TextStyle.SHORT, Locale.UK).take(2),
                checked = value > 0 && value and pow != 0,
                onCheckedChange = {
                    value = if (days and pow == 0) {
                        days or pow
                    } else {
                        (days or pow) - pow
                    }
                    onChange(value);
                }
            )
        }
    }
}