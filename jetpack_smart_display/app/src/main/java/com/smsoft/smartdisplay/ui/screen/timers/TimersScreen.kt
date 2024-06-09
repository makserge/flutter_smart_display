package com.smsoft.smartdisplay.ui.screen.timers

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.TimerState
import com.smsoft.smartdisplay.data.VoiceCommand
import com.smsoft.smartdisplay.data.VoiceCommandType
import com.smsoft.smartdisplay.data.database.entity.emptyTimer
import com.smsoft.smartdisplay.ui.common.AddItemButton
import com.smsoft.smartdisplay.ui.common.NoItems
import com.smsoft.smartdisplay.ui.composable.timers.ItemsList
import com.smsoft.smartdisplay.ui.composable.timers.TimerAlert
import com.smsoft.smartdisplay.ui.composable.timers.UpdateItem
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimersScreen(
    modifier: Modifier = Modifier,
    command: VoiceCommand,
    onResetCommand: () -> Unit,
    viewModel: TimersViewModel = hiltViewModel()
) {
    val items by viewModel.getAll.collectAsStateWithLifecycle(
        initialValue = null
    )
    val timerState: Map<Long, TimerState> by viewModel.timerState.collectAsStateWithLifecycle()
    val timerFinishedState: TimerState by viewModel.timerFinishedState.collectAsStateWithLifecycle()

    var isModifyItems by remember { mutableStateOf(false) }
    var isOpenEditItemDialog by remember { mutableStateOf(false) }
    var currentEditItem by remember { mutableStateOf(emptyTimer) }

    val coroutineScope = rememberCoroutineScope()
    if (command.type != VoiceCommandType.CLOCK) {
        LaunchedEffect(command.timeStamp) {
            command.let {
                coroutineScope.launch {
                    viewModel.processVoiceCommand(it!!.type)
                    onResetCommand()
                }
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
                        itemsTick = viewModel.timerTickMap,
                        itemsState = timerState,
                        editMode = isModifyItems,
                        onChangeItemState = { item ->
                            viewModel.changeItemState(item)
                        },
                        onResetItemState = { item ->
                            viewModel.resetItemState(item)
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
                        title = stringResource(R.string.no_timers_items),
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
                        currentEditItem = emptyTimer
                    },
                    onUpdateItem = { item ->
                        viewModel.updateItem(item)
                        currentEditItem = emptyTimer
                    }
                )
            }
            if (timerFinishedState is TimerState.Finished) {
                TimerAlert(
                    modifier = Modifier,
                    duration = timerFinishedState.timer.duration,
                    onDismiss = {
                        viewModel.cancelTimerAlert(timerFinishedState.timer)
                    }
                )
            }
        },
        floatingActionButton = {
            if (isModifyItems) {
                AddItemButton(
                    modifier = Modifier,
                    contentDescription = stringResource(R.string.add_timer),
                    onClick = {
                        isOpenEditItemDialog = true
                    }
                )
            }
        }
    )
}

