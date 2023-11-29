package com.smsoft.smartdisplay.ui.composable.sensors

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.SensorType
import kotlinx.coroutines.job

@Composable
fun TextFieldWithIcon(
    modifier: Modifier,
    context: Context,
    title: String,
    topic: String,
    unitTitle: String = "",
    unit: String = "",
    icon: String,
    type: String = "",
    focusRequester: FocusRequester,
    initialFocus: Boolean,
    showUnit: Boolean = true,
    showType: Boolean = false,
    onTopicChange: (String) -> Unit,
    onUnitChange: (String) -> Unit = {},
    onIconChange: (String) -> Unit,
    onTypeChange: (String) -> Unit = {},
) {
    Row(
        modifier = Modifier
            .height(52.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier
                .width(230.dp)
                .focusRequester(focusRequester),
            value = topic,
            onValueChange = onTopicChange,
            singleLine = true,
            placeholder = {
                Text(
                    modifier = Modifier,
                    text = title
                )
            }
        )
        if (showUnit) {
            OutlinedTextField(
                modifier = Modifier
                    .width(65.dp),
                value = unit,
                onValueChange = onUnitChange,
                singleLine = true,
                placeholder = {
                    Text(
                        modifier = Modifier,
                        text = unitTitle
                    )
                }
            )
        }
        IconPicker(
            modifier = Modifier
                .width(if (showType) 90.dp else 110.dp)
                .padding(
                    start = 5.dp
                ),
            value = icon,
            onValueChange = {
                onIconChange(it)
            },
        )
        if (showType) {
            ItemTypeSelector(
                modifier = Modifier
                    .width(85.dp),
                context = context,
                value = SensorType.getById(type),
                onValueChange = {
                    onTypeChange(it)
                },
            )
        }
    }
    if (initialFocus) {
        LaunchedEffect(Unit) {
            coroutineContext.job.invokeOnCompletion {
                focusRequester.requestFocus()
            }
        }
    }
}

@Preview
@Composable
fun TextFieldWithIconPreview() {
    val context = LocalContext.current

    var title = ""
    var titleIcon = ""
    var type = "MQTT"
    val titleFocusRequester = FocusRequester()

    TextFieldWithIcon(
        modifier = Modifier,
        context = context,
        title = stringResource(R.string.add_sensor_title),
        topic = title,
        icon = titleIcon,
        type = type,
        focusRequester = titleFocusRequester,
        initialFocus = true,
        showType = true,
        onTopicChange = { value ->
            title = value
        },
        onUnitChange = {
        },
        onIconChange = {
            titleIcon = it
        },
        onTypeChange = {
            type = it
        }
    )
}

