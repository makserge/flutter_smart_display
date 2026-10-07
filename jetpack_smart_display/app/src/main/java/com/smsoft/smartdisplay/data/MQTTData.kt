package com.smsoft.smartdisplay.data

import java.lang.System.currentTimeMillis

/**
 * Latest value per MQTT topic, or per "<BLE address>/<measurement>" key for Bluetooth sensors.
 *
 * Immutable on purpose: every change produces a new instance with a new map. The sensor cards
 * receive the map itself, and Compose skips a card whose arguments are the same instances, so a
 * map that was mutated in place never showed new readings until the page was opened again.
 */
data class MQTTData(
    val value: Map<String, String> = emptyMap(),
    val lastUpdated: Long = currentTimeMillis()
) {
    fun withValues(newValues: Map<String, String>): MQTTData {
        if (newValues.isEmpty()) {
            return this
        }
        return MQTTData(value = value + newValues)
    }
}
