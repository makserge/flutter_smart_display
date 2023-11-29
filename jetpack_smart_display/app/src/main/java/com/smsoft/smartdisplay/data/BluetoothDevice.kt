package com.smsoft.smartdisplay.data

data class BluetoothDevice(
    val deviceName: String,
    val address: String,
    val rssi: Int = 0,
    val bytes: ByteArray? = null
)

val emptyBluetoothDevice = BluetoothDevice(
    deviceName = "",
    address = "",
    rssi = 0,
    bytes = null
)