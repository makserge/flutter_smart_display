package com.smsoft.smartdisplay.ui.composable.alarms

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.smsoft.smartdisplay.data.database.entity.Alarm
import com.smsoft.smartdisplay.ui.common.KeepListEndInEditMode
import com.smsoft.smartdisplay.ui.common.verticalScrollbar

@Composable
fun ItemsList(
    modifier: Modifier = Modifier,
    padding: PaddingValues,
    items: List<Alarm>,
    editMode: Boolean,
    onDeleteItem: (item: Alarm) -> Unit,
    onChangeItemState: (item: Alarm, state: Boolean) -> Unit,
    onEnableItemModification: () -> Unit,
    onEditItem: (item: Alarm) -> Unit
) {
    val listState = rememberLazyListState()
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
                    item = items[index],
                    editMode = editMode,
                    onDelete = onDeleteItem,
                    onChangeState = onChangeItemState,
                    onEnableModification = onEnableItemModification,
                    onEdit = onEditItem
                )
            }
        }
    )
}