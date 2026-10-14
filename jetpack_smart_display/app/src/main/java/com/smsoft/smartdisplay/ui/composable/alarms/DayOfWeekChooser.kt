package com.smsoft.smartdisplay.ui.composable.alarms

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.smsoft.smartdisplay.ui.common.COMPACT_SCREEN_DP
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.pow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayOfWeekChooser(
    modifier: Modifier,
    days: Int,
    onChange: (days: Int) -> Unit
) {
    var value by remember { mutableIntStateOf(days) }
    // On a narrow screen the weekdays and the weekend go on two lines, so no label is broken
    val narrow = LocalConfiguration.current.screenWidthDp < COMPACT_SCREEN_DP
    FlowRow(
        maxItemsInEachRow = if (narrow) 5 else 7
    ) {
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