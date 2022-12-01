package com.smsoft.smartdisplay.ui.screen.sensors

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.smsoft.smartdisplay.data.MQTTData
import com.smsoft.smartdisplay.data.Screen
import com.smsoft.smartdisplay.data.database.entity.emptySensor
import com.smsoft.smartdisplay.ui.composable.sensors.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SensorsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: SensorsViewModel = hiltViewModel()
) {
    val items by viewModel.getAll.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    var isModifyItems by mutableStateOf(false)
    var isOpenEditItemDialog by mutableStateOf(false)
    var currentEditItem by mutableStateOf(emptySensor)
    val itemsData: MQTTData by viewModel.mqttTopicData.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            if (viewModel.onStart()) {
                withContext(Dispatchers.Main) {
                    navController.navigate(Screen.SensorsSettings.route)
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
        modifier = Modifier,
        topBar = {
            TopBar(
                modifier = Modifier,
                onEditClick = {
                    isModifyItems = !isModifyItems
                },
                onSettingsClick = {
                    navController.navigate(Screen.SensorsSettings.route)
                },
            )
        },
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
