package com.smsoft.smartdisplay.ui.screen.sensors

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smsoft.smartdisplay.data.MQTTData
import com.smsoft.smartdisplay.data.SensorType
import com.smsoft.smartdisplay.data.database.entity.Sensor
import com.smsoft.smartdisplay.data.database.repository.SensorRepository
import com.smsoft.smartdisplay.service.ble.BluetoothHandler
import com.smsoft.smartdisplay.service.ble.BluetoothScanState
import com.smsoft.smartdisplay.utils.getSensorDataByBluetoothType
import dagger.hilt.android.lifecycle.HiltViewModel
import info.mqtt.android.service.MqttAndroidClient
import info.mqtt.android.service.QoS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject

@HiltViewModel
class SensorsViewModel @Inject constructor(
    val dataStore: DataStore<Preferences>,
    private val sensorRepository: SensorRepository,
    private val mqttClient: MqttAndroidClient,
    private val bluetoothHandler: BluetoothHandler
) : ViewModel() {

    val getAll = sensorRepository.getAll
    val bluetoothSensorsList = sensorRepository.getByType(SensorType.BLUETOOTH.id)

    private val mqttTopicDataInt = MutableStateFlow(MQTTData())
    val mqttTopicData = mqttTopicDataInt.asStateFlow()

    private val bleScanStateInt = MutableStateFlow<BluetoothScanState>(BluetoothScanState.Initial)
    val bleScanState = bleScanStateInt.asStateFlow()

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
        if (SensorType.isBluetooth(item.type)) {
            return
        }
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
        if (SensorType.isBluetooth(item.type)) {
            return
        }
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

    fun startBleScan() {
        bluetoothHandler.startScan()
    }

    fun stopBleScan() {
        bluetoothHandler.stopScan()
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothHandler.isBluetoothEnabled()
    }

    private val mqttClientCallback = object : MqttCallback {
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
        viewModelScope.launch {
            val scanState = bluetoothHandler.scanState.stateIn(
                initialValue = BluetoothScanState.Initial,
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000)
            )
            scanState.collect { state ->
                Log.d("SensorsViewModel", state.toString())
                bleScanStateInt.value = state

                if (state is BluetoothScanState.Result) {
                    val devices = state.devices
                    devices.forEach { device ->
                        mqttTopicDataInt.value = getSensorDataByBluetoothType(
                            device = device,
                            data = mqttTopicDataInt.value
                        )
                    }
                }
                Log.d("SensorsViewModel", mqttTopicDataInt.value.toString())
            }
        }
    }
}

const val MQTT_CLIENT_ID = "SmartDisplay"