package com.smsoft.smartdisplay.ui.screen.sensors

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.MQTTData
import com.smsoft.smartdisplay.data.MQTTServer
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.database.entity.Sensor
import com.smsoft.smartdisplay.data.database.repository.SensorRepository
import com.smsoft.smartdisplay.ui.screen.MainActivity
import com.smsoft.smartdisplay.ui.screen.sensorssettings.MQTT_DEFAULT_PORT
import com.smsoft.smartdisplay.ui.screen.sensorssettings.SensorsSettingsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import info.mqtt.android.service.MqttAndroidClient
import info.mqtt.android.service.QoS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.*
import javax.inject.Inject

@HiltViewModel
class SensorsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    preferencesRepository: SensorsSettingsViewModel.PreferencesRepository,
    private val sensorRepository: SensorRepository
) : ViewModel() {

    class PreferencesRepository @Inject constructor(
        val dataStore: DataStore<Preferences>
    )

    val dataStore = preferencesRepository.dataStore

    private var mqttAndroidClient: MqttAndroidClient? = null

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
            mqttAndroidClient!!.subscribe(
                item.topic1,
                QoS.AtMostOnce.value,
                null,
                null
            )
        }
        if (item.topic2.isNotEmpty()) {
            mqttAndroidClient!!.subscribe(
                item.topic2,
                QoS.AtMostOnce.value,
                null,
                null
            )
        }
        if (item.topic3.isNotEmpty()) {
            mqttAndroidClient!!.subscribe(
                item.topic3,
                QoS.AtMostOnce.value,
                null,
                null
            )
        }
        if (item.topic4.isNotEmpty()) {
            mqttAndroidClient!!.subscribe(
                item.topic4,
                QoS.AtMostOnce.value,
                null,
                null
            )
        }
    }

    private fun unSubscribeMQTTTopic(item: Sensor) {
        if (item.topic1.isNotEmpty()) {
            mqttAndroidClient!!.unsubscribe(
                item.topic1
            )
        }
        if (item.topic2.isNotEmpty()) {
            mqttAndroidClient!!.unsubscribe(
                item.topic2
            )
        }
        if (item.topic3.isNotEmpty()) {
            mqttAndroidClient!!.unsubscribe(
                item.topic3
            )
        }
        if (item.topic4.isNotEmpty()) {
            mqttAndroidClient!!.unsubscribe(
                item.topic4
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

    private val mqttClientListener = object : IMqttActionListener {
        override fun onSuccess(
            asyncActionToken: IMqttToken
        ) {
            val disconnectedBufferOptions = DisconnectedBufferOptions().apply {
                isBufferEnabled = true
                bufferSize = 100
                isPersistBuffer = false
                isDeleteOldestMessages = false
            }
            mqttAndroidClient!!.setBufferOpts(disconnectedBufferOptions)
        }

        override fun onFailure(
            asyncActionToken: IMqttToken?,
            exception: Throwable?
        ) {
            Log.d("MQTT", "Failed to connect")
        }
    }

    private fun initMQTTClient(
        mqttServer: MQTTServer
    ) {
        mqttAndroidClient = MqttAndroidClient(
            context,
            "tcp://" + mqttServer.host + ":" + mqttServer.port,
            clientId
        ).apply {
            setForegroundService(
                getForegroundNotification(),
                foregroundServiceNotificationId
            )
            setCallback(mqttClientCallback)
        }
        val mqttConnectOptions = MqttConnectOptions().apply {
            userName = mqttServer.login
            password = mqttServer.password.toCharArray()
            isAutomaticReconnect = true
            isCleanSession = false
        }
        mqttAndroidClient!!.connect(
            mqttConnectOptions,
            null,
            mqttClientListener)
    }

    private fun getForegroundNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags)

        val notificationCompat = NotificationCompat.Builder(context, channelName)
            .setAutoCancel(true)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher)
        return notificationCompat.build()
    }

    suspend fun onStart() : Boolean {
        val server = getMQTTServerCredentials()
        if (server.host.isNotEmpty()) {
            initMQTTClient(
                mqttServer = server
            )
            return false
        }
        return true
    }

    fun onStop() {
        mqttAndroidClient?.disconnect()
    }

    private suspend fun getMQTTServerCredentials() : MQTTServer {
        val data = dataStore.data.first()
        var host = ""
        data[stringPreferencesKey(PreferenceKey.MQTT_BROKER_HOST.key)]?.let {
            host = it
        }
        var port = MQTT_DEFAULT_PORT
        data[stringPreferencesKey(PreferenceKey.MQTT_BROKER_PORT.key)]?.let {
            port = it
        }
        var login = ""
        data[stringPreferencesKey(PreferenceKey.MQTT_BROKER_USERNAME.key)]?.let {
            login = it
        }
        var password = ""
        data[stringPreferencesKey(PreferenceKey.MQTT_BROKER_PASSWORD.key)]?.let {
            password = it
        }
        return MQTTServer(
            host = host,
            port = port,
            login = login,
            password = password
        )
    }

    private val channelId = "MQTTchannnel"
    private val channelName = "MQTTchannnel"
    private val foregroundServiceNotificationId = 3
    private val clientId = "SmartDisplay"
}