package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.BluetoothDevice
import com.smsoft.smartdisplay.data.SensorType
import com.smsoft.smartdisplay.data.database.entity.Sensor
import com.smsoft.smartdisplay.data.emptyBluetoothDevice
import com.smsoft.smartdisplay.ui.screen.sensors.SensorsViewModel
import com.smsoft.smartdisplay.utils.getBluetoothDeviceByType
import com.smsoft.smartdisplay.utils.getSensorByBluetoothType

@Composable
fun UpdateItem(
    modifier: Modifier,
    item: Sensor,
    viewModel: SensorsViewModel,
    onCloseDialog: () -> Unit,
    onUpdateItem: (item: Sensor) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(item.title) }
    var titleIcon by remember { mutableStateOf(item.titleIcon) }
    var topic1 by remember { mutableStateOf(item.topic1) }
    var topic1Unit by remember { mutableStateOf(item.topic1Unit) }
    var topic1Icon by remember { mutableStateOf(item.topic1Icon) }
    var topic2 by remember { mutableStateOf(item.topic2) }
    var topic2Unit by remember { mutableStateOf(item.topic2Unit) }
    var topic2Icon by remember { mutableStateOf(item.topic2Icon) }
    var topic3 by remember { mutableStateOf(item.topic3) }
    var topic3Unit by remember { mutableStateOf(item.topic3Unit) }
    var topic3Icon by remember { mutableStateOf(item.topic3Icon) }
    var topic4 by remember { mutableStateOf(item.topic4) }
    var topic4Unit by remember { mutableStateOf(item.topic4Unit) }
    var topic4Icon by remember { mutableStateOf(item.topic4Icon) }
    var type by remember { mutableStateOf(item.type) }

    val titleFocusRequester = FocusRequester()
    val topic1FocusRequester = FocusRequester()

    var isShowBlePermissions by remember { mutableStateOf(SensorType.isBluetooth(type)) }
    val bleScanState = viewModel.bleScanState.collectAsStateWithLifecycle()
    var selectedBleDevice by remember { mutableStateOf(
            if (SensorType.isBluetooth(type) && (item.id > 0)) {
                getBluetoothDeviceByType(
                    type = item.topic3,
                    item = item
                )
            } else {
                emptyBluetoothDevice
            }
        )
    }

    AlertDialog(
        modifier = Modifier
            .padding(20.dp),
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        onDismissRequest = {
            onClose(
                type = type,
                viewModel = viewModel,
                onCloseDialog = onCloseDialog
            )
        },
        title = {
            Text(
                modifier = Modifier,
                text = stringResource(if (item.id > 0) R.string.edit_sensor else R.string.add_sensor)
            )
        },
        text = {
            Column(
                modifier = Modifier,
            ) {
                Text(
                    modifier = Modifier
                        .height(1.dp),
                    text = ""
                )
                Row(
                    modifier = Modifier,
                ) {
                    TextFieldWithIcon(
                        modifier = Modifier,
                        context = context,
                        title = stringResource(R.string.add_sensor_title),
                        topic = title,
                        icon = titleIcon,
                        type = type,
                        focusRequester = titleFocusRequester,
                        initialFocus = true,
                        showUnit = false,
                        showType = (item.id == 0L),
                        onTopicChange = {
                            title = it
                        },
                        onIconChange = {
                            titleIcon = it
                        },
                        onTypeChange = {
                            type = it
                            isShowBlePermissions = SensorType.isBluetooth(type)
                        },
                    )
                }
                Spacer(
                    modifier = Modifier.height(5.dp)
                )
                if (type == SensorType.BLUETOOTH.id) {
                    Spacer(modifier = Modifier.height(5.dp))
                    BluetoothDevices(
                        modifier = Modifier,
                        state = bleScanState,
                        selectedItem = selectedBleDevice,
                        onClick = {
                            selectedBleDevice = it
                        },
                        onRescan = {
                            viewModel.startBleScan()
                        }
                    )
                } else {
                    UpdateMQTTTopics(
                        modifier = Modifier,
                        context = context,
                        topic1 = topic1,
                        topic1Unit = topic1Unit,
                        topic1Icon = topic1Icon,
                        topic1FocusRequester = topic1FocusRequester,
                        topic2 = topic2,
                        topic2Unit = topic2Unit,
                        topic2Icon = topic2Icon,
                        topic3 = topic3,
                        topic3Unit = topic3Unit,
                        topic3Icon = topic3Icon,
                        topic4 = topic4,
                        topic4Unit = topic4Unit,
                        topic4Icon = topic4Icon,
                        onTopic1Change = {
                            topic1 = it
                        },
                        onUnit1Change = {
                            topic1Unit = it
                        },
                        onIcon1Change = {
                            topic1Icon = it
                        },
                        onTopic2Change = {
                            topic2 = it
                        },
                        onUnit2Change = {
                            topic2Unit = it
                        },
                        onIcon2Change = {
                            topic2Icon = it
                        },
                        onTopic3Change = {
                            topic3 = it
                        },
                        onUnit3Change = {
                            topic3Unit = it
                        },
                        onIcon3Change = {
                            topic3Icon = it
                        },
                        onTopic4Change = {
                            topic4 = it
                        },
                        onUnit4Change = {
                            topic4Unit = it
                        },
                        onIcon4Change = {
                            topic4Icon = it
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                modifier = Modifier,
                onClick = {
                    if (!checkRequiredFields(
                            type = type,
                            title = title,
                            titleFocusRequester = titleFocusRequester,
                            topic1 = topic1,
                            topic1FocusRequester = topic1FocusRequester,
                            selectedBleDevice = selectedBleDevice
                        )) {
                        return@TextButton
                    }
                    val sensor = if (SensorType.isBluetooth(type)) {
                        getSensorByBluetoothType(
                            id = item.id,
                            title = title,
                            type = selectedBleDevice.deviceName,
                            address = selectedBleDevice.address
                        )
                    } else {
                        Sensor(
                            id = item.id,
                            title = title,
                            titleIcon = titleIcon,
                            topic1 = topic1,
                            topic1Unit = topic1Unit,
                            topic1Icon = topic1Icon,
                            topic2 = topic2,
                            topic2Unit = topic2Unit,
                            topic2Icon = topic2Icon,
                            topic3 = topic3,
                            topic3Unit = topic3Unit,
                            topic3Icon = topic3Icon,
                            topic4 = topic4,
                            topic4Unit = topic4Unit,
                            topic4Icon = topic4Icon,
                            type = type
                        )
                    }
                    onUpdateItem(sensor)
                    onClose(
                        type = type,
                        viewModel = viewModel,
                        onCloseDialog = onCloseDialog
                    )
                }
            ) {
                Text(
                    modifier = Modifier,
                    text = stringResource(if (item.id > 0) R.string.add_sensor_update_button else R.string.add_sensor_add_button)
                )
            }
        },
        dismissButton = {
            TextButton(
                modifier = Modifier,
                onClick = {
                    onClose(
                        type = type,
                        viewModel = viewModel,
                        onCloseDialog = onCloseDialog
                    )
                }
            ) {
                Text(
                    modifier = Modifier,
                    text = stringResource(R.string.add_sensor_cancel_button)
                )
            }
        }
    )
    if (isShowBlePermissions) {
        BluetoothPermissions(
            modifier = Modifier,
            isEnabled = viewModel.isBluetoothEnabled(),
            onAllowed = {
                isShowBlePermissions = false
                viewModel.startBleScan()
            },
            onDenied = {
                isShowBlePermissions = false
                viewModel.stopBleScan()
            }
        )
    }
}

private fun onClose(
    type: String,
    viewModel: SensorsViewModel,
    onCloseDialog: () -> Unit
) {
    if (SensorType.isBluetooth(type)) {
        viewModel.stopBleScan()
    }
    onCloseDialog()
}

private fun checkRequiredFields(
    type: String,
    title: String,
    titleFocusRequester: FocusRequester,
    topic1: String,
    topic1FocusRequester: FocusRequester,
    selectedBleDevice: BluetoothDevice
): Boolean {
    if (SensorType.isBluetooth(type)) {
        if (!checkField(title, titleFocusRequester)) {
            return false
        }
        if (selectedBleDevice.address.isEmpty()) {
            return false
        }
    } else {
        if (!checkField(title, titleFocusRequester)) {
            return false
        }
        if (!checkField(topic1, topic1FocusRequester)) {
            return false
        }
    }
    return true
}

private fun checkField(
    value: String,
    focusRequester: FocusRequester
): Boolean {
    if (value.isNotEmpty()) {
        return true
    }
    focusRequester.requestFocus()
    return false
}