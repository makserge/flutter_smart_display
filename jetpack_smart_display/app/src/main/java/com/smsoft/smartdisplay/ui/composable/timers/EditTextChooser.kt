package com.smsoft.smartdisplay.ui.composable.timers

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.smsoft.smartdisplay.ui.common.TextEntryScreen
import com.smsoft.smartdisplay.ui.common.prefs.TextPref

/**
 * A row showing [value]; a tap opens a full screen that edits it (TextEntryScreen). Save hands the
 * new text to [onChange]; Cancel leaves the value as it was (the pop-up it replaces applied every
 * key at once, so Cancel did not undo anything).
 */
@Composable
fun EditTextChooser(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    onChange: ((String) -> Unit) = {}
) {
    val textColor = MaterialTheme.colors.onBackground
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf(value) }

    TextPref(
        title = title,
        summary = value,
        modifier = modifier,
        textColor = textColor,
        enabled = true,
        onClick = {
            text = value
            showDialog = true
        },
    )
    if (showDialog) {
        TextEntryScreen(
            title = title,
            value = text,
            onValueChange = { text = it },
            onSave = {
                onChange(text)
                showDialog = false
            },
            onDismissRequest = { showDialog = false }
        )
    }
}