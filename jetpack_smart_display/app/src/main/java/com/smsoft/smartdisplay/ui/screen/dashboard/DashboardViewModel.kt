package com.smsoft.smartdisplay.ui.screen.dashboard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.MQTTServer
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.screen.MainActivity
import com.smsoft.smartdisplay.ui.screen.settings.DOORBELL_ALARM_DEFAULT_TOPIC
import dagger.hilt.android.lifecycle.HiltViewModel
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val mqttClient: MqttAndroidClient
) : ViewModel() {
    private val doorBellAlarmStateInt = MutableStateFlow(false)
    val doorBellAlarmState = doorBellAlarmStateInt.asStateFlow()

    private var pushButtonTopic = PUSH_BUTTON_DEFAULT_TOPIC
    private var doorbellTopic = DOORBELL_ALARM_DEFAULT_TOPIC

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getMQTTTopics()
            initMQTT()
        }
    }

    fun resetDoorBellAlarmState() {
        doorBellAlarmStateInt.value = false
    }

    fun sendPressButtonEvent() {
        if (!mqttClient.isConnected) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val message = MqttMessage().apply {
                payload = MQTT_PRESS_BUTTON_MESSAGE.toByteArray()
            }
            mqttClient.publish(
                topic = pushButtonTopic,
                message = message,
                userContext = null,
                callback = object: IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d("DashboardViewModel", "sendPressButtonEvent Ok")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d("DashboardViewModel", "sendPressButtonEvent Error")
                    }
                }
            )
        }
    }

    private suspend fun getMQTTTopics() {
        val data = dataStore.data.first()
        data[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_TOPIC.key)]?.let {
            pushButtonTopic = it.trim()
        }
        data[stringPreferencesKey(PreferenceKey.DOORBELL_ALARM_TOPIC.key)]?.let {
            doorbellTopic = it.trim()
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
            if (topic == doorbellTopic) {
                doorBellAlarmStateInt.value = true
            }
        }

        override fun deliveryComplete(
            token: IMqttDeliveryToken
        ) {
        }
    }

    private fun initMQTT() {
        CoroutineScope(Dispatchers.IO).launch {
            val mqttServer = getMQTTServerCredentials(dataStore)
            mqttClient.apply {
                setForegroundService(
                    notification = getForegroundNotification(
                        context = context,
                        channelId = MQTT_CHANNEL,
                        channelName = MQTT_CHANNEL
                    )
                )
            }
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
                    mqttClient.addCallback(mqttClientCallback)
                }

                override fun onFailure(
                    asyncActionToken: IMqttToken?,
                    exception: Throwable?
                ) {
                    Log.d("MQTT", "Failed to connect")
                }
            }
            mqttClient.connect(
                options = mqttConnectOptions,
                userContext = null,
                callback = mqttClientListener
            )
        }
    }

    private fun getMQTTServerCredentials(
        dataStore: DataStore<Preferences>,
    ) : MQTTServer {
        return runBlocking {
            val data = dataStore.data.first()
            var login = ""
            data[stringPreferencesKey(PreferenceKey.MQTT_BROKER_USERNAME.key)]?.let {
                login = it
            }
            var password = ""
            data[stringPreferencesKey(PreferenceKey.MQTT_BROKER_PASSWORD.key)]?.let {
                password = it
            }
            return@runBlocking MQTTServer(
                login = login,
                password = password
            )
        }
    }
    private fun getForegroundNotification(
        context: Context,
        channelId: String,
        channelName: String
    ): Notification {
        val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags)

        val notificationCompat = NotificationCompat.Builder(context, channelName)
            .setAutoCancel(true)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher)
        return notificationCompat.build()
    }

    private val MQTT_CHANNEL = "MQTTchannnel"
    private val MQTT_PRESS_BUTTON_MESSAGE = "1"
}

const val PUSH_BUTTON_DEFAULT_TOPIC = "pushbutton"