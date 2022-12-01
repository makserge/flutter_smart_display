package com.smsoft.smartdisplay.data

import java.lang.System.currentTimeMillis

data class MQTTData(
    var value: HashMap<String, String> = HashMap(),
    var lastUpdated: Long = currentTimeMillis()
)