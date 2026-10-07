package com.smsoft.smartdisplay.ui.common.prefs

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit

/**
 * Boolean preference. Storage: `booleanPreferencesKey(key)`, written immediately on every
 * toggle; nothing is written while the user has not touched it ([defaultChecked] is display-only).
 */
@Composable
fun SwitchPref(
    key: String,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    defaultChecked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    textColor: Color = MaterialTheme.colors.onBackground,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val selectionKey = remember(key) { booleanPreferencesKey(key) }
    val scope = rememberCoroutineScope()
    val dataStore = LocalPrefsDataStore.current
    val prefs by rememberPreferences()
    val checked = prefs?.get(selectionKey) ?: defaultChecked

    // The new state is computed inside edit {} so two quick taps toggle twice instead of
    // both writing the same stale value.
    fun write(newState: (current: Boolean) -> Boolean) {
        scope.launchPrefWrite(key) {
            var written = defaultChecked
            dataStore.edit { preferences ->
                written = newState(preferences[selectionKey] ?: defaultChecked)
                preferences[selectionKey] = written
            }
            onCheckedChange?.invoke(written)
        }
    }

    TextPref(
        title = title,
        modifier = modifier,
        summary = summary,
        darkenOnDisable = true,
        onClick = { write { current -> !current } },
        textColor = textColor,
        enabled = enabled,
        leadingIcon = leadingIcon,
    ) {
        Switch(
            checked = checked,
            onCheckedChange = { value -> write { value } },
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary),
        )
    }
}
