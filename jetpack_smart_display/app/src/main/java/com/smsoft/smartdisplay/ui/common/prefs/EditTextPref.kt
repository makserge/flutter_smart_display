package com.smsoft.smartdisplay.ui.common.prefs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.DialogProperties
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.R

/**
 * Text preference. Storage: `stringPreferencesKey(key)`, written only when Save is pressed
 * (Save always writes the field content, even if unchanged); Cancel/dismiss writes nothing.
 * The row shows [summary] as given by the caller, not the stored text.
 */
@Composable
fun EditTextPref(
    key: String,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    dialogTitle: String? = null,
    dialogMessage: String? = null,
    defaultValue: String = "",
    onValueSaved: (String) -> Unit = {},
    onValueChange: (String) -> Unit = {},
    dialogBackgroundColor: Color = MaterialTheme.colors.background,
    textColor: Color = MaterialTheme.colors.onBackground,
    enabled: Boolean = true,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val selectionKey = remember(key) { stringPreferencesKey(key) }
    val scope = rememberCoroutineScope()
    val dataStore = LocalPrefsDataStore.current
    val prefs by rememberPreferences()
    val value = prefs?.get(selectionKey) ?: defaultValue
    var textVal by remember(key) { mutableStateOf(value) }
    var dialogSize by remember { mutableStateOf(Size.Zero) }

    TextPref(
        title = title,
        modifier = modifier,
        summary = summary,
        textColor = textColor,
        enabled = enabled,
        onClick = {
            if (enabled) {
                textVal = value
                showDialog = !showDialog
            }
        },
    )

    if (showDialog) {
        // Also covers a dialog restored by rememberSaveable after recreation.
        LaunchedEffect(Unit) { textVal = value }
        AlertDialog(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .onGloballyPositioned { dialogSize = it.size.toSize() },
            onDismissRequest = { showDialog = false },
            buttons = {
                Column(verticalArrangement = Arrangement.SpaceBetween) {
                    DialogHeader(dialogTitle = dialogTitle, dialogMessage = dialogMessage)
                    OutlinedTextField(
                        value = textVal,
                        onValueChange = {
                            textVal = it
                            onValueChange(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .weight(1f, fill = false),
                    )
                    Row(
                        modifier = Modifier.width(with(LocalDensity.current) { dialogSize.width.toDp() }),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            modifier = Modifier.padding(end = 16.dp),
                            onClick = { showDialog = false },
                        ) {
                            Text(text = stringResource(R.string.cancel), style = MaterialTheme.typography.body1)
                        }
                        TextButton(
                            modifier = Modifier.padding(end = 16.dp),
                            onClick = {
                                val newValue = textVal
                                scope.launchPrefWrite(key) {
                                    dataStore.edit { it[selectionKey] = newValue }
                                    onValueSaved(newValue)
                                }
                                showDialog = false
                            },
                        ) {
                            Text(text = stringResource(R.string.save_button), style = MaterialTheme.typography.body1)
                        }
                    }
                }
            },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            backgroundColor = dialogBackgroundColor,
        )
    }
}

@Composable
private fun DialogHeader(dialogTitle: String?, dialogMessage: String?) {
    Column(modifier = Modifier.padding(16.dp)) {
        if (dialogTitle != null) {
            Text(text = dialogTitle, style = MaterialTheme.typography.h6)
        }
        if (dialogMessage != null) {
            Text(text = dialogMessage, style = MaterialTheme.typography.subtitle1)
        }
    }
}
