package com.smsoft.smartdisplay.ui.screen.radio

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.text.Html
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.RadioType
import com.smsoft.smartdisplay.data.VoiceCommandType
import com.smsoft.smartdisplay.service.radio.MediaState
import com.smsoft.smartdisplay.service.radio.PlayerEvent
import com.smsoft.smartdisplay.service.radio.RadioMediaService
import com.smsoft.smartdisplay.service.radio.RadioMediaServiceHandler
import com.smsoft.smartdisplay.utils.getRadioPreset
import com.smsoft.smartdisplay.utils.getRadioType
import com.smsoft.smartdisplay.utils.m3uparser.M3uParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    var progressString = mutableStateOf("00:00")
    var isPlaying = mutableStateOf(false)
    var metaTitle = mutableStateOf("")
    var volume = mutableFloatStateOf(-1F)

    private val isShowVolumeInt = MutableStateFlow(false)
    val isShowVolume = isShowVolumeInt.asStateFlow()

    private val uiStateInt = MutableStateFlow<UIState>(UIState.Initial)
    val uiState = uiStateInt.asStateFlow()

    private var volumeHideTimer: CountDownTimer? = null

    init {
        viewModelScope.launch {
            if (isInternalPlayer()) {
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
                radioMediaServiceHandler.addMediaItemList(mediaItemList)
            }
        }
        viewModelScope.launch {
            radioMediaServiceHandler.mediaState.collect { mediaState ->
                when (mediaState) {
                    MediaState.Initial -> uiStateInt.value = UIState.Initial
                    is MediaState.Buffering -> calculateProgressValues(mediaState.progress)
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
                mediaMetadata.title?.let {
                    metaTitle.value = convertCharset(it as String)
                }
            }
        }
        viewModelScope.launch {
            radioMediaServiceHandler.playerState.collect { playerState ->
                playerState.volume.let {
                    if (it == -1F) {
                        return@let
                    }
                    volume.floatValue = it
                    isShowVolumeInt.value = true
                    reStartVolumeHideTimer {
                        isShowVolumeInt.value = false
                    }
                }
            }
        }
    }

    fun isInternalPlayer(): Boolean {
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

    private fun convertCharset(
        data: String
    ): String {
        if (data.isNotEmpty()) {
            return Html.fromHtml(data, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
        }
        return data
    }

    @UnstableApi
    fun onStartService() {
        if (isInternalPlayer()) {
            val intent = Intent(context, RadioMediaService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }
    }

    @UnstableApi
    internal fun onStopService() {
        if (isInternalPlayer()) {
            context.stopService(Intent(context, RadioMediaService::class.java))
        }
        onCleared()
    }

    internal fun resetState() {
        uiStateInt.value = UIState.Initial
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

    fun processVoiceCommand(
        command: VoiceCommandType
    ) {
        if (radioMediaServiceHandler.mediaItemCount() == 0) {
            return
        }
        when (command) {
            VoiceCommandType.INTERNET_RADIO_OFF, VoiceCommandType.INTERNET_RADIO_OFF2 -> onUIEvent(UIEvent.Pause)
            VoiceCommandType.INTERNET_RADIO_PREV_ITEM -> onUIEvent(UIEvent.Backward)
            VoiceCommandType.INTERNET_RADIO_NEXT_ITEM -> onUIEvent(UIEvent.Forward)
            VoiceCommandType.INTERNET_RADIO_VOL_DOWN -> changeVolume(isForward = false)
            VoiceCommandType.INTERNET_RADIO_VOL_UP -> changeVolume(isForward = true)
            else -> {
                val preset = getRadioPreset(dataStore)
                radioMediaServiceHandler.playItem(preset)
            }
        }
    }

    private fun changeVolume(
        isForward: Boolean
    ) {
        viewModelScope.launch {
            if (volume.floatValue == -1F) { //No value from MPD
                val preset = getRadioPreset(dataStore)
                radioMediaServiceHandler.playItem(preset)
                delay(500)
            }
            if (isForward) {
                if (volume.floatValue < VOLUME_MAX) {
                    setVolume(
                        value = volume.floatValue + VOLUME_STEP
                    )
                }
            } else {
                if (volume.floatValue > VOLUME_MIN) {
                    setVolume(
                        value = volume.floatValue - VOLUME_STEP
                    )
                }
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

const val PLAYLIST = "radio.m3u"
private const val VOLUME_HIDE_TIMER = 3000L //3s

private const val VOLUME_MIN = 0F
private const val VOLUME_MAX = 1F
private const val VOLUME_STEP = 0.05F //5%
