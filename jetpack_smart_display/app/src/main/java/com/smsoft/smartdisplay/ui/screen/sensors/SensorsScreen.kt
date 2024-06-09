package com.smsoft.smartdisplay.ui.screen.sensors

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.MQTTData
import com.smsoft.smartdisplay.data.database.entity.emptySensor
import com.smsoft.smartdisplay.ui.common.AddItemButton
import com.smsoft.smartdisplay.ui.common.NoItems
import com.smsoft.smartdisplay.ui.composable.sensors.BluetoothPermissions
import com.smsoft.smartdisplay.ui.composable.sensors.ItemsList
import com.smsoft.smartdisplay.ui.composable.sensors.UpdateItem

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
    val bluetoothSensorsList by viewModel.bluetoothSensorsList.collectAsStateWithLifecycle(
        initialValue = null
    )
    var isShowBluetoothPermissions by remember { mutableStateOf(false) }
    if (bluetoothSensorsList != null && bluetoothSensorsList!!.isNotEmpty()) {
        isShowBluetoothPermissions = true
        DisposableEffect(null) {
            onDispose {
                viewModel.stopBleScan()
            }
        }
    }

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
                        modifier = Modifier,
                        title = stringResource(R.string.no_sensors_items),
                    ) {
                        isOpenEditItemDialog = true
                    }
                }
            }
            if (isOpenEditItemDialog) {
                UpdateItem(
                    modifier = Modifier,
                    item = currentEditItem,
                    viewModel = viewModel,
                    onCloseDialog = {
                        isOpenEditItemDialog = false
                        currentEditItem = emptySensor
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
            if (isShowBluetoothPermissions) {
                isShowBluetoothPermissions = false
                BluetoothPermissions(
                    modifier = Modifier,
                    isEnabled = viewModel.isBluetoothEnabled(),
                    onAllowed = {
                        viewModel.startBleScan()
                    },
                    onDenied = {
                        viewModel.stopBleScan()
                    }
                )
            }
        },
        floatingActionButton = {
            if (isModifyItems) {
                AddItemButton(
                    modifier = Modifier,
                    contentDescription = stringResource(R.string.add_sensor),
                    onClick = {
                        isOpenEditItemDialog = true
                    }
                )
            }
        }
    )
}

