package com.smsoft.smartdisplay.service.alarm

import android.os.SystemClock
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
import com.smsoft.smartdisplay.utils.fadeProgress
import com.smsoft.smartdisplay.utils.lightFadePercent
import dagger.Lazy
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.io.IOException

/**
 * The wake-up light of a ringing alarm: switched on once the room is dark, and a dimmer it
 * switched on goes off again when the ring ends. It belongs to the ring in [AlarmHandler], not to
 * a dashboard: a dashboard that replaced another one used to switch the light off and fade it in
 * again from zero, and a light switched on for a ring that ended without a dashboard stayed on.
 * Main thread only.
 */
class AlarmWakeLight(
    private val dataStore: DataStore<Preferences>,
    private val mqttClient: Lazy<MqttAndroidClient>,
    private val lightSensorState: StateFlow<Int>,
    private val scope: CoroutineScope
) {
    // Waits for a dark room, then switches the light on or fades the dimmer in. It runs on the
    // main thread, where it is also cancelled, so no dimmer step follows the "off" command.
    private var job: Job? = null
    // The "off" command (topic to payload) for a dimmer this ring switched on, else null. Only
    // such a dimmer is switched off again: a room that was already bright (e.g. the same light
    // switched on by hand) keeps its light when the alarm ends. Captured when switching on, so
    // that a topic edited in Settings meanwhile cannot send "off" to another light.
    private var dimmerOffCommand: Pair<String, String>? = null

    /** Called when a ring starts. A light that is already waiting, fading in or on is kept. */
    fun start() {
        if ((job?.isActive == true) || (dimmerOffCommand != null)) {
            return
        }
        job = scope.launch(Dispatchers.Main) {
            val config = readConfig() ?: return@launch
            if (!config.isEnabled) {
                return@launch
            }
            // Waits for a dark room once (0 means no reading yet) and ends.
            lightSensorState.first { value ->
                (value > 0) && (value < config.sensorThreshold)
            }
            // Off the main thread: creating the client reads the broker settings blocking.
            withContext(Dispatchers.IO) {
                getClient()
            }
            if (config.isDimmerEnabled) {
                fadeInDimmer(config)
            } else {
                publish(config.pushButtonTopic, config.pushButtonPayloadOn)
            }
        }
    }

    /** Called when the ring ends, also when it ends without a dashboard. */
    fun stop() {
        job?.cancel()
        job = null
        dimmerOffCommand?.let { (topic, payload) ->
            dimmerOffCommand = null
            publish(topic, payload)
        }
    }

    private suspend fun readConfig(): AlarmLightConfig? = try {
        readAlarmLightConfig(dataStore.data.first())
    } catch (e: IOException) {
        Log.w(TAG, "Alarm light settings not readable", e)
        null
    }

    private suspend fun fadeInDimmer(config: AlarmLightConfig) {
        dimmerOffCommand = config.dimmerOnOffTopic to config.dimmerPayloadOff
        publish(config.dimmerOnOffTopic, config.dimmerPayloadOff)
        publish(config.dimmerLevelTopic, "0")
        publish(config.dimmerOnOffTopic, config.dimmerPayloadOn)
        // Again after "on": a dimmer that ignores the level while off, or restores its last
        // brightness when switched on, would otherwise show that until the curve reaches 1 % (1.4 s)
        publish(config.dimmerLevelTopic, "0")
        // Along the perceived-brightness curve: a linear level looked bright within seconds and
        // then hardly changed. By the time since the start, so late steps do not stretch the fade.
        // An integer percent, as the dimmer gets everywhere else (it was e.g. "0.33333334"), and
        // only when it changes: at most 100 more level messages instead of 300.
        val startedAt = SystemClock.elapsedRealtime()
        var level = 0
        while (level < 100) {
            delay(DIMMER_FADE_STEP_MS)
            val elapsedMs = SystemClock.elapsedRealtime() - startedAt
            val next = lightFadePercent(fadeProgress(elapsedMs, DIMMER_FADE_IN_MS))
            if (next > level) {
                level = next
                publish(config.dimmerLevelTopic, level.toString())
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
    private fun publish(topic: String, messagePayload: String) {
        val client = getClient() ?: return
        try {
            if (!client.isConnected) {
                return
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
        } catch (e: Exception) {
            Log.w(TAG, "Publishing to $topic failed", e)
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

// The dimmer goes from 0 to 100 % in 30 s along lightFadePercent, checked every 100 ms.
private const val DIMMER_FADE_IN_MS = 30_000L
private const val DIMMER_FADE_STEP_MS = 100L

private const val TAG = "AlarmWakeLight"