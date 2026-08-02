package com.smsoft.smartdisplay.ui.screen.dashboard.mqtt

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.data.MQTTServer
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.service.mqtt.MqttCallbackDispatcher
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

data class DashboardMqttMessage(val topic: String, val payload: String)

class DashboardMqttManager(
    private val dataStore: DataStore<Preferences>,
    private val mqttClient: MqttAndroidClient,
    private val mqttCallbackDispatcher: MqttCallbackDispatcher,
) {
    private val incomingMessagesInt = MutableSharedFlow<DashboardMqttMessage>(extraBufferCapacity = 16)
    val incomingMessages = incomingMessagesInt.asSharedFlow()

    private val mqttClientCallback = object : MqttCallbackExtended {
        override fun connectComplete(
            reconnect: Boolean,
            serverURI: String
        ) {
            if (reconnect) {
                Log.d(TAG, "Reconnected: $serverURI")
            } else {
                Log.d(TAG, "Connected: $serverURI")
            }
        }

        override fun connectionLost(
            cause: Throwable?
        ) {
            Log.d(TAG, "The Connection was lost.")
        }

        override fun messageArrived(
            topic: String,
            message: MqttMessage
        ) {
            Log.d(TAG, "messageArrived: $topic: $message")
            incomingMessagesInt.tryEmit(
                DashboardMqttMessage(
                    topic = topic,
                    payload = message.payload.toString(Charsets.UTF_8)
                )
            )
        }

        override fun deliveryComplete(
            token: IMqttDeliveryToken
        ) {
        }
    }

    fun start(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            connect()
        }
    }

    fun stop() {
        mqttCallbackDispatcher.removeListener(mqttClientCallback)
    }

    fun publish(scope: CoroutineScope, topic: String, messagePayload: String) {
        if (!mqttClient.isConnected) {
            return
        }
        scope.launch(Dispatchers.IO) {
            mqttClient.publish(
                topic = topic,
                message = MqttMessage().apply {
                    payload = messagePayload.toByteArray()
                },
                userContext = null,
                callback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(TAG, "publishMQTT Error")
                    }
                }
            )
        }
    }

    private suspend fun connect() {
        val mqttServer = getMQTTServerCredentials()
        val mqttConnectOptions = MqttConnectOptions().apply {
            userName = mqttServer.login.trim()
            password = mqttServer.password.trim().toCharArray()
            isAutomaticReconnect = true
            isCleanSession = false
        }
        val mqttClientListener = object : IMqttActionListener {
            override fun onSuccess(
                asyncActionToken: IMqttToken
            ) {
                val disconnectedBufferOptions = DisconnectedBufferOptions().apply {
                    isBufferEnabled = true
                    bufferSize = 100
                    isPersistBuffer = false
                    isDeleteOldestMessages = false
                }
                mqttClient.setBufferOpts(disconnectedBufferOptions)
                mqttCallbackDispatcher.addListener(mqttClientCallback)
            }

            override fun onFailure(
                asyncActionToken: IMqttToken?,
                exception: Throwable?
            ) {
                Log.d(TAG, "Failed to connect")
            }
        }
        mqttClient.connect(
            options = mqttConnectOptions,
            userContext = null,
            callback = mqttClientListener
        )
    }

    private suspend fun getMQTTServerCredentials(): MQTTServer {
        val data = dataStore.data.first()
        var login = ""
        data[stringPreferencesKey(PreferenceKey.MQTT_BROKER_USERNAME.key)]?.let {
            login = it
        }
        var password = ""
        data[stringPreferencesKey(PreferenceKey.MQTT_BROKER_PASSWORD.key)]?.let {
            password = it
        }
        return MQTTServer(
            login = login,
            password = password
        )
    }

    companion object {
        private const val TAG = "DashboardMqttManager"
    }
}
