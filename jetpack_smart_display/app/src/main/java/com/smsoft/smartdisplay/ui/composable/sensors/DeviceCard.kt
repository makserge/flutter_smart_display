package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.data.BluetoothDevice

@Composable
fun DeviceItem(
    modifier: Modifier,
    item: BluetoothDevice,
    selected: Boolean,
    onClick: (item: BluetoothDevice) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick(item)
            },
        elevation = 8.dp,
        backgroundColor = if (selected) MaterialTheme.colors.secondary else MaterialTheme.colors.surface,
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(.15f)
                    .padding(end = 6.dp),
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    imageVector = Icons.Filled.SignalCellularAlt,
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = item.rssi.toString(),
                    color = MaterialTheme.colors.primary,
                    textAlign = TextAlign.Center
                )
            }
            Column(modifier = Modifier.fillMaxWidth(.9f)) {
                Text(
                    text = item.deviceName,
                    color = MaterialTheme.colors.primary
                )
                Text(
                    text = item.address,
                    color = MaterialTheme.colors.primary
                )
            }
        }
    }
}

@Preview
@Composable
fun DeviceItemPreview() {
    DeviceItem(
        modifier = Modifier,
        item = BluetoothDevice(
            deviceName = "deviceName",
            address = "93:58:00:27:XX:00",
            rssi = -56
        ),
        selected = false,
        onClick = {

        }
    )
}
