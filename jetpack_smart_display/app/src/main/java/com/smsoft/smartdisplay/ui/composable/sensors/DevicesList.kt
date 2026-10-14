package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.data.BluetoothDevice

/**
 * The Bluetooth devices found, as rows of the sensor editor, which scrolls as a whole: a lazy list
 * inside it had no height to fill and crashed once devices were found.
 */
@Composable
fun DevicesList(
    modifier: Modifier,
    items: List<BluetoothDevice>,
    selectedItem: BluetoothDevice,
    onClick: (item: BluetoothDevice) -> Unit
) {
    Column(
        modifier = Modifier
    ) {
        items.forEach { item ->
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