package com.smsoft.smartdisplay.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddItemButton(
    modifier: Modifier,
    contentDescription: String,
    onClick: () -> Unit
) {
    FloatingActionButton(
        modifier = Modifier
            .padding(10.dp),
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