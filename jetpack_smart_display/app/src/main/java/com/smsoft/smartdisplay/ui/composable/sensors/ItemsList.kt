package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
    onEditItem: (item: Sensor) -> Unit
) {
    val itemsDataValue = itemsData.lastUpdated
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize(),
        columns = GridCells.Fixed(2),
        contentPadding = padding,
        content = {
            items(items.size) { index ->
                Item(
                    modifier = Modifier,
                    item = items[index],
                    itemsData = itemsData.value,
                    editMode = editMode,
                    onDelete = onDeleteItem,
                    onOptionsClick = onEditItem,
                )
            }
        }
    )
}