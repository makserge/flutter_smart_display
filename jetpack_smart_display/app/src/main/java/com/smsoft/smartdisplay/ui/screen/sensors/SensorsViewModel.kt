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
import com.smsoft.smartdisplay.service.mqtt.MqttCallbackDispatcher
import com.smsoft.smartdisplay.utils.getSensorDataByBluetoothType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject

@HiltViewModel
class SensorsViewModel @Inject constructor(
    val dataStore: DataStore<Preferences>,
    private val sensorRepository: SensorRepository,
    private val mqttCallbackDispatcher: MqttCallbackDispatcher,
    private val bluetoothHandler: BluetoothHandler
) : ViewModel() {

    val getAll = sensorRepository.getAll
    val bluetoothSensorsList = sensorRepository.getByType(SensorType.BLUETOOTH.id)

    private val mqttTopicDataInt = MutableStateFlow(MQTTData())
    val mqttTopicData = mqttTopicDataInt.asStateFlow()

    private val bleScanStateInt = MutableStateFlow<BluetoothScanState>(BluetoothScanState.Initial)
    val bleScanState = bleScanStateInt.asStateFlow()

    // The dashboard's MQTT manager follows the sensor list and owns the broker subscriptions.
    // Subscribing here as well unsubscribed topics that another sensor or a dashboard setting
    // still used, and crashed when MQTT had never connected.
    fun addItem(item: Sensor) = viewModelScope.launch(Dispatchers.IO) {
        sensorRepository.insert(item)
    }

    fun deleteItem(item: Sensor) = viewModelScope.launch(Dispatchers.IO) {
        sensorRepository.delete(item)
    }

    fun updateItem(item: Sensor) = viewModelScope.launch(Dispatchers.IO) {
        sensorRepository.update(item)
    }

    /** Who needs BLE results right now; the shared scan runs while anyone does. */
    private val bleScanClients = mutableSetOf<BleScanClient>()

    fun startBleScan(client: BleScanClient) {
        bleScanClients += client
        bluetoothHandler.startScan()
    }

    /** Scans again from scratch, e.g. "rescan" in the device picker. */
    fun rescanBle(client: BleScanClient) {
        bleScanClients += client
        bluetoothHandler.rescan()
    }

    /**
     * The sensor list and the sensor editor share one scan. Closing the editor used to stop it
     * while the list still showed Bluetooth sensors, which froze their readings until the page
     * was opened again.
     */
    fun stopBleScan(client: BleScanClient) {
        bleScanClients -= client
        if (bleScanClients.isEmpty()) {
            bluetoothHandler.stopScan()
        }
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
            // Called on an MQTT thread: update() replaces the immutable value atomically.
            mqttTopicDataInt.update {
                it.withValues(mapOf(topic to message.toString()))
            }
        }

        override fun deliveryComplete(token: IMqttDeliveryToken) {}
    }

    init {
        mqttCallbackDispatcher.addListener(mqttClientCallback)
        viewModelScope.launch {
            bluetoothHandler.scanState.collect { state ->
                bleScanStateInt.value = state

                if (state is BluetoothScanState.Result) {
                    mqttTopicDataInt.update { current ->
                        state.devices.fold(current) { data, device ->
                            getSensorDataByBluetoothType(
                                device = device,
                                data = data
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        mqttCallbackDispatcher.removeListener(mqttClientCallback)
        if (bleScanClients.isNotEmpty()) {
            bleScanClients.clear()
            bluetoothHandler.stopScan()
        }
    }
}

enum class BleScanClient {
    SENSOR_LIST,
    SENSOR_EDITOR
}

const val MQTT_CLIENT_ID = "SmartDisplay"