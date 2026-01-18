package com.smsoft.smartdisplay.ui.screen.alarms

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.data.AlarmSoundToneType
import com.smsoft.smartdisplay.data.AlarmSoundType
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.database.entity.Alarm
import com.smsoft.smartdisplay.data.database.entity.emptyAlarm
import com.smsoft.smartdisplay.data.database.repository.AlarmRepository
import com.smsoft.smartdisplay.service.alarm.AlarmHandler
import com.smsoft.smartdisplay.service.radio.ExoPlayerImpl
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_COMMAND_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_COMMAND_OFF_DEFAULT_PAYLOAD
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_COMMAND_ON_DEFAULT_PAYLOAD
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_COMMAND_ON_OFF_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_ENABLED_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_ENABLED_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_SENSOR_THRESHOLD_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_SOUND_VOLUME_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_TIMEOUT_DEFAULT
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_COMMAND_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.screen.dashboard.PUSH_BUTTON_DEFAULT_PAYLOAD_ON
import com.smsoft.smartdisplay.ui.screen.radio.PLAYLIST
import com.smsoft.smartdisplay.utils.fadeInVolume
import com.smsoft.smartdisplay.utils.m3uparser.M3uParser
import com.smsoft.smartdisplay.utils.playAlarmSound
import com.smsoft.smartdisplay.utils.playStream
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@UnstableApi
@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val app: Application,
    @ApplicationContext private val context: Context,
    val dataStore: DataStore<Preferences>,
    private val alarmRepository: AlarmRepository,
    private val alarmHandler: AlarmHandler,
    private val alarmManager: AlarmManager,
    private val mqttClient: MqttAndroidClient
) : ViewModel() {
    var player = ExoPlayerImpl.getExoPlayer(
        context = context,
        audioAttributes = ExoPlayerImpl.getAudioAttributes()
    )

    private var pushButtonCommandTopic = PUSH_BUTTON_COMMAND_DEFAULT_TOPIC
    private var pushButtonPayloadOn = PUSH_BUTTON_DEFAULT_PAYLOAD_ON
    private var isAlarmLightEnabled = ALARM_LIGHT_ENABLED_DEFAULT
    private var lightSensorThreshold = Integer.parseInt(ALARM_LIGHT_SENSOR_THRESHOLD_DEFAULT)
    private var alarmOffTimeout = ALARM_TIMEOUT_DEFAULT
    private var alarmSoundVolume = ALARM_SOUND_VOLUME_DEFAULT
    private var isAlarmLightDimmerEnabled = ALARM_LIGHT_DIMMER_ENABLED_DEFAULT
    private var dimmerCommandOnOffTopic = ALARM_LIGHT_DIMMER_COMMAND_ON_OFF_DEFAULT_TOPIC
    private var dimmerCommandPayloadOn = ALARM_LIGHT_DIMMER_COMMAND_ON_DEFAULT_PAYLOAD
    private var dimmerCommandPayloadOff = ALARM_LIGHT_DIMMER_COMMAND_OFF_DEFAULT_PAYLOAD
    private var dimmerCommandTopic = ALARM_LIGHT_DIMMER_COMMAND_DEFAULT_TOPIC

    val getAll = alarmRepository.getAll

    private val radioPresetsInt = MutableStateFlow(emptyMap<String, String>())
    val radioPresets = radioPresetsInt.asStateFlow()

    private val alarmStateInt = MutableStateFlow(emptyAlarm)
    val alarmState = alarmStateInt.asStateFlow()

    private var alarmOffTimer: CountDownTimer? = null

    init {
        initAlarm()
    }

    private fun initAlarm() {
        viewModelScope.launch {
            val data = dataStore.data.first()
            data[booleanPreferencesKey(PreferenceKey.ALARM_LIGHT_ENABLED.key)]?.let {
                isAlarmLightEnabled = it
            }
            data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_SENSOR_THRESHOLD.key)]?.let {
                lightSensorThreshold = it.toInt()
            }
            data[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_COMMAND_TOPIC.key)]?.let {
                pushButtonCommandTopic = it.trim()
            }
            data[stringPreferencesKey(PreferenceKey.PUSH_BUTTON_PAYLOAD_ON.key)]?.let {
                pushButtonPayloadOn = it.trim()
            }
            data[floatPreferencesKey(PreferenceKey.ALARM_SOUND_VOLUME.key)]?.let {
                alarmSoundVolume = it
            }
            data[floatPreferencesKey(PreferenceKey.ALARM_TIMEOUT.key)]?.let {
                alarmOffTimeout = it
            }
            data[booleanPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_ENABLED.key)]?.let {
                isAlarmLightDimmerEnabled = it
            }
            data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_ON_OFF_TOPIC.key)]?.let {
                dimmerCommandOnOffTopic = it.trim()
            }
            data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_ON_PAYLOAD.key)]?.let {
                dimmerCommandPayloadOn = it.trim()
            }
            data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_OFF_PAYLOAD.key)]?.let {
                dimmerCommandPayloadOff = it.trim()
            }
            data[stringPreferencesKey(PreferenceKey.ALARM_LIGHT_DIMMER_COMMAND_TOPIC.key)]?.let {
                dimmerCommandTopic = it.trim()
            }
        }
        viewModelScope.launch {
            alarmHandler.alarmState.collect { alarm ->
                Log.v("AlarmsViewModel:", "Alarm - $alarm")
                if (alarm.id != 0L) {
                    showAlarm(alarm)
                    alarmStateInt.value = alarm
                    switchOnLight()

                    restartAlarmOffTimer {
                       cancelAlarm(alarm)
                    }
                }
            }
        }
    }

    fun deleteItem(item: Alarm) = viewModelScope.launch(Dispatchers.IO) {
        alarmRepository.delete(item)
        alarmHandler.deleteAlarm(item)
    }

    fun updateItem(item: Alarm) = viewModelScope.launch(Dispatchers.IO) {
        if (item.id > 0) {
            alarmRepository.update(item)
            if (item.isEnabled) {
                alarmHandler.createAlarm(item)
            } else {
                alarmHandler.deleteAlarm(item)
            }
        } else {
            val alarmId = alarmRepository.insert(item)
            val savedItem = item.copy(id = alarmId)

            alarmHandler.createAlarm(savedItem)
        }
    }

    fun loadRadioPresets() {
        viewModelScope.launch(Dispatchers.IO) {
            val m3uStream = context.assets.open(PLAYLIST)
            val streamEntries = M3uParser.parse(m3uStream.reader())
            val playlist = streamEntries.associate {
                streamEntries.indexOf(it).toString() to it.title!!
            }
            radioPresetsInt.value = playlist
        }
    }

    fun changeItemState(item: Alarm, state: Boolean) {
        updateItem(item.copy(isEnabled = state))
    }

    fun checkAlarmPermissions(): Boolean {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) && !alarmManager.canScheduleExactAlarms()) {
            Intent().apply { action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM }.also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                app.applicationContext.startActivity(it)
            }
            return false
        }
        return true
    }

    fun cancelAlarm(alarm: Alarm) {
        alarmOffTimer?.cancel()

        alarmStateInt.value = emptyAlarm

        if (alarm.soundType == AlarmSoundType.TONE.id) {
            player.stop()
        } else {
            stopPlayer()
        }
        if (isAlarmLightDimmerEnabled) {
            publishMQTT(
                topic = dimmerCommandOnOffTopic,
                messagePayload = dimmerCommandPayloadOff
            )
        }
    }

    private fun showAlarm(alarm: Alarm) {
        if (alarm.soundType == AlarmSoundType.TONE.id) {
            playAlarmSound(
                player = player,
                soundToneType = AlarmSoundToneType.getById(alarm.soundTone),
                soundVolume = alarmSoundVolume,
                isFadeIn = true,
                isRepeat = true
            )
        } else {
            playRadio(alarm.radioPreset, true) {
                playAlarmSound(
                    player = player,
                    soundToneType = AlarmSoundToneType.getById(alarm.soundTone),
                    soundVolume = alarmSoundVolume,
                    isFadeIn = true,
                    isRepeat = true
                )
                restartAlarmOffTimer {
                    cancelAlarm(alarm.copy(soundType = AlarmSoundType.TONE.id))
                }
            }
        }
    }

    private fun switchOnLight() {
        if (!isAlarmLightEnabled) {
            return
        }
        var isFired = false
        viewModelScope.launch {
            alarmHandler.lightSensorState.collect { value ->
                if (!isFired && (value > 0) && (value < lightSensorThreshold)) {
                    isFired = true

                    if (isAlarmLightDimmerEnabled) {
                        fadeInLightDimmer()
                    } else {
                        publishMQTT(
                            topic = pushButtonCommandTopic,
                            messagePayload = pushButtonPayloadOn
                        )
                    }
                    return@collect
                }
            }
        }
    }

    private fun fadeInLightDimmer() {
        publishMQTT(
            topic = dimmerCommandOnOffTopic,
            messagePayload = dimmerCommandPayloadOff
        )
        publishMQTT(
            topic = dimmerCommandTopic,
            messagePayload = "0"
        )
        publishMQTT(
            topic = dimmerCommandOnOffTopic,
            messagePayload = dimmerCommandPayloadOn
        )
        fadeInValue( 0f, 1f) {
            publishMQTT(
                topic = dimmerCommandTopic,
                messagePayload = (it * 100).toString()
            )
        }
    }

    private var fadeValueJob: Job? = null
    fun fadeInValue(
        fromValue: Float,
        toValue: Float,
        step: Long = 100L,
        durationMillis: Long = 30000L,
        onChange: (value: Float) -> Unit = {}
    ) {
        val oldFadeJob = fadeValueJob
        fadeValueJob = CoroutineScope(Dispatchers.Main).launch {
            oldFadeJob?.cancelAndJoin()
            val stepSize = (toValue - fromValue) / (max(1, durationMillis) / step)

            onChange(fromValue)
            var value = fromValue
            while (isActive) {
                value = max(0f, min(value + stepSize, 1F))
                onChange(value)
                if ((stepSize < 0 && value <= toValue) || (stepSize > 0 && value >= toValue)) {
                    onChange(toValue)
                    break
                }
                delay(step)
            }
        }
    }

    private fun publishMQTT(topic: String, messagePayload: String) {
        if (!mqttClient.isConnected) {
            return
        }
        mqttClient.publish(
            topic = topic,
            message = MqttMessage().apply {
                payload = messagePayload.toByteArray()
            },
            userContext = null,
            callback = object: IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("AlarmsViewModel", "publishMQTT Ok")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("AlarmsViewModel", "publishMQTT Error")
                }
            }
        )
    }

    fun playRadio(preset: Int, isFadeEnabled: Boolean, onError: () -> Unit) {
        viewModelScope.launch {
            val m3uStream = context.assets.open(PLAYLIST)
            val streamEntries = M3uParser.parse(m3uStream.reader())
            val entry = streamEntries[preset]
            playStream(
                player = player,
                uri = entry.location.url.toString(),
                soundVolume = 0F,
                onError = onError
            )
            if (isFadeEnabled) {
                player.volume = 0F
                fadeInVolume( 0F, alarmSoundVolume) {
                    player.volume = it
                }
            } else {
                player.volume = alarmSoundVolume
            }
        }
    }

    private fun restartAlarmOffTimer(
        onEnd: () -> Unit
    ) {
        if (alarmOffTimer != null) {
            alarmOffTimer!!.cancel()
            alarmOffTimer = null
        }
        alarmOffTimer = object : CountDownTimer(alarmOffTimeout.toLong() * 60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                onEnd()
            }
        }
        alarmOffTimer!!.start()
    }

    fun stopPlayer() {
        player.stop()
    }
}