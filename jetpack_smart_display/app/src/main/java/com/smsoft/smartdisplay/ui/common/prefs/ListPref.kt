package com.smsoft.smartdisplay.ui.common.prefs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.R

/**
 * Single-choice preference. Storage: `stringPreferencesKey(key)` holding the selected entry's
 * map KEY (not its label), written when an entry is picked; the dialog closes after the write.
 */
@Composable
fun ListPref(
    key: String,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    defaultValue: String? = null,
    onValueChange: ((String) -> Unit)? = null,
    useSelectedAsSummary: Boolean = false,
    dialogBackgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(dialogBackgroundColor),
    textColor: Color = MaterialTheme.colors.onBackground,
    selectionColor: Color = MaterialTheme.colors.primary,
    buttonColor: Color = MaterialTheme.colors.primary,
    enabled: Boolean = true,
    entries: Map<String, String> = emptyMap(),
) {
    val entryList = remember(entries) { entries.toList() }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val selectionKey = remember(key) { stringPreferencesKey(key) }
    val scope = rememberCoroutineScope()
    val dataStore = LocalPrefsDataStore.current
    val prefs by rememberPreferences()
    val selected = prefs?.get(selectionKey) ?: defaultValue

    fun select(entryKey: String) {
        scope.launchPrefWrite(key) {
            dataStore.edit { it[selectionKey] = entryKey }
            onValueChange?.invoke(entryKey)
            showDialog = false
        }
    }

    TextPref(
        title = title,
        modifier = modifier,
        summary = when {
            useSelectedAsSummary && selected != null -> entries[selected]
            useSelectedAsSummary -> "Not Set"
            else -> summary
        },
        textColor = textColor,
        enabled = true,
        onClick = { if (enabled) showDialog = !showDialog },
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            text = {
                Column {
                    Text(
                        modifier = Modifier.padding(vertical = 16.dp),
                        text = title,
                    )
                    LazyColumn {
                        items(entryList) { (entryKey, label) ->
                            val isSelected = selected == entryKey
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = isSelected,
                                        onClick = { if (!isSelected) select(entryKey) },
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { if (!isSelected) select(entryKey) },
                                    colors = RadioButtonDefaults.colors(selectedColor = selectionColor),
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.body2,
                                    color = textColor,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.body1,
                        color = buttonColor,
                    )
                }
            },
            backgroundColor = dialogBackgroundColor,
            contentColor = contentColor,
            properties = DialogProperties(usePlatformDefaultWidth = true),
        )
    }
}
