package com.smsoft.smartdisplay.ui.composable.timers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import com.smsoft.smartdisplay.data.TimerState
import com.smsoft.smartdisplay.data.database.entity.Timer

@Composable
fun ItemsList(
    modifier: Modifier = Modifier,
    padding: PaddingValues,
    items: List<Timer>,
    itemsTick: SnapshotStateMap<Long, Long>,
    itemsState: Map<Long, TimerState>,
    editMode: Boolean,
    onDeleteItem: (item: Timer) -> Unit,
    onChangeItemState: (state: TimerState) -> Unit,
    onResetItemState: (item: Timer) -> Unit,
    onEnableItemModification: () -> Unit,
    onEditItem: (item: Timer) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = padding,
        content = {
            items(items.size) { index ->
                Item(
                    modifier = Modifier,
                    itemTick = if (itemsTick[items[index].id] != null) itemsTick[items[index].id]!! else 0,
                    itemState = if (itemsState[items[index].id] != null) itemsState[items[index].id]!! else TimerState.Idle(items[index]),
                    editMode = editMode,
                    onDelete = onDeleteItem,
                    onChangeState = onChangeItemState,
                    onResetState = onResetItemState,
                    onEnableModification = onEnableItemModification,
                    onEdit = onEditItem
                )
            }
        }
    )
}