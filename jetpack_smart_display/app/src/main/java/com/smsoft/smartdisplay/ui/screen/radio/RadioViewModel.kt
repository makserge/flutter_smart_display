package com.smsoft.smartdisplay.ui.screen.radio

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.text.Html
import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.HtmlCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.VoiceCommandType
import com.smsoft.smartdisplay.service.radio.MediaState
import com.smsoft.smartdisplay.service.radio.PlayerEvent
import com.smsoft.smartdisplay.service.radio.RadioMediaService
import com.smsoft.smartdisplay.service.radio.RadioMediaServiceHandler
import com.smsoft.smartdisplay.service.radio.RadioVolume
import com.smsoft.smartdisplay.utils.getRadioPreset
import com.smsoft.smartdisplay.utils.radioPresetKey
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
@UnstableApi
class RadioViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val radioMediaServiceHandler: RadioMediaServiceHandler,
    val dataStore: DataStore<Preferences>
) : ViewModel() {
    var presetTitle = mutableStateOf("")
    var duration = mutableLongStateOf(0L)
    var progress = mutableFloatStateOf(0F)
    // The same text as after a stop (calculateProgressValues(0)), not "00:00".
    var progressString = mutableStateOf(formatDuration(0L))
    var isPlaying = mutableStateOf(false)
    var metaTitle = mutableStateOf("")
    /** The volume as a slider position 0..1, even in loudness (RadioVolume), or UNKNOWN. */
    var volume = mutableFloatStateOf(RadioVolume.UNKNOWN)

    private val isShowVolumeInt = MutableStateFlow(false)
    val isShowVolume = isShowVolumeInt.asStateFlow()

    private val uiStateInt = MutableStateFlow<UIState>(UIState.Initial)
    val uiState = uiStateInt.asStateFlow()

    private var volumeHideTimer: CountDownTimer? = null

    /** True between [onEnter] and [onLeave], i.e. while the radio page is the settled page. */
    private var isEntered = false

    /** Whether the radio is the internal player; follows the radio type setting. */
    val isInternalRadio = mutableStateOf(radioMediaServiceHandler.isInternalPlayer)

    init {
        viewModelScope.launch {
            radioMediaServiceHandler.mediaState.collect { mediaState ->
                when (mediaState) {
                    MediaState.Initial -> uiStateInt.value = UIState.Initial
                    // Also reported while the radio is off (MPD connecting to show its state):
                    // the elapsed time only counts while the radio plays.
                    is MediaState.Buffering -> if (radioMediaServiceHandler.isActive.value) {
                        calculateProgressValues(mediaState.progress)
                    }
                    is MediaState.Playing -> isPlaying.value = mediaState.isPlaying
                    is MediaState.Progress -> calculateProgressValues(mediaState.progress)
                    is MediaState.Ready -> {
                        saveCurrentPreset(mediaState.currentMediaItemIndex)
                        presetTitle.value = if ((mediaState.currentMediaItem != null) && (mediaState.currentMediaItem != MediaItem.EMPTY)) mediaState.currentMediaItem.mediaMetadata.displayTitle.toString() else ""
                        duration.longValue = if (mediaState.duration > 0) mediaState.duration else 0
                        uiStateInt.value = UIState.Ready
                    }
                    MediaState.Error -> uiStateInt.value = UIState.Error
                }
            }
        }
        viewModelScope.launch {
            radioMediaServiceHandler.mediaMetadata.collect { mediaMetadata ->
                if (radioMediaServiceHandler.isActive.value) {
                    showSongTitle(mediaMetadata)
                }
            }
        }
        viewModelScope.launch {
            radioMediaServiceHandler.isActive.collect { isOn ->
                if (isOn) {
                    // MPD reports the song of a stream started by another client before it
                    // reports that it plays.
                    showSongTitle(radioMediaServiceHandler.mediaMetadata.value)
                } else {
                    // A radio that is off plays no song, and a new start begins the stream anew.
                    metaTitle.value = ""
                    calculateProgressValues(0L)
                }
            }
        }
        viewModelScope.launch {
            var isInitialValue = true
            radioMediaServiceHandler.playerState.collect { playerState ->
                val isFirst = isInitialValue
                isInitialValue = false
                playerState.volume.let {
                    if (it == RadioVolume.UNKNOWN) {
                        return@let
                    }
                    volume.floatValue = it
                    // Only real volume changes show the slider, not the value read on creation.
                    if (!isFirst) {
                        setShowVolume()
                    }
                }
            }
        }
        viewModelScope.launch {
            // The radio settings changed and the handler switched to a new player (carrying on if
            // the radio was on). Forget what the old one showed; the media service runs for the
            // internal player only.
            radioMediaServiceHandler.currentPlayer.drop(1).collect { newPlayer ->
                isInternalRadio.value = isInternalPlayer()
                // The new player's volume: UNKNOWN for MPD until it answers, so a voice volume
                // step waits for MPD instead of being dropped. The playerState collector skips
                // UNKNOWN and would keep the old player's value.
                volume.floatValue = radioMediaServiceHandler.volumePosition(newPlayer)
                volumeHideTimer?.cancel()
                isShowVolumeInt.value = false
                // The new type's saved station ("" for MPD until it answers); a radio that was on
                // has already restarted on the new player and reports its own station.
                presetTitle.value = radioMediaServiceHandler.stationTitle(savedPreset())
                // The metadata collector skips missing titles, so the old song is cleared here.
                metaTitle.value = ""
                duration.longValue = 0L
                calculateProgressValues(0L)
                if (isEntered) {
                    if (isInternalPlayer()) {
                        ensureServiceStarted()
                    } else {
                        stopRadioService()
                        radioMediaServiceHandler.connectMpd()
                    }
                }
            }
        }
    }

    fun isInternalPlayer(): Boolean {
        return radioMediaServiceHandler.isInternalPlayer
    }

    private fun saveCurrentPreset(
        currentMediaItemIndex: Int
    ) {
        // Chosen now, not in the edit: the index belongs to the list of the player that reported
        // it, and a later switch of the radio type must not file it under the new type.
        val key = radioPresetKey(radioMediaServiceHandler.radioType)
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[key] = currentMediaItemIndex
            }
        }
    }

    /** The saved station of the current radio type; the internal list and the MPD queue differ. */
    private fun savedPreset(): Int {
        return getRadioPreset(dataStore, radioMediaServiceHandler.radioType)
    }

    override fun onCleared() {
        volumeHideTimer?.cancel()
        onLeave()
        super.onCleared()
    }

    fun onUIEvent(
        uiEvent: UIEvent
    ) {
        if (uiEvent != UIEvent.Pause) {
            ensureServiceStarted()
        }
        // A radio that is off starts from the saved station, as the voice commands do: the player
        // itself would start from its first station.
        val isOff = !radioMediaServiceHandler.isActive.value
        when (uiEvent) {
            UIEvent.Backward -> if (isOff) {
                radioMediaServiceHandler.previous(savedPreset())
            } else {
                radioMediaServiceHandler.onPlayerEvent(PlayerEvent.Previous)
            }
            UIEvent.Forward -> if (isOff) {
                radioMediaServiceHandler.next(savedPreset())
            } else {
                radioMediaServiceHandler.onPlayerEvent(PlayerEvent.Next)
            }
            UIEvent.PlayPause,
            UIEvent.Play -> if (isOff) {
                radioMediaServiceHandler.ensurePlaying(savedPreset())
            } else {
                radioMediaServiceHandler.onPlayerEvent(
                    if (uiEvent == UIEvent.Play) PlayerEvent.Play else PlayerEvent.PlayPause
                )
            }
            UIEvent.Pause -> radioMediaServiceHandler.onPlayerEvent(PlayerEvent.Pause)
            is UIEvent.UpdateProgress -> {
                progress.floatValue = uiEvent.newProgress
                radioMediaServiceHandler.onPlayerEvent(
                    PlayerEvent.UpdateProgress(
                        uiEvent.newProgress
                    )
                )
            }
        }
    }

    fun formatDuration(
        duration: Long
    ): String {
        val totalSeconds = duration.toInt()
        val hours = totalSeconds / 3600
        val minutes = totalSeconds / 60 - (hours * 60)
        val seconds = totalSeconds - (hours * 3600) - (minutes * 60)
        return when {
            duration < 0 -> context.getString(R.string.duration_unknown)
            else -> if (hours > 0) {
                context.getString(R.string.duration_format_hours).format(hours, minutes, seconds)
            } else  {
                context.getString(R.string.duration_format).format(minutes, seconds)
            }
        }
    }

    private fun calculateProgressValues(
        currentProgress: Long
    ) {
        progress.floatValue = if (currentProgress > 0) ((currentProgress / 1000).toFloat() / duration.longValue) else 0F
        progressString.value = formatDuration(currentProgress / 1000)
    }

    /**
     * Shows the song of [mediaMetadata]. A report without a title keeps the song shown, except
     * the empty metadata of a station change (RadioMediaServiceHandler.onMediaItemTransition):
     * the old station's song is not playing any more.
     */
    private fun showSongTitle(
        mediaMetadata: MediaMetadata
    ) {
        if (mediaMetadata == MediaMetadata.EMPTY) {
            metaTitle.value = ""
            return
        }
        mediaMetadata.title?.let {
            metaTitle.value = convertCharset(it.toString())
        }
    }

    private fun convertCharset(
        data: String
    ): String {
        if (data.isNotEmpty()) {
            return Html.fromHtml(data, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
        }
        return data
    }

    /**
     * The radio page became the settled pager page. Opening the page does not change playback:
     * a radio that plays keeps playing, and one that is off stays off until a voice command or
     * the play button starts it. Repeated calls are ignored until [onLeave].
     */
    fun onEnter() {
        if (isEntered) {
            return
        }
        isEntered = true
        if (radioMediaServiceHandler.isActive.value) {
            return
        }
        // A radio that is off has no stream, so the player never reports Ready and the page
        // showed a spinner forever. It shows the saved station with a play button instead.
        if (uiStateInt.value == UIState.Initial) {
            presetTitle.value = radioMediaServiceHandler.stationTitle(savedPreset())
            isPlaying.value = false
            uiStateInt.value = UIState.Ready
        }
        // MPD's queue is unknown until the app connects, and another client may have started
        // MPD: the page shows MPD's station and play state once it answers.
        radioMediaServiceHandler.connectMpd()
    }

    /**
     * Starts the media service (session, notification, foreground state) if it is not running.
     * Called before every playback start: the system may stop the service while the radio is
     * paused and the screen is off, and the page would not start it again by itself.
     */
    private fun ensureServiceStarted() {
        if (!isInternalPlayer() || !isEntered) {
            return
        }
        try {
            context.startService(Intent(context, RadioMediaService::class.java))
        } catch (e: IllegalStateException) {
            // Not allowed while the app is in the background; playback still works without it.
            Log.w(TAG, "Radio media service not started", e)
        }
    }

    /**
     * The radio page was left. The radio keeps playing: leaving the page does not stop it. With
     * the radio off, the connection that only showed MPD's state is closed.
     */
    fun onLeave() {
        if (!isEntered) {
            return
        }
        isEntered = false
        radioMediaServiceHandler.disconnectMpdIfOff()
    }

    private fun stopRadioService() {
        context.stopService(Intent(context, RadioMediaService::class.java))
    }

    internal fun resetState() {
        uiStateInt.value = UIState.Initial
        radioMediaServiceHandler.clearError()
    }

    /** Sets the volume to the slider [position] 0..1 (see RadioVolume). */
    internal fun setVolume(
        position: Float
    ) {
        radioMediaServiceHandler.setVolume(
            position = position
        )
        setShowVolume()
    }

    fun setShowVolume() {
        isShowVolumeInt.value = true
        reStartVolumeHideTimer {
            isShowVolumeInt.value = false
        }
    }

    private fun reStartVolumeHideTimer(
        callback: () -> Unit
    ) {
        volumeHideTimer?.cancel()
        volumeHideTimer = object : CountDownTimer(VOLUME_HIDE_TIMER, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                callback()
            }
        }.also {
            it.start()
        }
    }

    /** Handles one radio voice command. Each command is delivered once (see RadioScreen). */
    fun processVoiceCommand(
        command: VoiceCommandType
    ) {
        // Without radio.m3u the internal player has no stations. The MPD queue is unknown before
        // the first connect: the handler then starts MPD and steps once the queue is known.
        if (isInternalPlayer() && (radioMediaServiceHandler.mediaItemCount() == 0)) {
            return
        }
        if (command != VoiceCommandType.INTERNET_RADIO_OFF) {
            ensureServiceStarted()
        }
        when (command) {
            VoiceCommandType.INTERNET_RADIO_OFF -> radioMediaServiceHandler.stop()
            VoiceCommandType.INTERNET_RADIO_PREV_ITEM -> radioMediaServiceHandler.previous(savedPreset())
            VoiceCommandType.INTERNET_RADIO_NEXT_ITEM -> radioMediaServiceHandler.next(savedPreset())
            VoiceCommandType.INTERNET_RADIO_VOL_DOWN -> changeVolume(isForward = false)
            VoiceCommandType.INTERNET_RADIO_VOL_UP -> changeVolume(isForward = true)
            VoiceCommandType.INTERNET_RADIO,
            VoiceCommandType.INTERNET_RADIO_ON -> radioMediaServiceHandler.ensurePlaying(savedPreset())
            else -> {}
        }
    }

    private fun changeVolume(
        isForward: Boolean
    ) {
        viewModelScope.launch {
            if (volume.floatValue == RadioVolume.UNKNOWN) { //No value from MPD yet
                radioMediaServiceHandler.ensurePlaying(savedPreset())
                delay(500)
                if (volume.floatValue == RadioVolume.UNKNOWN) {
                    return@launch
                }
            }
            // 5 % of the slider: 2 dB on the internal player at any volume
            setVolume(
                position = RadioVolume.step(volume.floatValue, isUp = isForward)
            )
        }
    }
}

sealed class UIEvent {
    data object Play : UIEvent()
    data object Pause : UIEvent()
    data object PlayPause : UIEvent()
    data object Backward : UIEvent()
    data object Forward : UIEvent()
    data class UpdateProgress(val newProgress: Float) : UIEvent()
}

sealed class UIState {
    data object Initial : UIState()
    data object Error : UIState()
    data object Ready : UIState()
}

const val PLAYLIST = "radio.m3u"
private const val TAG = "RadioViewModel"
private const val VOLUME_HIDE_TIMER = 3000L //3s
