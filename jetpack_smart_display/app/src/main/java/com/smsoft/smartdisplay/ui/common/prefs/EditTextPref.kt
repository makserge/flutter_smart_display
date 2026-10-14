package com.smsoft.smartdisplay.ui.common.prefs

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.ui.common.TextEntryScreen

/**
 * Text preference. Storage: `stringPreferencesKey(key)`, written only when Save is pressed
 * (Save always writes the field content, even if unchanged); Cancel/dismiss writes nothing.
 * The row shows [summary] as given by the caller, not the stored text. The text is edited on a
 * full screen (TextEntryScreen).
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
        // Also covers a screen restored by rememberSaveable after recreation.
        LaunchedEffect(Unit) { textVal = value }
        TextEntryScreen(
            title = dialogTitle ?: title,
            message = dialogMessage,
            value = textVal,
            onValueChange = {
                textVal = it
                onValueChange(it)
            },
            onSave = {
                val newValue = textVal
                scope.launchPrefWrite(key) {
                    dataStore.edit { it[selectionKey] = newValue }
                    onValueSaved(newValue)
                }
                showDialog = false
            },
            onDismissRequest = { showDialog = false },
        )
    }
}
