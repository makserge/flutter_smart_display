package com.smsoft.smartdisplay.service.sensor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface SensorHandler {
    var isServiceStarted: MutableStateFlow<Boolean>
    var lightSensorState: Flow<Int>?
    var proximitySensorState: Flow<Int>?
}