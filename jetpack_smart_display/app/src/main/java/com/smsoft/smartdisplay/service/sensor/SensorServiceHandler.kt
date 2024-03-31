package com.smsoft.smartdisplay.service.sensor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SensorServiceHandler: SensorHandler {
    override var isServiceStarted: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override var lightSensorState: Flow<Int>? = null
    override var proximitySensorState: Flow<Int>? = null
}