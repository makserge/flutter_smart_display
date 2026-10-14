package com.smsoft.smartdisplay.ui.common.prefs

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
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
import com.smsoft.smartdisplay.ui.common.ChoiceScreen

/**
 * Single-choice preference. Storage: `stringPreferencesKey(key)` holding the selected entry's
 * map KEY (not its label), written when an entry is picked; the full-screen list (ChoiceScreen)
 * closes after the write.
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
    textColor: Color = MaterialTheme.colors.onBackground,
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
        ChoiceScreen(
            title = title,
            entries = entryList,
            selected = selected,
            onSelect = { entryKey ->
                if (entryKey == selected) {
                    showDialog = false
                } else {
                    select(entryKey)
                }
            },
            onDismissRequest = { showDialog = false },
        )
    }
}
