package com.smsoft.smartdisplay.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R

/**
 * A full screen that asks one question: Cancel, the [title] and [confirmText] in the bar at the
 * top, [message] below. It replaces the pop-up questions (the permission requests).
 */
@Composable
fun QuestionScreen(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    BarScreen(
        onDismissRequest = onDismissRequest,
        title = {
            BarTitle(text = title)
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText)
            }
        }
    ) {
        Text(text = message)
    }
}

/**
 * A full screen that edits one text: Cancel, the [title] and Save in the bar at the top (above the
 * keyboard), then [message] if any and the field, which takes the focus so the keyboard opens at
 * once. The keyboard's done key saves as well. It replaces the pop-up text fields.
 */
@Composable
fun TextEntryScreen(
    title: String,
    message: String? = null,
    value: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    // The cursor at the end of the text, ready to change it
    var field by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    LaunchedEffect(value) {
        if (field.text != value) {
            field = TextFieldValue(value, TextRange(value.length))
        }
    }
    BarScreen(
        onDismissRequest = onDismissRequest,
        title = {
            BarTitle(text = title)
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(text = stringResource(R.string.save_button))
            }
        }
    ) {
        if (message != null) {
            Text(
                modifier = Modifier.padding(bottom = 12.dp),
                text = message,
                style = MaterialTheme.typography.body2
            )
        }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            value = field,
            onValueChange = {
                field = it
                onValueChange(it.text)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSave() })
        )
        // Here, next to the field: the screen is drawn at the root (FullScreenHost), a frame
        // later than where TextEntryScreen is called, and the field must exist to take the focus
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}
