package com.smsoft.smartdisplay.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.AsrWakeWord
import com.smsoft.smartdisplay.data.AudioType
import com.smsoft.smartdisplay.data.BluetoothDevice
import com.smsoft.smartdisplay.data.BluetoothDeviceType
import com.smsoft.smartdisplay.data.MQTTData
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.RadioType
import com.smsoft.smartdisplay.data.SensorType
import com.smsoft.smartdisplay.data.database.entity.Sensor
import com.smsoft.smartdisplay.data.database.entity.emptySensor
import com.smsoft.smartdisplay.data.emptyBluetoothDevice
import com.smsoft.smartdisplay.ui.composable.settings.LIGHT_SENSOR_ENABLED_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.LIGHT_SENSOR_INTERVAL_DEFAULT
import com.smsoft.smartdisplay.ui.screen.MainActivity
import com.smsoft.smartdisplay.ui.screen.dashboard.APP_CHANNEL
import com.smsoft.smartdisplay.ui.screen.settings.MPD_SERVER_DEFAULT_HOST
import com.smsoft.smartdisplay.ui.screen.settings.MPD_SERVER_DEFAULT_PORT
import com.smsoft.smartdisplay.ui.screen.settings.MQTT_SERVER_DEFAULT_HOST
import com.smsoft.smartdisplay.ui.screen.settings.MQTT_SERVER_DEFAULT_PORT
import com.smsoft.smartdisplay.utils.mpd.data.MPDCredentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.net.NetworkInterface

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
    var id =
        context.resources.getIdentifier(
            "ic_outline_" + res + "_48",
            "drawable",
            context.packageName
        )
    if (id == 0) {
        id = getIcon(
            context = context,
            item = ""
        )
    }
    return id
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

