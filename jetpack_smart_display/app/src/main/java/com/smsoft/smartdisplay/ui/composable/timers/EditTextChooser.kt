package com.smsoft.smartdisplay.ui.composable.timers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.DialogProperties
import com.jamal.composeprefs.ui.prefs.TextPref
import com.smsoft.smartdisplay.R

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun EditTextChooser(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    onChange: ((String) -> Unit) = {}
) {
    val dialogBackgroundColor = MaterialTheme.colors.background
    val textColor = MaterialTheme.colors.onBackground
    var dialogSize by remember { mutableStateOf(Size.Zero) }
    var showDialog by rememberSaveable { mutableStateOf(false) }

    TextPref(
        title = title,
        summary = value,
        modifier = modifier,
        textColor = textColor,
        enabled = true,
        onClick = { showDialog = !showDialog },
    )
    if (showDialog) {
        AlertDialog(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .onGloballyPositioned {
                    dialogSize = it.size.toSize()
                },
            onDismissRequest = { showDialog = false },
            buttons = {
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    OutlinedTextField(
                        value = value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .weight(1f, fill = false),
                        onValueChange = {
                            onChange(it)
                        }
                    )

                    Row(
                        modifier = Modifier.width(with(LocalDensity.current) { dialogSize.width.toDp() }),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            modifier = Modifier.padding(end = 16.dp),
                            onClick = { showDialog = false }
                        ) {
                            Text(
                                modifier = Modifier,
                                text = stringResource(R.string.add_sensor_cancel_button)
                            )
                        }
                        TextButton(
                            modifier = Modifier.padding(end = 16.dp),
                            onClick = {
                                showDialog = false
                            }
                        ) {
                            Text(
                                modifier = Modifier,
                                text = stringResource(R.string.save_button)
                            )
                        }
                    }
                }
            },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            backgroundColor = dialogBackgroundColor,
        )
    }
}