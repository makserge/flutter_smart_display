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
import android.os.SystemClock
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
import com.smsoft.smartdisplay.data.AlarmSoundToneType
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
import com.smsoft.smartdisplay.ui.screen.MainActivity
import com.smsoft.smartdisplay.ui.screen.dashboard.APP_CHANNEL
import com.smsoft.smartdisplay.ui.screen.settings.MQTT_SERVER_DEFAULT_HOST
import com.smsoft.smartdisplay.ui.screen.settings.MQTT_SERVER_DEFAULT_PORT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import com.smsoft.smartdisplay.ui.screen.sensors.MQTT_CLIENT_ID
import androidx.datastore.preferences.core.edit
import java.util.UUID
import kotlin.coroutines.CoroutineContext

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

/**
 * Key of the saved station index of [type]. The internal list and the MPD queue are different
 * lists, so each radio type keeps its own index (the internal one under the original key).
 */
fun radioPresetKey(type: RadioType): Preferences.Key<Int> = intPreferencesKey(
    when (type) {
        RadioType.INTERNAL -> PreferenceKey.RADIO_PRESET.key
        RadioType.MPD -> PreferenceKey.RADIO_PRESET_MPD.key
    }
)

fun getRadioPreset(dataStore: DataStore<Preferences>, type: RadioType): Int {
    var preset = 0
    runBlocking {
        val data = dataStore.data.first()
        data[radioPresetKey(type)]?.let {
            preset = it
        }
    }
    return preset
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
    val pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    val pendingIntent = PendingIntent.getActivity(context, 0, getShowAppIntent(context), pendingIntentFlags)

    val notificationCompat = NotificationCompat.Builder(context, channelName)
        .setAutoCancel(true)
        .setContentTitle(context.getString(R.string.app_name))
        .setContentIntent(pendingIntent)
        .setWhen(System.currentTimeMillis())
        .setSmallIcon(R.drawable.ic_small_notification)
    return notificationCompat.build()
}

/**
 * Brings the running dashboard to the front the way a launcher icon does. With the singleTask
 * MainActivity this reaches the running instance whether the app runs as the home screen or was
 * started from another launcher. A HOME intent sent by the system on the app's behalf (the old
 * alarm-clock intent) always started a second dashboard instead.
 */
fun getShowAppIntent(context: Context): Intent {
    return Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_LAUNCHER)
        .setClass(context, MainActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}

/**
 * MQTT client id, unique per panel and stable across restarts.
 *
 * A broker drops the existing connection when another client connects with the same id. The old
 * "SmartDisplay" + MAC id became "SmartDisplayAndroidClient" on every Android 10+ panel (the MAC
 * is hidden from apps), so panels kept knocking each other off the broker. ANDROID_ID is per
 * device and is not copied by backup or device transfer (a stored id would be). The id is kept
 * at 23 characters, the length every MQTT 3.1.1 broker must accept.
 */
fun getMQTTClientId(
    context: Context
): String {
    val androidId = android.provider.Settings.Secure.getString(
        context.contentResolver,
        android.provider.Settings.Secure.ANDROID_ID
    )?.takeIf { it.isNotBlank() && (it != BROKEN_ANDROID_ID) }
    val deviceId = androidId ?: getNoBackupRandomId(context)
    return (MQTT_CLIENT_ID + "-" + deviceId).take(MQTT_CLIENT_ID_MAX_LENGTH)
}

/** Fallback id in the no-backup directory, so a restored panel does not inherit it. */
private fun getNoBackupRandomId(context: Context): String {
    val file = File(context.noBackupFilesDir, "mqtt_client_id")
    file.takeIf { it.exists() }?.readText()?.trim()?.takeIf { it.isNotEmpty() }?.let {
        return it
    }
    return UUID.randomUUID().toString().replace("-", "").take(12).also {
        file.writeText(it)
    }
}

// Constant ANDROID_ID reported by some old devices.
private const val BROKEN_ANDROID_ID = "9774d56d682e549c"
private const val MQTT_CLIENT_ID_MAX_LENGTH = 23

/** Errors are reported to the owner's own Player.Listener; this used to add one per call. */
@UnstableApi
fun playStream(
    player: Player,
    uri: String,
    soundVolume: Float
) {
    player.apply{
        volume = soundVolume
        setMediaItem(MediaItem.fromUri(uri))
        prepare()
        playWhenReady = true
    }
}


@UnstableApi
fun playAssetSound(
    player: Player,
    audioType: AudioType,
    soundVolume: Float
) {
    player.apply{
        // The same player also plays repeating message alerts (playAlarmSound with isRepeat);
        // without this reset a later wake-word/error chime would loop forever.
        repeatMode = Player.REPEAT_MODE_OFF
        volume = soundVolume
        setMediaItem(MediaItem.fromUri(Uri.parse(audioType.path)))
        prepare()
        playWhenReady = true
    }
}

