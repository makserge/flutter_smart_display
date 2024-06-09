package com.smsoft.smartdisplay.ui.composable.alarms

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.smsoft.smartdisplay.data.database.entity.Alarm

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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = padding,
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