package com.smsoft.smartdisplay.service.mqtt

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
        listeners.forEach { it.messageArrived(topic, message) }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken) {
        listeners.forEach { it.deliveryComplete(token) }
    }
}
