package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.smsoft.smartdisplay.data.BluetoothDevice
import com.smsoft.smartdisplay.service.ble.BluetoothScanState

@Composable
internal fun BluetoothDevices(
    modifier: Modifier,
    state: State<BluetoothScanState>,
    selectedItem: BluetoothDevice,
    onClick: (item: BluetoothDevice) -> Unit,
    onRescan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(fraction = 0.6f),
    ) {
        when (state.value) {
            BluetoothScanState.Initial -> CircularProgressIndicator()
            is BluetoothScanState.Result -> {
                val items = (state.value as BluetoothScanState.Result).devices

                if (items.isNotEmpty()) {
                    DevicesList(
                        modifier = Modifier,
                        items = items,
                        selectedItem = selectedItem,
                        onClick = onClick
                    )
                } else {
                    NoDevices(
                        modifier = Modifier,
                        onClick = onRescan
                    )
                }
            }
            else -> {}
        }
    }
}