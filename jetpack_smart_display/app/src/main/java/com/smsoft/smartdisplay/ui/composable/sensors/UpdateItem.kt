package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.database.entity.Sensor

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UpdateItem(
    modifier: Modifier,
    item: Sensor,
    onCloseDialog: () -> Unit,
    onUpdateItem: (item: Sensor) -> Unit
) {
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

    val titleFocusRequester = FocusRequester()
    val topic1FocusRequester = FocusRequester()
    val topic2FocusRequester = FocusRequester()
    val topic3FocusRequester = FocusRequester()
    val topic4FocusRequester = FocusRequester()

    AlertDialog(
        modifier = Modifier
            .padding(20.dp),
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        onDismissRequest = onCloseDialog,
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
                TextFieldWithIcon(
                    modifier = Modifier,
                    title = stringResource(R.string.add_sensor_title),
                    topic = title,
                    unitTitle = "",
                    unit = "",
                    icon = titleIcon,
                    focusRequester = titleFocusRequester,
                    initialFocus = true,
                    showUnit = false,
                    onTopicChange = { value ->
                        title = value
                    },
                    onUnitChange = {
                    },
                    onIconChange = { value ->
                        titleIcon = value
                    }
                )
                Spacer(
                    modifier = Modifier.height(5.dp)
                )
                TextFieldWithIcon(
                    modifier = Modifier,
                    title = stringResource(R.string.add_sensor_topic1),
                    topic = topic1,
                    unitTitle = stringResource(R.string.add_sensor_unit),
                    unit = topic1Unit,
                    icon = topic1Icon,
                    focusRequester = topic1FocusRequester,
                    initialFocus = false,
                    onTopicChange = { value ->
                        topic1 = value
                    },
                    onUnitChange = { value ->
                        topic1Unit = value
                    },
                    onIconChange = { value ->
                        topic1Icon = value
                    }
                )
                Spacer(
                    modifier = Modifier.height(5.dp)
                )
                TextFieldWithIcon(
                    modifier = Modifier,
                    title = stringResource(R.string.add_sensor_topic2),
                    topic = topic2,
                    unitTitle = stringResource(R.string.add_sensor_unit),
                    unit = topic2Unit,
                    icon = topic2Icon,
                    focusRequester = topic2FocusRequester,
                    initialFocus = false,
                    onTopicChange = { value ->
                        topic2 = value
                    },
                    onUnitChange = { value ->
                        topic2Unit = value
                    },
                    onIconChange = { value ->
                        topic2Icon = value
                    }
                )
                Spacer(
                    modifier = Modifier.height(5.dp)
                )
                TextFieldWithIcon(
                    modifier = Modifier,
                    title = stringResource(R.string.add_sensor_topic3),
                    topic = topic3,
                    unitTitle = stringResource(R.string.add_sensor_unit),
                    unit = topic3Unit,
                    icon = topic3Icon,
                    focusRequester = topic3FocusRequester,
                    initialFocus = false,
                    onTopicChange = { value ->
                        topic3 = value
                    },
                    onUnitChange = { value ->
                        topic3Unit = value
                    },
                    onIconChange = { value ->
                        topic3Icon = value
                    }
                )
                Spacer(
                    modifier = Modifier.height(5.dp)
                )
                TextFieldWithIcon(
                    modifier = Modifier,
                    title = stringResource(R.string.add_sensor_topic4),
                    topic = topic4,
                    unitTitle = stringResource(R.string.add_sensor_unit),
                    unit = topic4Unit,
                    icon = topic4Icon,
                    focusRequester = topic4FocusRequester,
                    initialFocus = false,
                    onTopicChange = { value ->
                        topic4 = value
                    },
                    onUnitChange = { value ->
                        topic4Unit = value
                    },
                    onIconChange = { value ->
                        topic4Icon = value
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                modifier = Modifier,
                onClick = {
                    if (!checkField(title, titleFocusRequester)) {
                        return@TextButton
                    }
                    if (!checkField(topic1, topic1FocusRequester)) {
                        return@TextButton
                    }
                    onCloseDialog()
                    val sensor = Sensor(
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
                        topic4Icon = topic4Icon
                    )
                    onUpdateItem(sensor)
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
                onClick = onCloseDialog
            ) {
                Text(
                    modifier = Modifier,
                    text = stringResource(R.string.add_sensor_cancel_button)
                )
            }
        }
    )
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