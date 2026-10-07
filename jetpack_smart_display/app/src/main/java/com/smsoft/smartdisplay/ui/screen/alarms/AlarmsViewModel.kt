package com.smsoft.smartdisplay.ui.screen.alarms

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.data.AlarmSoundToneType
import com.smsoft.smartdisplay.data.AlarmSoundType
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.database.entity.Alarm
import com.smsoft.smartdisplay.data.database.entity.emptyAlarm
import com.smsoft.smartdisplay.data.database.repository.AlarmRepository
import com.smsoft.smartdisplay.service.alarm.AlarmHandler
import com.smsoft.smartdisplay.service.alarm.RingingAlarm
import com.smsoft.smartdisplay.service.radio.ExoPlayerImpl
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_SOUND_VOLUME_DEFAULT
import com.smsoft.smartdisplay.ui.screen.radio.PLAYLIST
import com.smsoft.smartdisplay.utils.VolumeFader
import com.smsoft.smartdisplay.utils.fadeProgress
import com.smsoft.smartdisplay.utils.fadeVolume
import com.smsoft.smartdisplay.utils.m3uparser.M3uParser
import com.smsoft.smartdisplay.utils.observe
import com.smsoft.smartdisplay.utils.playAlarmSound
import com.smsoft.smartdisplay.utils.playStream
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val app: Application,
    @ApplicationContext private val context: Context,
    val dataStore: DataStore<Preferences>,
    private val alarmRepository: AlarmRepository,
    private val alarmHandler: AlarmHandler,
    private val alarmManager: AlarmManager
) : ViewModel() {
    // Released in onCleared. Private: only this ViewModel knows when it is released, so the item
    // editor plays its previews through previewTone() and previewRadio().
    private val player = ExoPlayerImpl.getExoPlayer(
        context = context,
        audioAttributes = ExoPlayerImpl.getAudioAttributes()
    )
    private var isPlayerReleased = false

    // Dies with viewModelScope. The alarm's fade-in used to run in a global job that nothing
    // stopped, so it kept setting the volume after the alarm was dismissed.
    private val volumeFader = VolumeFader(viewModelScope)

    // The player's only listener. playStream() used to add a listener for every stream, so a
    // later stream error also ran the error fallback of every earlier radio alarm and preview.
    private var onStreamError: (() -> Unit)? = null
    private val playerListener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            onStreamError?.invoke()
        }
    }

    private var alarmSoundVolume = ALARM_SOUND_VOLUME_DEFAULT

    val getAll = alarmRepository.getAll

    private val radioPresetsInt = MutableStateFlow(emptyMap<String, String>())
    val radioPresets = radioPresetsInt.asStateFlow()

    private val alarmStateInt = MutableStateFlow(emptyAlarm)
    val alarmState = alarmStateInt.asStateFlow()

    // The ring this ViewModel sounds, else null. The ring itself, with its deadline and the
    // wake-up light, lives in AlarmHandler; this ViewModel only plays it and shows the dialog.
    private var soundingRing: RingingAlarm? = null

    init {
        player.addListener(playerListener)
        initAlarm()
    }

    private fun initAlarm() {
        // Follows Settings: the alarm settings used to be read once, so a change only applied
        // after a restart. A change applies from the next alarm.
        val settingsLoaded = CompletableDeferred<Unit>()
        dataStore.observe(viewModelScope, {
            it[floatPreferencesKey(PreferenceKey.ALARM_SOUND_VOLUME.key)] ?: ALARM_SOUND_VOLUME_DEFAULT
        }) {
            alarmSoundVolume = it
            settingsLoaded.complete(Unit)
        }
        viewModelScope.launch {
            // A ring that began before this ViewModel existed has to use the saved volume too.
            settingsLoaded.await()
            // A StateFlow: delivers a ring that began before this ViewModel existed (cold start, or
            // the dashboard this one replaced), and its end wherever that happens (timeout in
            // AlarmHandler, dismissal in another dashboard, "radio off" by voice).
            alarmHandler.ringingAlarm.collect { ring ->
                when {
                    ring === soundingRing -> {}
                    ring == null -> stopRinging()
                    else -> startRinging(ring)
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

    /** Dismisses the ringing [alarm] in every dashboard; AlarmHandler also switches its light off. */
    fun cancelAlarm(alarm: Alarm) {
        val ring = soundingRing?.takeIf { it.alarm.id == alarm.id } ?: return
        stopRinging()
        alarmHandler.onAlarmDismissed(ring)
    }

    private fun startRinging(ring: RingingAlarm) {
        val elapsedMs = ring.elapsedMs()
        if (elapsedMs >= ring.timeoutMs) {
            // Over already: AlarmHandler is about to end it.
            stopRinging()
            return
        }
        Log.i(TAG, "Ringing alarm ${ring.alarm.id}: $elapsedMs ms in, ${ring.timeoutMs - elapsedMs} ms left")
        soundingRing = ring
        alarmStateInt.value = ring.alarm
        showAlarm(ring.alarm, elapsedMs)
    }

    private fun stopRinging() {
        if (soundingRing == null) {
            return
        }
        soundingRing = null
        alarmStateInt.value = emptyAlarm
        stopPlayer()
    }

    private fun showAlarm(alarm: Alarm, elapsedMs: Long) {
        if (alarm.soundType == AlarmSoundType.TONE.id) {
            playTone(
                soundToneType = AlarmSoundToneType.getById(alarm.soundTone),
                soundVolume = alarmSoundVolume,
                isFadeIn = true,
                isRepeat = true,
                fadeElapsedMs = elapsedMs
            )
        } else {
            playRadio(alarm.radioPreset, isFadeEnabled = true, fadeElapsedMs = elapsedMs) {
                // A new sound with a fade of its own; the ring keeps its deadline.
                playTone(
                    soundToneType = AlarmSoundToneType.getById(alarm.soundTone),
                    soundVolume = alarmSoundVolume,
                    isFadeIn = true,
                    isRepeat = true
                )
            }
        }
    }

    private fun playTone(
        soundToneType: AlarmSoundToneType,
        soundVolume: Float,
        isFadeIn: Boolean,
        isRepeat: Boolean,
        fadeElapsedMs: Long = 0L
    ) {
        if (isPlayerReleased) {
            return
        }
        onStreamError = null
        volumeFader.cancel()
        val startVolume = if (isFadeIn) fadeFrom(soundVolume, fadeElapsedMs) else null
        playAlarmSound(
            player = player,
            soundToneType = soundToneType,
            soundVolume = startVolume ?: soundVolume,
            isRepeat = isRepeat
        )
        if (startVolume != null) {
            fadeIn(soundVolume, fadeElapsedMs)
        }
    }

    /** Sound preview in the alarm editor. */
    fun previewTone(soundToneId: String) {
        playTone(
            soundToneType = AlarmSoundToneType.getById(soundToneId),
            soundVolume = 1F,
            isFadeIn = false,
            isRepeat = false
        )
    }

    /** Station preview in the alarm editor. */
    fun previewRadio(preset: Int) {
        playRadio(preset, isFadeEnabled = false, onError = null)
    }

    private fun playRadio(
        preset: Int,
        isFadeEnabled: Boolean,
        fadeElapsedMs: Long = 0L,
        onError: (() -> Unit)?
    ) {
        viewModelScope.launch {
            if (isPlayerReleased) {
                return@launch
            }
            val m3uStream = context.assets.open(PLAYLIST)
            val streamEntries = M3uParser.parse(m3uStream.reader())
            val entry = streamEntries[preset]
            volumeFader.cancel()
            // Only the stream that plays now may run its error fallback.
            onStreamError = onError
            val startVolume = if (isFadeEnabled) fadeFrom(alarmSoundVolume, fadeElapsedMs) else null
            playStream(
                player = player,
                uri = entry.location.url.toString(),
                soundVolume = startVolume ?: alarmSoundVolume
            )
            if (startVolume != null) {
                fadeIn(alarmSoundVolume, fadeElapsedMs)
            }
        }
    }

    /** The alarm fade-in from silence to [volume], [elapsedMs] into the ring. */
    private fun fadeIn(volume: Float, elapsedMs: Long) {
        volumeFader.fadeIn(
            fromVolume = 0F,
            toVolume = volume,
            step = FADE_STEP_MS,
            durationMillis = ALARM_FADE_IN_MS,
            elapsedMillis = elapsedMs
        ) {
            player.volume = it
        }
    }

    fun stopPlayer() {
        if (isPlayerReleased) {
            return
        }
        onStreamError = null
        volumeFader.cancel()
        player.stop()
    }

    /**
     * The player used to live as long as the process: one more ExoPlayer with its playback thread
     * for every dashboard that was replaced, and a ringing alarm kept sounding from the closed
     * dashboard, out of reach of any dismiss button.
     *
     * viewModelScope is already cancelled here (collectors, fades, a pending playRadio). A ring
     * goes on in AlarmHandler with its deadline and light, so the player is released at once: a
     * dashboard that replaces this one rings it for the time left.
     */
    override fun onCleared() {
        super.onCleared()
        soundingRing = null
        volumeFader.cancel()
        onStreamError = null
        // Before release(): a release that times out reports an error to the listeners.
        player.removeListener(playerListener)
        isPlayerReleased = true
        player.release()
    }
}

/**
 * The volume of the alarm fade-in to [volume], [elapsedMs] into the ring, or null when the fade is
 * over. A ring that a new dashboard takes over goes on where the fade would be now instead of
 * starting from silence again; fadeIn() continues from the same point of the curve. (The fade was
 * linear, so a linear start volume followed by a fade of its own was enough; on a curve it is not.)
 */
private fun fadeFrom(volume: Float, elapsedMs: Long): Float? =
    if (ALARM_FADE_IN_MS - elapsedMs <= FADE_STEP_MS) {
        null
    } else {
        fadeVolume(0F, volume, fadeProgress(elapsedMs, ALARM_FADE_IN_MS))
    }

// The fade-in length VolumeFader uses by default, given explicitly so that a fade can go on.
private const val ALARM_FADE_IN_MS = 15_000L
private const val FADE_STEP_MS = 100L

private const val TAG = "AlarmsViewModel"
