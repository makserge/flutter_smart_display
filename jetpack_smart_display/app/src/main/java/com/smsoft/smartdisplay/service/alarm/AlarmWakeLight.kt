package com.smsoft.smartdisplay.service.alarm

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.utils.alarmLightPercentAt
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_COMMAND_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_COMMAND_OFF_DEFAULT_PAYLOAD
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_COMMAND_ON_DEFAULT_PAYLOAD
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_COMMAND_ON_OFF_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_ENABLED_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_ENABLED_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_SENSOR_THRESHOLD_DEFAULT
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_COMMAND_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_DEFAULT_PAYLOAD_ON
import dagger.Lazy
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlin.math.max
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.io.IOException

/**
 * The wake-up light of a ringing alarm: switched on once the room is dark, and a dimmer it
 * switched on goes off again when the ring ends. It belongs to the ring in [AlarmHandler], not to
 * a dashboard: a dashboard that replaced another one used to switch the light off and fade it in
 * again from zero, and a light switched on for a ring that ended without a dashboard stayed on.
 * [start] and [stop] are called on the main thread. The fade itself runs on a background thread:
 * on the panel the level updates came about 1.5 s apart, not 100 ms, while the screen was busy.
 */
class AlarmWakeLight(
    private val dataStore: DataStore<Preferences>,
    private val mqttClient: Lazy<MqttAndroidClient>,
    private val lightSensorState: StateFlow<Int>,
    private val scope: CoroutineScope
) {
    // Waits for a dark room, then switches the light on or fades the dimmer in. It is cancelled by
    // stop(), and each step checks for that before it publishes, so no dimmer step follows the
    // "off" command.
    private var job: Job? = null
    // The "off" command (topic to payload) for a dimmer this ring switched on, else null. Only
    // such a dimmer is switched off again: a room that was already bright (e.g. the same light
    // switched on by hand) keeps its light when the alarm ends. Captured when switching on, so
    // that a topic edited in Settings meanwhile cannot send "off" to another light. Written by the
    // fade thread, read by stop(), so it is volatile.
    @Volatile
    private var dimmerOffCommand: Pair<String, String>? = null

    /** Called when [ring] starts. A light that is already waiting, fading in or on is kept. */
    fun start(ring: RingingAlarm) {
        if ((job?.isActive == true) || (dimmerOffCommand != null)) {
            return
        }
        job = scope.launch(Dispatchers.Default) {
            val config = readConfig() ?: return@launch
            if (!config.isEnabled) {
                return@launch
            }
            // Waits for a dark room once (0 means no reading yet) and ends.
            lightSensorState.first { value ->
                (value > 0) && (value < config.sensorThreshold)
            }
            if (ring.elapsedMs() >= ring.timeoutMs) {
                // Dark only now, while the ring fades out: switching on would wake for nothing.
                return@launch
            }
            // Off the main thread: creating the client reads the broker settings blocking.
            withContext(Dispatchers.IO) {
                getClient()
            }
            if (config.isDimmerEnabled) {
                fadeDimmer(config, ring)
            } else {
                // Kept trying while the ring lasts: a light that could not be reached when the room
                // got dark is switched on once the link is back. The end of the ring cancels this.
                while (!publish(config.pushButtonTopic, config.pushButtonPayloadOn)) {
                    if (config.pushButtonTopic.isBlank()) {
                        return@launch
                    }
                    delay(LIGHT_RETRY_MS)
                }
            }
        }
    }

    /** Called when the ring ends, also when it ends without a dashboard. */
    fun stop() {
        job?.cancel()
        job = null
        dimmerOffCommand?.let { (topic, payload) ->
            dimmerOffCommand = null
            sendOffUntilSent(topic, payload)
        }
    }

    // The "off" of a dimmer this ring switched on must not be lost to a broken link: it is retried
    // in the background for a while after the ring has ended.
    private fun sendOffUntilSent(topic: String, payload: String) {
        scope.launch(Dispatchers.IO) {
            repeat(OFF_ATTEMPTS) {
                if (publish(topic, payload)) {
                    return@launch
                }
                delay(LIGHT_RETRY_MS)
            }
            Log.w(TAG, "Dimmer off on $topic was not sent after $OFF_ATTEMPTS attempts")
        }
    }

    private suspend fun readConfig(): AlarmLightConfig? = try {
        readAlarmLightConfig(dataStore.data.first())
    } catch (e: IOException) {
        Log.w(TAG, "Alarm light settings not readable", e)
        null
    }

    private suspend fun fadeDimmer(config: AlarmLightConfig, ring: RingingAlarm) {
        dimmerOffCommand = config.dimmerOnOffTopic to config.dimmerPayloadOff
        publish(config.dimmerOnOffTopic, config.dimmerPayloadOff)
        publish(config.dimmerLevelTopic, "0")
        publish(config.dimmerOnOffTopic, config.dimmerPayloadOn)
        // Again after "on": a dimmer that ignores the level while off, or restores its last
        // brightness when switched on, would otherwise show that until the curve reaches 1 % (2.7 s)
        publish(config.dimmerLevelTopic, "0")
        // Along the perceived-brightness curve: a linear level looked bright within seconds and
        // then hardly changed. Up from the moment it is switched on, down again from the alarm
        // timeout, by the time of the ring, so late steps do not stretch the fade. An integer
        // percent, as the dimmer gets everywhere else (it was e.g. "0.33333334"), and only when it
        // changes: at most about 200 level messages. stop() sends "off" when the ring ends.
        val fadeInStartMs = ring.elapsedMs()
        var level = 0
        // How late the steps come: the largest gap between two steps beyond DIMMER_FADE_STEP_MS.
        // Logged at the end, so a slow panel shows its lag in the log.
        var lastStepMs = fadeInStartMs
        var largestLagMs = 0L
        var levelChanges = 0
        while (true) {
            delay(DIMMER_FADE_STEP_MS)
            val elapsedMs = ring.elapsedMs()
            largestLagMs = max(largestLagMs, elapsedMs - lastStepMs - DIMMER_FADE_STEP_MS)
            lastStepMs = elapsedMs
            val next = alarmLightPercentAt(elapsedMs, ring.timeoutMs, fadeInStartMs)
            if (next != level) {
                level = next
                levelChanges++
                // Checked again here: stop() may have cancelled the fade during the wait
                coroutineContext.ensureActive()
                Log.d(TAG, "dimmer $level % at $elapsedMs ms")
                publish(config.dimmerLevelTopic, level.toString())
            }
            if (elapsedMs >= ring.endMs) {
                Log.i(TAG, "fade ended: $levelChanges level changes, largest step lag ${largestLagMs} ms")
                return
            }
        }
    }

    private fun getClient(): MqttAndroidClient? = try {
        mqttClient.get()
    } catch (e: Exception) {
        Log.w(TAG, "MQTT client not available", e)
        null
    }

    // Never throws: an exception here would escape into the app-wide scope and crash the app.
    // True when the command was handed to the client; false when nothing was sent.
    private fun publish(topic: String, messagePayload: String): Boolean {
        if (topic.isBlank()) {
            return false
        }
        val client = getClient() ?: return false
        return try {
            if (!client.isConnected) {
                return false
            }
            client.publish(
                topic = topic,
                message = MqttMessage().apply {
                    payload = messagePayload.toByteArray()
                },
                userContext = null,
                callback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(TAG, "publishMQTT Ok")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(TAG, "publishMQTT Error")
                    }
                }
            )
            true
        } catch (e: Exception) {
            Log.w(TAG, "Publishing to $topic failed", e)
            false
        }
    }
}

