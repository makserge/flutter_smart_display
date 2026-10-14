package com.smsoft.smartdisplay.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * The add button in a strip of its own below a list, for Scaffold's bottomBar slot. It used to
 * float over the list and covered the delete button of the card under it.
 *
 * Scaffold lays its content out under the bottom bar and only reports the bar's height in its
 * padding, so the list must take that padding as Modifier.padding: as contentPadding, the cards
 * would scroll under the strip again.
 */
@Composable
fun AddItemBar(
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // The end padding puts the button's right edge in line with the cards' (their padding)
            .padding(top = 4.dp, end = 4.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        AddItemButton(
            contentDescription = contentDescription,
            onClick = onClick
        )
    }
}

/**
 * Keeps a list that was at its end at its end when edit mode adds the [AddItemBar] below it. A list
 * that gets shorter keeps its first visible card, so the last card, the one long-pressed to delete
 * it, went under the list's new edge with its buttons. A list that fits the page counts as at its
 * end: with the strip it may no longer fit.
 */
@Composable
fun KeepListEndInEditMode(listState: LazyListState, editMode: Boolean) {
    val isEditMode by rememberUpdatedState(editMode)
    var wasAtEnd by remember { mutableStateOf(false) }
    LaunchedEffect(listState) {
        // Edit mode is read here too, so leaving it records where the list is then
        snapshotFlow { isEditMode to !listState.canScrollForward }
            .collect { (inEditMode, atEnd) ->
                if (!inEditMode) {
                    wasAtEnd = atEnd
                }
            }
    }
    // Requested here, the scroll is applied by the measure that makes room for the strip; from an
    // effect it came a frame later, after a frame with the last card cut
    val previousEditMode = remember { mutableStateOf(editMode) }
    SideEffect {
        val count = listState.layoutInfo.totalItemsCount
        if (editMode && !previousEditMode.value && wasAtEnd && (count > 0)) {
            listState.requestScrollToItem(count - 1)
        }
        previousEditMode.value = editMode
    }
}

@Composable
fun AddItemButton(
    modifier: Modifier = Modifier,
    contentDescription: String,
    onClick: () -> Unit
) {
    FloatingActionButton(
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.primary,
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier,
            imageVector = Icons.Default.Add,
            contentDescription = contentDescription
        )
    }
}