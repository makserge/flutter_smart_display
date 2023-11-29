package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.data.BluetoothDevice

@Composable
fun DevicesList(
    modifier: Modifier,
    items: List<BluetoothDevice>,
    selectedItem: BluetoothDevice,
    onClick: (item: BluetoothDevice) -> Unit
) {
    LazyColumn(
        modifier = Modifier
    ) {
        items(items) { item ->
            DeviceItem(
                modifier = Modifier,
                item = item,
                selected = item.address == selectedItem.address,
                onClick = onClick
            )
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}