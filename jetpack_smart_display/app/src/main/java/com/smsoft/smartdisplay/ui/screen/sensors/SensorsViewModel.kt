package com.smsoft.smartdisplay.ui.screen.sensors

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smsoft.smartdisplay.data.MQTTData
import com.smsoft.smartdisplay.data.database.entity.Sensor
import com.smsoft.smartdisplay.data.database.repository.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import info.mqtt.android.service.MqttAndroidClient
import info.mqtt.android.service.QoS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject

@HiltViewModel
class SensorsViewModel @Inject constructor(
    val dataStore: DataStore<Preferences>,
    private val sensorRepository: SensorRepository,
    private val mqttClient: MqttAndroidClient
) : ViewModel() {

    val getAll = sensorRepository.getAll

    private val mqttTopicDataInt = MutableStateFlow(MQTTData())
    val mqttTopicData = mqttTopicDataInt.asStateFlow()

    fun addItem(item: Sensor) = viewModelScope.launch(Dispatchers.IO) {
        sensorRepository.insert(item)
        subscribeToMQTTTopic(item)
    }

    fun deleteItem(item: Sensor) = viewModelScope.launch(Dispatchers.IO) {
        sensorRepository.delete(item)
        unSubscribeMQTTTopic(item)
    }

    fun updateItem(item: Sensor) = viewModelScope.launch(Dispatchers.IO) {
        val oldItem = sensorRepository.get(item.id)
        unSubscribeMQTTTopic(oldItem)
        sensorRepository.update(item)
        subscribeToMQTTTopic(item)
    }

    private fun subscribeToMQTTTopic(item: Sensor) {
        if (item.topic1.isNotEmpty()) {
            mqttClient.subscribe(
                topic = item.topic1,
                qos = QoS.AtMostOnce.value
            )
        }
        if (item.topic2.isNotEmpty()) {
            mqttClient.subscribe(
                topic = item.topic2,
                qos = QoS.AtMostOnce.value
            )
        }
        if (item.topic3.isNotEmpty()) {
            mqttClient.subscribe(
                topic = item.topic3,
                qos = QoS.AtMostOnce.value
            )
        }
        if (item.topic4.isNotEmpty()) {
            mqttClient.subscribe(
                topic = item.topic4,
                qos = QoS.AtMostOnce.value
            )
        }
    }

    private fun unSubscribeMQTTTopic(item: Sensor) {
        if (item.topic1.isNotEmpty()) {
            mqttClient.unsubscribe(
                topic = item.topic1
            )
        }
        if (item.topic2.isNotEmpty()) {
            mqttClient.unsubscribe(
                topic = item.topic2
            )
        }
        if (item.topic3.isNotEmpty()) {
            mqttClient.unsubscribe(
                topic = item.topic3
            )
        }
        if (item.topic4.isNotEmpty()) {
            mqttClient.unsubscribe(
                topic = item.topic4
            )
        }
    }

    private val mqttClientCallback = object : MqttCallbackExtended {
        override fun connectComplete(
            reconnect: Boolean,
            serverURI: String
        ) {
            if (reconnect) {
                Log.d("MQTT", "Reconnected: $serverURI")
            } else {
                Log.d("MQTT","Connected: $serverURI")
            }
        }

        override fun connectionLost(
            cause: Throwable?
        ) {
            Log.d("MQTT", "The Connection was lost.")
        }

        override fun messageArrived(
            topic: String,
            message: MqttMessage
        ) {
            Log.d("MQTT", "messageArrived: $topic: $message")
            mqttTopicDataInt.value.value[topic] = message.toString()
            mqttTopicDataInt.value = MQTTData(
                value = mqttTopicDataInt.value.value
            )
        }

        override fun deliveryComplete(token: IMqttDeliveryToken) {}
    }

    init {
        if (mqttClient.isConnected) {
            mqttClient.addCallback(mqttClientCallback)
        }
    }
}

const val MQTT_CLIENT_ID = "SmartDisplay"