package com.smsoft.smartdisplay.data

enum class BluetoothDeviceType(val id: String, val title: String) {
    THERMOBEACON("thermobeacon", "ThermoBeacon"),
    ATC("atc", "ATC_");

    companion object {
        fun toList(): List<String> {
            val list = mutableListOf<String>()
            BluetoothDeviceType.values().forEach {
                list.add(it.title)
            }
            return list
        }
    }
}