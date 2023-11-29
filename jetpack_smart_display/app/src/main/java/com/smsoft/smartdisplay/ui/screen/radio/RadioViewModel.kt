package com.smsoft.smartdisplay.ui.screen.radio

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.text.Html
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.RadioType
import com.smsoft.smartdisplay.data.VoiceCommand
import com.smsoft.smartdisplay.service.radio.MediaState
import com.smsoft.smartdisplay.service.radio.PlayerEvent
import com.smsoft.smartdisplay.service.radio.RadioMediaService
import com.smsoft.smartdisplay.service.radio.RadioMediaServiceHandler
import com.smsoft.smartdisplay.utils.getRadioPreset
import com.smsoft.smartdisplay.utils.getRadioType
import com.smsoft.smartdisplay.utils.m3uparser.M3uParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
@SuppressLint("StaticFieldLeak")
class RadioViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val radioMediaServiceHandler: RadioMediaServiceHandler,
    savedStateHandle: SavedStateHandle,
    val dataStore: DataStore<Preferences>
) : ViewModel() {
    var presetTitle by savedStateHandle.saveable { mutableStateOf("") }
    var duration by savedStateHandle.saveable { mutableLongStateOf(0L) }
    var progress by savedStateHandle.saveable { mutableFloatStateOf(0F) }
    var progressString by savedStateHandle.saveable { mutableStateOf("00:00") }
    var isPlaying by savedStateHandle.saveable { mutableStateOf(false) }
    var metaTitle by savedStateHandle.saveable { mutableStateOf("") }
    var volume by savedStateHandle.saveable { mutableFloatStateOf(-1F) }

    private val isShowVolumeInt = MutableStateFlow(false)
    val isShowVolume = isShowVolumeInt.asStateFlow()

    private val uiStateInt = MutableStateFlow<UIState>(UIState.Initial)
    val uiState = uiStateInt.asStateFlow()

    private var volumeHideTimer: CountDownTimer? = null

    private var command = MutableStateFlow(VoiceCommand.INTERNET_RADIO)

    init {
        viewModelScope.launch {
            radioMediaServiceHandler.mediaState.collect { mediaState ->
                when (mediaState) {
                    MediaState.Initial -> uiStateInt.value = UIState.Initial
                    is MediaState.Buffering -> calculateProgressValues(mediaState.progress)
                    is MediaState.Playing -> isPlaying = mediaState.isPlaying
                    is MediaState.Progress -> calculateProgressValues(mediaState.progress)
                    is MediaState.Ready -> {
                        saveCurrentPreset(mediaState.currentMediaItemIndex)
                        presetTitle = if (mediaState.currentMediaItem != null) mediaState.currentMediaItem.mediaMetadata.displayTitle.toString() else ""
                        duration = if (mediaState.duration > 0) mediaState.duration else 0
                        uiStateInt.value = UIState.Ready

                        if (isInternalPlayer()) {
                            onStartService()
                        }
                        runCommand()
                    }
                    MediaState.Error -> uiStateInt.value = UIState.Error
                }
            }
        }
        viewModelScope.launch {
            radioMediaServiceHandler.mediaMetadata.collect { mediaMetadata ->
                mediaMetadata.title?.let {
                    metaTitle = convertCharset(it as String)
                }
            }
        }
        viewModelScope.launch {
            radioMediaServiceHandler.playerState.collect { playerState ->
                playerState.volume.let {
                    volume = it
                }
            }
        }
    }

    private suspend fun runCommand() {
        if (command.value != VoiceCommand.INTERNET_RADIO) {
            delay(VOICE_COMMAND_DELAY)
            processCommand(command.value)
            command.value = VoiceCommand.INTERNET_RADIO
        }
    }

    private fun isInternalPlayer(): Boolean {
        return getRadioType(dataStore) == RadioType.INTERNAL
    }

    private fun saveCurrentPreset(
        currentMediaItemIndex: Int
    ) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[intPreferencesKey(PreferenceKey.RADIO_PRESET.key)] = currentMediaItemIndex
            }
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            radioMediaServiceHandler.onPlayerEvent(PlayerEvent.Stop)
        }
    }

    fun onUIEvent(
        uiEvent: UIEvent
    ) = viewModelScope.launch {
        when (uiEvent) {
            UIEvent.Backward -> radioMediaServiceHandler.onPlayerEvent(PlayerEvent.Previous)
            UIEvent.Forward -> radioMediaServiceHandler.onPlayerEvent(PlayerEvent.Next)
            UIEvent.PlayPause -> radioMediaServiceHandler.onPlayerEvent(PlayerEvent.PlayPause)
            UIEvent.Play -> radioMediaServiceHandler.onPlayerEvent(PlayerEvent.Play)
            UIEvent.Pause -> radioMediaServiceHandler.onPlayerEvent(PlayerEvent.Pause)
            is UIEvent.UpdateProgress -> {
                progress = uiEvent.newProgress
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
        progress = if (currentProgress > 0) ((currentProgress / 1000).toFloat() / duration) else 0f
        progressString = formatDuration(currentProgress / 1000)
    }

    private fun convertCharset(
        data: String
    ): String {
        if (data.isNotEmpty()) {
            return Html.fromHtml(data, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
        }
        return data
    }

    private fun onStartService() {
        val intent = Intent(context, RadioMediaService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    internal fun onStopService() {
        context.stopService(Intent(context, RadioMediaService::class.java))
        onCleared()
    }

    internal fun resetState() {
        uiStateInt.value = UIState.Initial
    }

    internal fun onStart(
        command: VoiceCommand?
    ) {
        val voiceCommand = this.command

        viewModelScope.launch {
            val mediaItemList = mutableListOf<MediaItem>()
            if (isInternalPlayer()) {
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
            }
            var preset = getRadioPreset(dataStore)
            if (preset > mediaItemList.size) {
                preset = 1
            }
            radioMediaServiceHandler.addMediaItemList(mediaItemList, preset)

            voiceCommand.value = command ?: VoiceCommand.INTERNET_RADIO
        }
    }

    internal fun setVolume(
        value: Float
    ) {
        radioMediaServiceHandler.setVolume(
            value = value
        )

        isShowVolumeInt.value = true
        reStartVolumeHideTimer {
            isShowVolumeInt.value = false
        }
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
        if (volumeHideTimer != null) {
            volumeHideTimer!!.cancel()
            volumeHideTimer = null
        }
        volumeHideTimer = object : CountDownTimer(VOLUME_HIDE_TIMER, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                callback()
            }
        }
        volumeHideTimer!!.start()
    }

    private fun processCommand(
        command: VoiceCommand
    ) {
        when (command) {
            VoiceCommand.INTERNET_RADIO, VoiceCommand.INTERNET_RADIO_ON, VoiceCommand.INTERNET_RADIO_ON2 -> onUIEvent(UIEvent.Play)
            VoiceCommand.INTERNET_RADIO_OFF, VoiceCommand.INTERNET_RADIO_OFF2 -> onUIEvent(UIEvent.Pause)
            VoiceCommand.INTERNET_RADIO_PREV_ITEM -> onUIEvent(UIEvent.Backward)
            VoiceCommand.INTERNET_RADIO_NEXT_ITEM -> onUIEvent(UIEvent.Forward)
            VoiceCommand.INTERNET_RADIO_VOL_DOWN -> changeVolume(isForward = false)
            VoiceCommand.INTERNET_RADIO_VOL_UP -> changeVolume(isForward = true)
            else -> {}
        }
    }

    private fun changeVolume(
        isForward: Boolean
    ) {
        if (volume == -1F) { //No value from MPD
            return
        }
        if (isForward) {
            if (volume < VOLUME_MAX) {
                setVolume(
                    value = volume + VOLUME_STEP
                )
            }
        } else {
            if (volume > VOLUME_MIN) {
                setVolume(
                    value = volume - VOLUME_STEP
                )
            }
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

private const val PLAYLIST = "radio.m3u"
private const val VOLUME_HIDE_TIMER = 3000L //3s

private const val VOLUME_MIN = 0F
private const val VOLUME_MAX = 1F
private const val VOLUME_STEP = 0.05F //5%
private const val VOICE_COMMAND_DELAY = 1000L
