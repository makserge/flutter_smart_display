package com.smsoft.smartdisplay.ui.screen.alarms

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.data.AlarmSoundToneType
import com.smsoft.smartdisplay.data.AlarmSoundType
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.database.entity.Alarm
import com.smsoft.smartdisplay.data.database.entity.emptyAlarm
import com.smsoft.smartdisplay.data.database.repository.AlarmRepository
import com.smsoft.smartdisplay.service.alarm.AlarmHandler
import com.smsoft.smartdisplay.service.radio.ExoPlayerImpl
import com.smsoft.smartdisplay.service.radio.MediaState
import com.smsoft.smartdisplay.service.radio.PlayerEvent
import com.smsoft.smartdisplay.service.radio.RadioMediaService
import com.smsoft.smartdisplay.service.radio.RadioMediaServiceHandler
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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val app: Application,
    @ApplicationContext private val context: Context,
    val dataStore: DataStore<Preferences>,
    private val alarmRepository: AlarmRepository,
    private val alarmHandler: AlarmHandler,
    private val alarmManager: AlarmManager,
    private val mqttClient: MqttAndroidClient,
    private val radioMediaServiceHandler: RadioMediaServiceHandler
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
                    publishMQTT(
                        topic = pushButtonCommandTopic,
                        messagePayload = pushButtonPayloadOn
                    )
                    return@collect
                }
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
        val intent = Intent(context, RadioMediaService::class.java)
        ContextCompat.startForegroundService(context, intent)

        viewModelScope.launch {
            val mediaItemList = mutableListOf<MediaItem>()
            val m3uStream = context.assets.open(PLAYLIST)
            val streamEntries = M3uParser.parse(m3uStream.reader())
            streamEntries.forEach {
                mediaItemList.add(
                    MediaItem.Builder()
                        .setUri(it.location.url.toString())
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setDisplayTitle(it.title)
                                .build()
                        ).build()
                )
            }
            radioMediaServiceHandler.apply {
                addMediaItemList(mediaItemList)
                playItem(preset)
            }
            if (isFadeEnabled) {
                radioMediaServiceHandler.setVolume(0F)
                fadeInVolume( 0F, alarmSoundVolume) {
                    radioMediaServiceHandler.setVolume(it)
                }
            } else {
                radioMediaServiceHandler.setVolume(alarmSoundVolume)
            }
            viewModelScope.launch {
                radioMediaServiceHandler.mediaState.collect { mediaState ->
                    if (mediaState == MediaState.Error) {
                        onError()
                    }
                }
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
        radioMediaServiceHandler.onPlayerEvent(PlayerEvent.Stop)
        context.stopService(Intent(context, RadioMediaService::class.java))
    }
}