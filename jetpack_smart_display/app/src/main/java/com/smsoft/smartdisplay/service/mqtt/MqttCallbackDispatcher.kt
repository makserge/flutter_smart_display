package com.smsoft.smartdisplay.service.mqtt

import com.smsoft.smartdisplay.data.MQTTData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttCallbackDispatcher @Inject constructor() : MqttCallbackExtended {
    private val listeners = CopyOnWriteArrayList<MqttCallback>()

    /**
     * The latest payload per topic since the app started. The broker replays retained sensor
     * values once, right after the dashboard subscribes on connect; the sensor page is created
     * only when it is first shown, so it missed them and showed "n/a" until each sensor
     * published again.
     */
    private val latestInt = MutableStateFlow(MQTTData())
    val latest: StateFlow<MQTTData> = latestInt.asStateFlow()

    fun addListener(listener: MqttCallback) {
        listeners.add(listener)
    }

    fun removeListener(listener: MqttCallback) {
        listeners.remove(listener)
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String) {
        listeners.forEach { listener ->
            if (listener is MqttCallbackExtended) {
                listener.connectComplete(reconnect, serverURI)
            }
        }
    }

    override fun connectionLost(cause: Throwable?) {
        listeners.forEach { it.connectionLost(cause) }
    }

    override fun messageArrived(topic: String, message: MqttMessage) {
        latestInt.update { it.withValues(mapOf(topic to message.toString())) }
        listeners.forEach { it.messageArrived(topic, message) }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken) {
        listeners.forEach { it.deliveryComplete(token) }
    }
}
