package com.smsoft.smartdisplay.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.jamal.composeprefs.ui.prefs.TextPref
import com.smsoft.smartdisplay.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListChooser(
    modifier: Modifier = Modifier,
    title: String,
    entries: Map<String, String>,
    value: String,
    onChange: ((String) -> Unit)
) {
    val dialogBackgroundColor = MaterialTheme.colors.surface
    val textColor = MaterialTheme.colors.onBackground

    val entryList = entries.toList()
    var showDialog by rememberSaveable { mutableStateOf(false) }

    TextPref(
        modifier = Modifier
            .padding(0.dp),
        title = title,
        summary = entries[value],
        textColor = textColor,
        enabled = true,
        onClick = {
            showDialog = !showDialog
        }
    )

    if (showDialog) {
        AlertDialog(
            modifier = Modifier,
            onDismissRequest = {
                showDialog = false
            },
            text = {
                Column {
                    Text(
                        modifier = Modifier.padding(vertical = 16.dp),
                        text = title
                    )
                    LazyColumn {
                        items(entryList) { item ->
                            val isSelected = value == item.first
                            val onSelected = {
                                onChange(item.first)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = isSelected,
                                        onClick = {
                                            if (!isSelected) onSelected()
                                        }
                                    ),
                                verticalAlignment = CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        if (!isSelected) onSelected()
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary)
                                )
                                Text(
                                    text = item.second,
                                    style = MaterialTheme.typography.body2,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    modifier = Modifier,
                    onClick = {
                        showDialog = false
                    },
                ) {
                    Text(
                        modifier = Modifier,
                        text = stringResource(R.string.add_sensor_cancel_button)
                    )
                }
            },
            backgroundColor = dialogBackgroundColor,
            contentColor = contentColorFor(dialogBackgroundColor),
            properties = DialogProperties(usePlatformDefaultWidth = true)
        )
    }
}