private data class AlarmLightConfig(
    val isEnabled: Boolean,
    val sensorThreshold: Int,
    val pushButtonTopic: String,
    val pushButtonPayloadOn: String,
    val isDimmerEnabled: Boolean,
    val dimmerOnOffTopic: String,
    val dimmerPayloadOn: String,
    val dimmerPayloadOff: String,
    val dimmerLevelTopic: String
)

/** The alarm light settings with the same defaults as a fresh start. */
private fun readAlarmLightConfig(data: Preferences) = AlarmLightConfig(
    isEnabled = data[booleanPreferencesKey(PreferenceKey.ALARM_LIGHT_ENABLED.key)] ?: ALARM_LIGHT_ENABLED_DEFAULT,
    sensorThreshold = data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_SENSOR_THRESHOLD.key)]?.trim()?.toIntOrNull()
        ?: Integer.parseInt(ALARM_LIGHT_SENSOR_THRESHOLD_DEFAULT),
    pushButtonTopic = data[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_COMMAND_TOPIC.key)]?.trim() ?: PUSH_BUTTON_COMMAND_DEFAULT_TOPIC,
    pushButtonPayloadOn = data[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_PAYLOAD_ON.key)]?.trim() ?: PUSH_BUTTON_DEFAULT_PAYLOAD_ON,
    isDimmerEnabled = data[booleanPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_ENABLED.key)] ?: ALARM_LIGHT_DIMMER_ENABLED_DEFAULT,
    dimmerOnOffTopic = data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_ON_OFF_TOPIC.key)]?.trim() ?: ALARM_LIGHT_DIMMER_COMMAND_ON_OFF_DEFAULT_TOPIC,
    dimmerPayloadOn = data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_ON_PAYLOAD.key)]?.trim() ?: ALARM_LIGHT_DIMMER_COMMAND_ON_DEFAULT_PAYLOAD,
    dimmerPayloadOff = data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_OFF_PAYLOAD.key)]?.trim() ?: ALARM_LIGHT_DIMMER_COMMAND_OFF_DEFAULT_PAYLOAD,
    dimmerLevelTopic = data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_TOPIC.key)]?.trim() ?: ALARM_LIGHT_DIMMER_COMMAND_DEFAULT_TOPIC
)

// The dimmer level is checked every 100 ms (alarmLightPercentAt: 60 s up, 10 s down).
private const val DIMMER_FADE_STEP_MS = 100L
// A light command that could not be sent is tried again every 5 s: for the ring, or up to 2 min for "off".
private const val LIGHT_RETRY_MS = 5_000L
private const val OFF_ATTEMPTS = 24

private const val TAG = "AlarmWakeLight"