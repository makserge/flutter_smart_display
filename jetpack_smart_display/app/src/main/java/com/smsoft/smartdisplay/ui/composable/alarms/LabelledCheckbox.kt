package com.smsoft.smartdisplay.ui.composable.alarms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LabelledCheckBox(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(
                onClick = { onCheckedChange(!checked) }
            )
            .padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 10.dp)
    ) {
        Text(
            text = label
        )
        Spacer(Modifier.size(2.dp))
        Checkbox(
            checked = checked,
            onCheckedChange = null
        )
    }
}