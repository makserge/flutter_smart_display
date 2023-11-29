package com.smsoft.smartdisplay.data

import android.content.Context
import com.smsoft.smartdisplay.R

enum class SensorType(val id: String, val titleId: Int) {
    MQTT("mqtt", R.string.sensor_type_mqtt),
    BLUETOOTH("bluetooth", R.string.sensor_type_bluetooth);

    companion object {
        fun toMap(context: Context): Map<String, String> {
            return SensorType.values().associate {
                it.id to context.getString(it.titleId)
            }
        }

        fun getDefault(): SensorType {
            return MQTT
        }

        fun getDefaultId(): String {
            return getDefault().id
        }

        fun getById(id: String): SensorType {
            val item = SensorType.values().filter {
                it.id == id
            }
            return item[0]
        }

        fun isBluetooth(type: String): Boolean {
            return SensorType.getById(type) == BLUETOOTH
        }
    }
}