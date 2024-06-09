package com.smsoft.smartdisplay.ui.screen.alarms

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.database.entity.emptyAlarm
import com.smsoft.smartdisplay.ui.common.AddItemButton
import com.smsoft.smartdisplay.ui.common.NoItems
import com.smsoft.smartdisplay.ui.composable.alarms.AlarmAlert
import com.smsoft.smartdisplay.ui.composable.alarms.ItemsList
import com.smsoft.smartdisplay.ui.composable.alarms.UpdateItem

@UnstableApi
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlarmsScreen(
    modifier: Modifier = Modifier,
    viewModel: AlarmsViewModel = hiltViewModel()
) {
    val items by viewModel.getAll.collectAsStateWithLifecycle(
        initialValue = null
    )
    var isModifyItems by remember { mutableStateOf(false) }
    var isOpenEditItemDialog by remember { mutableStateOf(false) }
    var currentEditItem by remember { mutableStateOf(emptyAlarm) }
    val alarmState = viewModel.alarmState.collectAsStateWithLifecycle()

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
                        editMode = isModifyItems,
                        onChangeItemState = { item, state ->
                            viewModel.changeItemState(item, state)
                        },
                        onEnableItemModification = {
                            isModifyItems = !isModifyItems
                        },
                        onDeleteItem = { item ->
                            viewModel.deleteItem(item)
                        }
                    ) { item ->
                        currentEditItem = item
                        isOpenEditItemDialog = true
                    }
                } else {
                    isModifyItems = false
                    NoItems(
                        modifier = Modifier,
                        title = stringResource(R.string.no_alarms_items),
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
                        currentEditItem = emptyAlarm
                        viewModel.stopPlayer()
                    },
                    onUpdateItem = { item ->
                        if (!viewModel.checkAlarmPermissions()) {
                            return@UpdateItem
                        }
                        viewModel.updateItem(item)
                        currentEditItem = emptyAlarm
                        viewModel.stopPlayer()
                    }
                )
            }
            if (alarmState.value != emptyAlarm) {
                AlarmAlert(
                    modifier = Modifier,
                    time = alarmState.value.time,
                    onDismiss = {
                        viewModel.cancelAlarm(alarmState.value)
                    }
                )
            }
        },
        floatingActionButton = {
            if (isModifyItems) {
                AddItemButton(
                    modifier = Modifier,
                    contentDescription = stringResource(R.string.add_alarm),
                    onClick = {
                        isOpenEditItemDialog = true
                    }
                )
            }
        }
    )
}

