package com.smsoft.smartdisplay.ui.composable.timers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import com.smsoft.smartdisplay.data.TimerState
import com.smsoft.smartdisplay.data.database.entity.Timer
import com.smsoft.smartdisplay.ui.common.KeepListEndInEditMode
import com.smsoft.smartdisplay.ui.common.verticalScrollbar

@Composable
fun ItemsList(
    modifier: Modifier = Modifier,
    padding: PaddingValues,
    items: List<Timer>,
    itemsTick: SnapshotStateMap<Long, Long>,
    itemsState: Map<Long, TimerState>,
    editMode: Boolean,
    listState: LazyListState = rememberLazyListState(),
    onDeleteItem: (item: Timer) -> Unit,
    onChangeItemState: (state: TimerState) -> Unit,
    onResetItemState: (item: Timer) -> Unit,
    onEnableItemModification: () -> Unit,
    onEditItem: (item: Timer) -> Unit,
) {
    KeepListEndInEditMode(listState, editMode)
    // The padding holds the add strip's height in edit mode: the list ends above it (see AddItemBar)
    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .verticalScrollbar(listState),
        state = listState,
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