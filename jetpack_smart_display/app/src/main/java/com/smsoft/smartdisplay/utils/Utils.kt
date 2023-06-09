package com.smsoft.smartdisplay.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.MQTTServer
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.RadioType
import com.smsoft.smartdisplay.ui.screen.MainActivity
import com.smsoft.smartdisplay.ui.screen.settings.DOORBELL_STREAM_DEFAULT_URL
import com.smsoft.smartdisplay.ui.screen.settings.MPD_SERVER_DEFAULT_HOST
import com.smsoft.smartdisplay.ui.screen.settings.MPD_SERVER_DEFAULT_PORT
import com.smsoft.smartdisplay.ui.screen.settings.MQTT_SERVER_DEFAULT_HOST
import com.smsoft.smartdisplay.ui.screen.settings.MQTT_SERVER_DEFAULT_PORT
import com.smsoft.smartdisplay.utils.mpd.data.MPDCredentials
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions

@Composable
fun getStateFromFlow(
    flow: Flow<*>,
    defaultValue: Any?
): Any? {
    return flow.collectAsStateWithLifecycle(initialValue = defaultValue).value
}

fun getParamFlow(
    dataStore: DataStore<Preferences>,
    defaultValue: Any?,
    getter: (preferences: Preferences) -> Any?
): Flow<*> {
    return dataStore.data.map {
        getter(it) ?: defaultValue
    }
}

fun getColor(value: androidx.compose.ui.graphics.Color) = Color.parseColor("#${Integer.toHexString(value.toArgb())}")

@SuppressLint("DiscouragedApi")
fun getIcon(
    context: Context,
    item: String
): Int {
    val res = item.ifEmpty {
        "empty"
    }
    return context.resources.getIdentifier("outline_" + res + "_24", "drawable", context.packageName)
}

fun getRadioType(dataStore: DataStore<Preferences>): RadioType {
    var radioType = RadioType.getDefault()
    runBlocking {
        val data = dataStore.data.first()
        data[stringPreferencesKey(PreferenceKey.RADIO_TYPE.key)]?.let {
            radioType = RadioType.getById(it)
        }
    }
    return radioType
}

fun getRadioPreset(dataStore: DataStore<Preferences>): Int {
    var preset = 0
    runBlocking {
        val data = dataStore.data.first()
        data[intPreferencesKey(PreferenceKey.RADIO_PRESET.key)]?.let {
            preset = it
        }
    }
    return preset
}

fun getRadioSettings(dataStore: DataStore<Preferences>): MPDCredentials {
    var host = MPD_SERVER_DEFAULT_HOST
    var port = MPD_SERVER_DEFAULT_PORT
    var password = ""
    runBlocking {
        val data = dataStore.data.first()
        data[stringPreferencesKey(PreferenceKey.MPD_SERVER_HOST.key)]?.let {
            host = it
        }
        data[stringPreferencesKey(PreferenceKey.MPD_SERVER_PORT.key)]?.let {
            port = it
        }
        data[stringPreferencesKey(PreferenceKey.MPD_SERVER_PASSWORD.key)]?.let {
            password = it
        }
    }
    return MPDCredentials(
        host = host,
        port = port.toInt(),
        password = password
    )
}

fun getDoorbellStreamUrl(dataStore: DataStore<Preferences>): String {
    var url = DOORBELL_STREAM_DEFAULT_URL
    runBlocking {
        val data = dataStore.data.first()
        data[stringPreferencesKey(PreferenceKey.DOORBELL_STREAM_URL.key)]?.let {
            if (it.trim().isNotEmpty()) {
                url = it.trim()
            }
        }
    }
    return url
}

fun getMQTTHostCredentials(dataStore: DataStore<Preferences>) : String {
    var uri: String
    runBlocking {
        val data = dataStore.data.first()
        var host = MQTT_SERVER_DEFAULT_HOST
        data[stringPreferencesKey(PreferenceKey.MQTT_BROKER_HOST.key)]?.let {
            host = it
        }
        var port = MQTT_SERVER_DEFAULT_PORT
        data[stringPreferencesKey(PreferenceKey.MQTT_BROKER_PORT.key)]?.let {
            port = it
        }
        uri = "tcp://" + host.trim() + ":" + port.trim()
    }
    return uri
}

fun initMQTT(
    context: Context,
    mqttClient: MqttAndroidClient,
    dataStore: DataStore<Preferences>
) {
    CoroutineScope(Dispatchers.IO).launch {
        val mqttServer = getMQTTServerCredentials(dataStore)
        mqttClient.apply {
            setForegroundService(
                notification = getForegroundNotification(
                    context = context,
                    channelId = "MQTTchannnel",
                    channelName = "MQTTchannnel"
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
        var host = ""
        data[stringPreferencesKey(PreferenceKey.MQTT_BROKER_HOST.key)]?.let {
            host = it
        }
        var port = MQTT_SERVER_DEFAULT_PORT
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
        return@runBlocking MQTTServer(
            host = host,
            port = port,
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