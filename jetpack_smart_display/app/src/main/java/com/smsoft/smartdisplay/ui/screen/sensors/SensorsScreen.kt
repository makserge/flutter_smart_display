package com.smsoft.smartdisplay.ui.screen.sensors

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsoft.smartdisplay.data.MQTTData
import com.smsoft.smartdisplay.data.database.entity.emptySensor
import com.smsoft.smartdisplay.ui.composable.sensors.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SensorsScreen(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit,
    viewModel: SensorsViewModel = hiltViewModel()
) {
    val items by viewModel.getAll.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    var isModifyItems by remember { mutableStateOf(false) }
    var isOpenEditItemDialog by remember { mutableStateOf(false) }
    var currentEditItem by remember { mutableStateOf(emptySensor) }
    val itemsData: MQTTData by viewModel.mqttTopicData.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            if (viewModel.onStart()) {
                withContext(Dispatchers.Main) {
                    onSettingsClick()
                }
            }
        }
    }
    DisposableEffect(key1 = viewModel) {
        onDispose {
            viewModel.onStop()
        }
    }

    Scaffold(
        modifier = Modifier
            .combinedClickable(
                onClick = onSettingsClick,
                onLongClick = {
                    isModifyItems = !isModifyItems
                }
            ),
        content = { padding ->
            ItemsList(
                modifier = Modifier,
                padding = padding,
                items = items,
                itemsData = itemsData,
                editMode = isModifyItems,
                onDeleteItem = { item ->
                    viewModel.deleteItem(item)
                }
            ) { item ->
                currentEditItem = item
                isOpenEditItemDialog = true
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
