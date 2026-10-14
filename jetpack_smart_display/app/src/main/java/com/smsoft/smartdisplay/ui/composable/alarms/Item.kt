package com.smsoft.smartdisplay.ui.composable.alarms

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.database.entity.Alarm
import com.smsoft.smartdisplay.ui.common.ItemCard
import com.smsoft.smartdisplay.utils.formatTime

@Composable
fun Item(
    modifier: Modifier,
    item: Alarm,
    editMode: Boolean,
    onDelete: (item: Alarm) -> Unit,
    onChangeState: (item: Alarm, state: Boolean) -> Unit,
    onEnableModification: () -> Unit,
    onEdit: (item: Alarm) -> Unit
) {
    // The time with its switch, then the days with the edit buttons at their end (ItemCard). The
    // switch stays in edit mode; the trash used to take its place.
    ItemCard(
        editMode = editMode,
        editDescription = stringResource(R.string.edit_alarm),
        onEnableModification = onEnableModification,
        onEdit = { onEdit(item) },
        onDelete = { onDelete(item) },
        rowSpacing = ALARM_ROW_SPACING,
        top = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1F),
                    text = formatTime(item.time),
                    style = MaterialTheme.typography.h2,
                    color = MaterialTheme.colors.primary
                )
                Switch(
                    modifier = Modifier
                        .scale(1.5f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colors.secondary,
                        checkedTrackColor = MaterialTheme.colors.secondary,
                        uncheckedThumbColor = MaterialTheme.colors.primary,
                        uncheckedTrackColor= MaterialTheme.colors.primary
                    ),
                    checked = item.isEnabled,
                    onCheckedChange = {
                        onChangeState(item, it)
                    }
                )
                // Room for the switch, which is scaled but laid out at its normal size
                Spacer(Modifier.width(SWITCH_END_SPACE))
            }
        },
        bottom = {
            DayOfWeek(
                modifier = Modifier,
                days = item.days
            )
        }
    )
}

// The scaled switch's end in line with the trash glyph below it
private val SWITCH_END_SPACE = 12.dp

// Keeps the trash at least 8 dp below the switch, whose touch area grows with its 1.5 scale, so a
// slip off the switch does not delete the alarm
private val ALARM_ROW_SPACING = 22.dp