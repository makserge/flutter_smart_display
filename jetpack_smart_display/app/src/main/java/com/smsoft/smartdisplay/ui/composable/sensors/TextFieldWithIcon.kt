package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R

@Composable
fun TextFieldWithIcon(
    modifier: Modifier,
    title: String,
    topic: String,
    unitTitle: String = "",
    unit: String = "",
    icon: String,
    focusRequester: FocusRequester,
    showUnit: Boolean = true,
    onTopicChange: (String) -> Unit,
    onUnitChange: (String) -> Unit = {},
    onIconChange: (String) -> Unit,
) {
    // The fields take their own height and the topic takes the width the others leave: a fixed
    // 52 dp row and a fixed 230 dp topic cut the text off in the larger dialog text, and were
    // wider than the dialog on the 4 inch panel. The icon field is as tall as the text fields.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        OutlinedTextField(
            modifier = Modifier
                .weight(1F)
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
                    .width(76.dp),
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
        Spacer(
            modifier = Modifier
                .width(5.dp)
        )
        IconPicker(
            modifier = Modifier
                .width(ICON_PICKER_WIDTH)
                .fillMaxHeight(),
            value = icon,
            onValueChange = onIconChange,
        )
    }
}

// The icon and the drop-down arrow, at any text size
private val ICON_PICKER_WIDTH = 72.dp

@Preview
@Composable
fun TextFieldWithIconPreview() {
    var title = ""
    var titleIcon = ""
    val titleFocusRequester = FocusRequester()

    TextFieldWithIcon(
        modifier = Modifier,
        title = stringResource(R.string.add_sensor_title),
        topic = title,
        icon = titleIcon,
        focusRequester = titleFocusRequester,
        showUnit = false,
        onTopicChange = { value ->
            title = value
        },
        onIconChange = {
            titleIcon = it
        }
    )
}

