package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import com.smsoft.smartdisplay.ui.common.KeepListEndInEditMode
import com.smsoft.smartdisplay.ui.common.verticalScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.smsoft.smartdisplay.data.MQTTData
import com.smsoft.smartdisplay.data.database.entity.Sensor

@Composable
fun ItemsList(
    modifier: Modifier = Modifier,
    padding: PaddingValues,
    items: List<Sensor>,
    itemsData: MQTTData,
    editMode: Boolean,
    onDeleteItem: (item: Sensor) -> Unit,
    onEnableItemModification: () -> Unit,
    onEditItem: (item: Sensor) -> Unit
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
                    itemsData = itemsData.value,
                    editMode = editMode,
                    onDelete = onDeleteItem,
                    onEnableModification = onEnableItemModification,
                    onOptionsClick = onEditItem,
                )
            }
        }
    )
}