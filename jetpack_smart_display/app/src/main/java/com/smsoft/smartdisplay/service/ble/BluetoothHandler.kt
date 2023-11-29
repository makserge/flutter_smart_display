package com.smsoft.smartdisplay.service.ble

import kotlinx.coroutines.flow.Flow

interface BluetoothHandler {
    var scanState: Flow<BluetoothScanState>
    fun isBluetoothEnabled(): Boolean
    fun startScan()
    fun stopScan()
}