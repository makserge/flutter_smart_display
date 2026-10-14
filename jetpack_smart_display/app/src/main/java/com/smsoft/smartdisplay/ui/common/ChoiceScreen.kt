package com.smsoft.smartdisplay.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R

/**
 * A full screen that picks one of [entries] (key to label), with a scroll bar when the list is
 * longer than the screen. A tap on a row calls [onSelect]; the arrow at the top left closes the
 * screen. [icon], if given, draws an icon for a key before its label. It replaces the pop-up
 * lists of the settings and the editors.
 */
@Composable
fun ChoiceScreen(
    title: String,
    entries: List<Pair<String, String>>,
    selected: String?,
    onSelect: (String) -> Unit,
    onDismissRequest: () -> Unit,
    icon: (@Composable (key: String) -> Unit)? = null
) {
    BarScreen(
        onDismissRequest = onDismissRequest,
        title = {
            BarTitle(text = title)
        },
        dismissButton = {
            BackButton(onClick = onDismissRequest)
        },
        // The list scrolls by itself
        scrollable = false
    ) {
        val listState = rememberLazyListState(
            initialFirstVisibleItemIndex = entries.indexOfFirst { it.first == selected }.coerceAtLeast(0)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollbar(listState),
            state = listState
        ) {
            items(entries) { (key, label) ->
                val isSelected = (key == selected)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .selectable(
                            selected = isSelected,
                            onClick = { onSelect(key) },
                            role = Role.RadioButton
                        )
                        .padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary)
                    )
                    if (icon != null) {
                        Box(modifier = Modifier.padding(end = 12.dp)) {
                            icon(key)
                        }
                    }
                    Text(text = label)
                }
            }
        }
    }
}

/** The arrow at the top left of a full screen that closes it. */
@Composable
fun BackButton(
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.cancel),
            tint = MaterialTheme.colors.onBackground
        )
    }
}
