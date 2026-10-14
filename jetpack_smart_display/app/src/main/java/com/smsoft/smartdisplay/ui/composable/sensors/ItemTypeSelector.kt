package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.SensorType

/**
 * The type of a new sensor, one choice per type side by side. It was a narrow drop-down field
 * beside the title, which broke "MQTT" into two lines in the larger dialog text.
 */
@Composable
fun ItemTypeSelector(
    modifier: Modifier,
    value: SensorType,
    onValueChange: (String) -> Unit = {}
) {
    Row(
        modifier = modifier
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .padding(end = 8.dp),
            text = stringResource(R.string.type)
        )
        SensorType.entries.forEach { type ->
            Row(
                modifier = Modifier
                    .selectable(
                        selected = (type == value),
                        onClick = {
                            onValueChange(type.id)
                        },
                        role = Role.RadioButton
                    )
                    // A whole choice is a comfortable touch target, not only its small circle
                    .heightIn(min = 48.dp)
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (type == value),
                    onClick = null
                )
                Text(
                    modifier = Modifier
                        .padding(start = 4.dp),
                    text = stringResource(type.titleId)
                )
            }
        }
    }
}

@Preview
@Composable
fun ItemTypeSelectorPreview() {
    ItemTypeSelector(
        modifier = Modifier,
        value = SensorType.getDefault(),
        onValueChange = {
        },
    )
}