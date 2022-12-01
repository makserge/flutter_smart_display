package com.smsoft.smartdisplay.ui.composable.sensors

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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.job

@Composable
fun TextFieldWithIcon(
    modifier: Modifier,
    title: String,
    topic: String,
    unitTitle: String,
    unit: String,
    icon: String,
    focusRequester: FocusRequester,
    initialFocus: Boolean,
    showUnit: Boolean = true,
    onTopicChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onIconChange: (String) -> Unit
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
                .padding(
                    start = 5.dp
                ),
            value = icon,
            onValueChange = { value->
                onIconChange(value)
            },
        )
    }
    if (initialFocus) {
        LaunchedEffect(Unit) {
            coroutineContext.job.invokeOnCompletion {
                focusRequester.requestFocus()
            }
        }
    }
}

