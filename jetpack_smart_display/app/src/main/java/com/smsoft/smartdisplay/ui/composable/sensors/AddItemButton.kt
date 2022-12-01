package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R

@Composable
fun AddItemButton(
    modifier: Modifier,
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
            contentDescription = stringResource(R.string.add_sensor)
        )
    }
}