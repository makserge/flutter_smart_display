package com.smsoft.smartdisplay.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.ui.common.prefs.TextPref

/** A row showing the label of [value]; a tap opens a full screen with all [entries] (see [ChoiceScreen]). */
@Composable
fun ListChooser(
    modifier: Modifier = Modifier,
    title: String,
    entries: Map<String, String>,
    value: String,
    onChange: ((String) -> Unit)
) {
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
        // Stays open after a choice, so the alarm tones and stations can be tried one after another
        ChoiceScreen(
            title = title,
            entries = entryList,
            selected = value,
            onSelect = {
                if (it != value) {
                    onChange(it)
                }
            },
            onDismissRequest = {
                showDialog = false
            }
        )
    }
}