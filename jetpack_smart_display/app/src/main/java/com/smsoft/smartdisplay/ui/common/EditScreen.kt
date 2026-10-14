package com.smsoft.smartdisplay.ui.common

import androidx.compose.runtime.Composable

/**
 * The screen that adds or edits an alarm, a timer or a sensor, with the slots of Material's
 * AlertDialog. It was a pop-up dialog; it is now a full screen (see [BarScreen]): [dismissButton]
 * (Cancel), the [title] and [confirmButton] (Add or Update) in a bar at the top that stays above
 * the on-screen keyboard, and the rows of [text] below it, scrolling.
 */
@Composable
fun EditScreen(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit
) {
    BarScreen(
        onDismissRequest = onDismissRequest,
        title = title,
        dismissButton = dismissButton,
        confirmButton = confirmButton
    ) {
        text()
    }
}
