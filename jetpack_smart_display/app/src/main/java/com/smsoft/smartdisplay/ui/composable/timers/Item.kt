package com.smsoft.smartdisplay.ui.composable.timers

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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.TimerState
import com.smsoft.smartdisplay.data.database.entity.Timer

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Item(
    modifier: Modifier,
    itemTick: Long,
    itemState: TimerState,
    editMode: Boolean,
    onDelete: (item: Timer) -> Unit,
    onChangeState: (state: TimerState) -> Unit,
    onResetState: (item: Timer) -> Unit,
    onEnableModification: () -> Unit,
    onEdit: (item: Timer) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onEdit(itemState.timer) },
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
                val duration = when (itemState) {
                    is TimerState.Running -> itemTick
                    is TimerState.Paused -> itemState.tick
                    else -> itemState.timer.duration
                }
                val hours = duration / 3600
                val minutes = duration / 60 - (hours * 60)
                val seconds = duration - (hours * 3600) - (minutes * 60)
                Text(
                    modifier = Modifier,
                    text = "%d:%02d:%02d".format(hours, minutes, seconds),
                    style = MaterialTheme.typography.h2,
                    color = MaterialTheme.colors.primary
                )
                Spacer(
                    modifier = Modifier.height(10.dp)
                )
                Text(
                    modifier = Modifier,
                    text = itemState.timer.title,
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary
                )
            }
            if (itemState !is TimerState.Idle) {
                Icon(
                    modifier = Modifier
                        .size(70.dp)
                        .padding(
                            end = 10.dp
                        )
                        .align(Alignment.CenterVertically)
                        .clickable(
                            onClick = {
                                onResetState(itemState.timer)
                            }
                        ),
                    imageVector = Icons.Filled.Replay,
                    contentDescription = stringResource(R.string.reset_timer_button)
                )
            }
            Icon(
                modifier = Modifier
                    .size(80.dp)
                    .padding(
                        end = 10.dp
                    )
                    .align(Alignment.CenterVertically)
                    .clickable(
                        onClick = {
                            onChangeState(itemState)
                        }
                    ),
                imageVector = if ((itemState is TimerState.Idle) || (itemState is TimerState.Paused)) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                contentDescription = stringResource(R.string.start_timer_button)
            )
            Spacer(Modifier.width(25.dp))
            if (editMode) {
                Icon(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(
                            end = 10.dp
                        )
                        .align(Alignment.CenterVertically)
                        .clickable(
                            onClick = {
                                onDelete(itemState.timer)
                            }
                        ),
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete)
                )
            }
        }
    }
}