fun getMQTTHostCredentials(
    dataStore: DataStore<Preferences>
) : String {
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

@UnstableApi
fun getForegroundNotification(
    context: Context,

): Notification {
    val channelId = APP_CHANNEL
    val channelName = APP_CHANNEL
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
        .setSmallIcon(R.drawable.ic_small_notification)
    return notificationCompat.build()
}

fun getMac(): String? {
    return try {
        NetworkInterface.getNetworkInterfaces()
            .toList()
            .find { networkInterface -> networkInterface.name.equals("wlan0", ignoreCase = true) }
            ?.hardwareAddress
            ?.joinToString(separator = "") { byte -> "%02X".format(byte) }
    } catch (ex: Exception) {
        null
    }
}

var player: Player? = null

@UnstableApi
fun playAssetSound(
    player: Player,
    audioType: AudioType,
    soundVolume: Float
) {
    player.apply{
        volume = soundVolume
        setMediaItem(MediaItem.fromUri(Uri.parse(audioType.path)))
        prepare()
        playWhenReady = true
    }
}

fun getAsrWakeWord(dataStore: DataStore<Preferences>): AsrWakeWord {
    var wakeWord = AsrWakeWord.getDefault()
    runBlocking {
        val data = dataStore.data.first()
        data[stringPreferencesKey(PreferenceKey.ASR_WAKE_WORD.key)]?.let {
            wakeWord = AsrWakeWord.getById(it)
        }
    }
    return wakeWord
}

fun getLightSensorSettings(dataStore: DataStore<Preferences>): Pair<Boolean, Int> {
    var isEnabled = LIGHT_SENSOR_ENABLED_DEFAULT
    var interval = LIGHT_SENSOR_INTERVAL_DEFAULT.toInt()
    runBlocking {
        val data = dataStore.data.first()
        data[booleanPreferencesKey(PreferenceKey.LIGHT_SENSOR_ENABLED.key)]?.let {
            isEnabled = it
        }
        data[stringPreferencesKey(PreferenceKey.LIGHT_SENSOR_INTERVAL.key)]?.let {
            interval = it.toInt()
        }
    }
    return Pair(isEnabled, interval)
}

fun getBluetoothDeviceByType(
    type: String,
    item: Sensor
): BluetoothDevice {
    return if (type == BluetoothDeviceType.THERMOBEACON.title) {
        BluetoothDevice(
            deviceName = item.topic3,
            address = item.topic4
        )
    } else if (type.startsWith(BluetoothDeviceType.ATC.title)) {
        BluetoothDevice(
            deviceName = item.topic3,
            address = item.topic4
        )
    } else {
        emptyBluetoothDevice
    }
}

fun getSensorByBluetoothType(
    id: Long,
    title: String,
    type: String,
    address: String
) : Sensor {
    return if (type == BluetoothDeviceType.THERMOBEACON.title) {
        Sensor(
            id = id,
            title = title,
            titleIcon = "",
            topic1 = "$address/temperature",
            topic1Unit = "C",
            topic1Icon = "thermostat",
            topic2 = "$address/humidity",
            topic2Unit = "%",
            topic2Icon = "humidity_low",
            topic3 = type,
            topic3Unit = "",
            topic3Icon = "",
            topic4 = address,
            topic4Unit = "",
            topic4Icon = "",
            type = SensorType.BLUETOOTH.id
        )
    } else if (type.startsWith(BluetoothDeviceType.ATC.title)) {
        Sensor(
            id = id,
            title = title,
            titleIcon = "",
            topic1 = "$address/temperature",
            topic1Unit = "C",
            topic1Icon = "thermostat",
            topic2 = "$address/humidity",
            topic2Unit = "%",
            topic2Icon = "humidity_low",
            topic3 = type,
            topic3Unit = "",
            topic3Icon = "",
            topic4 = address,
            topic4Unit = "",
            topic4Icon = "",
            type = SensorType.BLUETOOTH.id
        )
    } else {
        emptySensor
    }
}

fun getSensorDataByBluetoothType(
    device: BluetoothDevice,
    data: MQTTData
): MQTTData {
    if (device.deviceName == BluetoothDeviceType.THERMOBEACON.title) {
        device.bytes?.let {
            val parsedBytes = parseThermoBeaconData(
                bytes = device.bytes
            )
            if (parsedBytes.first > -100) { //
                data.value[device.address + "/temperature"] = String.format("%.2f", parsedBytes.first)
            }
            if (parsedBytes.second > 0) { //
                data.value[device.address + "/humidity"] = String.format("%.2f", parsedBytes.second)
            }
        }
    } else if (device.deviceName.startsWith(BluetoothDeviceType.ATC.title)) {
        device.bytes?.let {
            val parsedBytes = parseATCData(
                bytes = device.bytes
            )
            data.value[device.address + "/temperature"] = String.format("%.2f", parsedBytes.first)
            data.value[device.address + "/humidity"] = String.format("%.2f", parsedBytes.second)
        }
    }
    return MQTTData(
        value = data.value
    )
}
private fun parseThermoBeaconData(
    bytes: ByteArray
): Pair<Float, Float> {
    //val battery = littleEndianDataParse(bytes, 19, 2) * 0.001
    var temperature = littleEndianDataParseHaveSign(
        bytes = bytes,
        offset = 21,
        numBytes = 2).toFloat()
    temperature = if (temperature == -1f) {
        -128f
    } else {
        temperature / 16f
    }
    var humidity = littleEndianDataParseHaveSign(
        bytes = bytes,
        offset = 23,
        numBytes = 2).toFloat()
    humidity = if (humidity == -1f) {
        -128f
    } else {
        humidity / 16f
    }
    return Pair(temperature, humidity)
}

private fun parseATCData(
    bytes: ByteArray
): Pair<Float, Float> {
    val temperature = bytesToUInt16(bytes[14], bytes[15]) * 0.01f
    val humidity = bytesToUInt16(bytes[16], bytes[17]) * 0.01f
    //val battery = bytesToUInt16(bytes[18], bytes[19]) * 0.001
    return Pair(temperature, humidity)
}

private fun littleEndianDataParse(
    bytes: ByteArray,
    offset: Int,
    numBytes: Int
): Int {
    val byte1: Int
    val byte2: Int
    if (numBytes != 2) {
        if (numBytes != 4) {
            return -1
        }
        byte1 = bytes[offset + 3].toInt() shl 24 and -0x1000000
        byte2 = bytes[offset].toInt() and 0xff or (0xff00 and (bytes[offset + 1].toInt() shl 8)) or (bytes[offset + 2].toInt() shl 16 and 0xff0000)
    } else {
        byte1 = bytes[offset + 1].toInt() shl 8 and 0xff00
        byte2 = (bytes[offset].toInt() and 0xff).toShort().toInt()
    }
    return byte1 or byte2
}

private fun littleEndianDataParseHaveSign(
    bytes: ByteArray,
    offset: Int,
    numBytes: Int
): Int {
    val byte1: Int
    val byte2: Int
    if (numBytes != 2) {
        if (numBytes != 4) {
            return -1
        }
        byte1 = bytes[offset + 3].toInt() shl 24
        byte2 = bytes[offset].toInt() or (bytes[offset + 1].toInt() shl 8) or (bytes[offset + 2].toInt() shl 16)

    } else {
        byte1 = bytes[offset + 1].toInt() shl 8
        byte2 = bytes[offset].toInt() and 0xff
    }
    return byte1 or byte2
}

private fun bytesToUInt16(
    byte1: Byte,
    byte2: Byte
): Int {
    return ((byte1.toInt() and 0xFF) shl 8) or (byte2.toInt() and 0xFF)
}