/** Plays a tone; with a [fader] it starts silent and fades in to [soundVolume]. */
@UnstableApi
fun playAlarmSound(
    player: Player,
    soundToneType: AlarmSoundToneType,
    soundVolume: Float,
    fader: VolumeFader? = null,
    isRepeat: Boolean = false
) {
    player.apply{
        repeatMode = if (isRepeat) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
        volume = if (fader != null) 0F else soundVolume
        setMediaItem(MediaItem.fromUri(Uri.parse(soundToneType.path)))
        prepare()
        playWhenReady = true
    }
    fader?.fadeIn(0F, soundVolume) {
        player.volume = it
    }
}

/**
 * One volume fade at a time for one owner; it ends with [scope]. It replaces a single global fade
 * job that no owner could stop, so it kept setting the volume of a player that had been stopped
 * or released, and fades of different players cancelled each other. Main thread only.
 *
 * The volume follows the perceptual curve [fadeVolume] by the time on [clock] (elapsedRealtime,
 * the clock of RingingAlarm). It used to add a fixed linear step per delay: that ramp seemed loud
 * at once and then hardly changed, late delays stretched it, and a fade between equal volumes
 * never ended. [dispatcher] and [clock] are replaceable for tests.
 */
class VolumeFader(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineContext = Dispatchers.Main,
    private val clock: () -> Long = { SystemClock.elapsedRealtime() }
) {
    private var job: Job? = null

    /**
     * Fades from [fromVolume] to [toVolume] in [durationMillis] (also down), setting the volume
     * every [step] ms; the last value is exactly [toVolume]. With [elapsedMillis] the fade starts
     * that far in, at the same point of the curve as a fade that had run all along (an alarm that
     * a new dashboard rings on).
     */
    fun fadeIn(
        fromVolume: Float,
        toVolume: Float,
        step: Long = 100L,
        durationMillis: Long = 15000L,
        elapsedMillis: Long = 0L,
        onChange: (value: Float) -> Unit
    ) {
        // Cancelled on the main thread, which also runs the fade, so the old fade takes no
        // further step.
        job?.cancel()
        val startedAt = clock() - elapsedMillis
        job = scope.launch(dispatcher) {
            while (true) {
                val progress = fadeProgress(clock() - startedAt, durationMillis)
                onChange(fadeVolume(fromVolume, toVolume, progress))
                if ((progress >= 1F) || (fromVolume == toVolume)) {
                    break
                }
                delay(step.coerceAtLeast(1L))
            }
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
    }
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
    val values = mutableMapOf<String, String>()
    if (device.deviceName == BluetoothDeviceType.THERMOBEACON.title) {
        device.bytes?.let {
            val parsedBytes = parseThermoBeaconData(
                bytes = device.bytes
            )
            if (parsedBytes.first > -100) { //
                values[device.address + "/temperature"] = String.format("%.2f", parsedBytes.first)
            }
            if (parsedBytes.second > 0) { //
                values[device.address + "/humidity"] = String.format("%.2f", parsedBytes.second)
            }
        }
    } else if (device.deviceName.startsWith(BluetoothDeviceType.ATC.title)) {
        device.bytes?.let {
            val parsedBytes = parseATCData(
                bytes = device.bytes
            )
            values[device.address + "/temperature"] = String.format("%.2f", parsedBytes.first)
            values[device.address + "/humidity"] = String.format("%.2f", parsedBytes.second)
        }
    }
    return data.withValues(values)
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
    // ATC1441/pvvx custom format: temperature is a signed int16 (x0.01 degC), humidity is
    // unsigned (x0.01 %). Using bytesToUInt16 for temperature corrupted every reading at or
    // below 0 degC, since it always returns a value in [0, 65535] - e.g. an actual -0.50 degC
    // (raw two's-complement bytes 0xFFCE) decoded as unsigned 65486 -> 654.86 degC.
    val temperature = bytesToInt16(bytes[14], bytes[15]) * 0.01f
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

private fun bytesToInt16(
    byte1: Byte,
    byte2: Byte
): Int {
    // Reinterpret the unsigned 16-bit combination as signed two's-complement: converting to
    // Short truncates to 16 bits and keeps the sign bit, then widening back to Int sign-extends
    // it (e.g. 65486 -> (-50).toShort() -> -50), which is what a negative ATC temperature needs.
    return bytesToUInt16(byte1, byte2).toShort().toInt()
}

fun formatTime(time: Int): String {
    val hours = time / 60
    val minutes = time - (hours * 60)
    return "%02d:%02d".format(hours, minutes)
}

fun formatTimeLong(duration: Int): String {
    val hours = duration / 3600
    val minutes = duration / 60 - (hours * 60)
    val seconds = duration - (hours * 3600) - (minutes * 60)
    return "%d:%02d:%02d".format(hours, minutes, seconds)
}