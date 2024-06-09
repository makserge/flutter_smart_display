package com.smsoft.smartdisplay.ui.composable.alarms

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.database.entity.Alarm
import com.smsoft.smartdisplay.utils.formatTime

@OptIn(ExperimentalFoundationApi::class)
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
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (editMode) onEdit(item) },
                onLongClick = { onEnableModification() }
            ),
        elevation = 8.dp,
        shape = RoundedCornerShape(10.dp),
        backgroundColor = MaterialTheme.colors.surface,
    ) {
        Row(
            modifier = Modifier
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1F)
            ) {
                Text(
                    modifier = Modifier,
                    text = formatTime(item.time),
                    style = MaterialTheme.typography.h2,
                    color = MaterialTheme.colors.primary
                )
                Spacer(
                    modifier = Modifier.height(10.dp)
                )
                DayOfWeek(
                    modifier = Modifier,
                    days = item.days
                )
            }
            if (editMode) {
                Icon(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterVertically)
                        .clickable(
                            onClick = {
                                onDelete(item)
                            }
                        ),
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete)
                )
            } else {
                Switch(
                    modifier = Modifier
                        .scale(1.5f)
                        .align(Alignment.CenterVertically),
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
            }
            Spacer(Modifier.width(50.dp))
        }
    }
}