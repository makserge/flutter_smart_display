package com.smsoft.smartdisplay.ui.screen.sensors

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.MQTTData
import com.smsoft.smartdisplay.data.database.entity.emptySensor
import com.smsoft.smartdisplay.ui.composable.sensors.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SensorsScreen(
    modifier: Modifier = Modifier,
    viewModel: SensorsViewModel = hiltViewModel()
) {
    val items by viewModel.getAll.collectAsStateWithLifecycle(
        initialValue = null
    )
    var isModifyItems by remember { mutableStateOf(false) }
    var isOpenEditItemDialog by remember { mutableStateOf(false) }
    var currentEditItem by remember { mutableStateOf(emptySensor) }
    val itemsData: MQTTData by viewModel.mqttTopicData.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .padding(10.dp)
            .combinedClickable(
                onClick = { },
                onLongClick = {
                    isModifyItems = !isModifyItems
                }
            ),
        content = { padding ->
            if (items != null) {
                if (items!!.isNotEmpty()) {
                    ItemsList(
                        modifier = Modifier,
                        padding = padding,
                        items = items!!,
                        itemsData = itemsData,
                        editMode = isModifyItems,
                        onDeleteItem = { item ->
                            viewModel.deleteItem(item)
                        }
                    ) { item ->
                        currentEditItem = item
                        isOpenEditItemDialog = true
                    }
                } else {
                    NoItems(
                        modifier = Modifier
                    ) {
                        isOpenEditItemDialog = true
                    }
                }
            }
            if (isOpenEditItemDialog) {
                UpdateItem(
                    modifier = Modifier,
                    item = currentEditItem,
                    onCloseDialog = {
                        isOpenEditItemDialog = false
                    },
                    onUpdateItem = { item ->
                        if (item.id > 0) {
                            viewModel.updateItem(item)
                        } else {
                            viewModel.addItem(item)
                        }
                        currentEditItem = emptySensor
                    }
                )
            }
        },
        floatingActionButton = {
            if (isModifyItems) {
                AddItemButton(
                    modifier = Modifier,
                    onClick = {
                        isOpenEditItemDialog = true
                    }
                )
            }
        }
    )
}

@Composable
fun NoItems(
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box (
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit){
                detectTapGestures(
                    onTap = {
                        onClick()
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier,
                text = stringResource(R.string.no_sensors_items),
                color = MaterialTheme.colors.primary
            )
        }
    }
}
