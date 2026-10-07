package com.smsoft.smartdisplay.service.ble

import kotlinx.coroutines.flow.StateFlow

interface BluetoothHandler {
    /** Latest scan state; hot, so it keeps delivering for as long as the scan runs. */
    val scanState: StateFlow<BluetoothScanState>
    fun isBluetoothEnabled(): Boolean

    /** Starts scanning. Calling it while a scan is already running does nothing. */
    fun startScan()

    /** Forgets the devices found so far and starts a fresh scan. */
    fun rescan()
    fun stopScan()
}
