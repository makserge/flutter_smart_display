package com.smsoft.smartdisplay.ui.screen.radio

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
import kotlin.math.floor

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class RadioViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val radioMediaServiceHandler: RadioMediaServiceHandler,
    savedStateHandle: SavedStateHandle,
    val dataStore: DataStore<Preferences>
) : ViewModel() {
    var presetTitle by savedStateHandle.saveable { mutableStateOf("") }
    var duration by savedStateHandle.saveable { mutableStateOf(0L) }
    var progress by savedStateHandle.saveable { mutableStateOf(0F) }
    var progressString by savedStateHandle.saveable { mutableStateOf("00:00") }
    var isPlaying by savedStateHandle.saveable { mutableStateOf(false) }
    var metaTitle by savedStateHandle.saveable { mutableStateOf("") }
    var volume by savedStateHandle.saveable { mutableStateOf(0F) }

    private val uiStateInt = MutableStateFlow<UIState>(UIState.Initial)
    val uiState = uiStateInt.asStateFlow()

    private var volumeHideTimer: CountDownTimer? = null

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

    private fun isInternalPlayer(): Boolean {
        return getRadioType(dataStore) == RadioType.INTERNAL
    }

    private fun saveCurrentPreset(currentMediaItemIndex: Int) {
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

    fun onUIEvent(uiEvent: UIEvent) = viewModelScope.launch {
        when (uiEvent) {
            UIEvent.Backward -> radioMediaServiceHandler.onPlayerEvent(PlayerEvent.Previous)
            UIEvent.Forward -> radioMediaServiceHandler.onPlayerEvent(PlayerEvent.Next)
            UIEvent.PlayPause -> radioMediaServiceHandler.onPlayerEvent(PlayerEvent.PlayPause)
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


    fun formatDuration(duration: Long): String {
        val totalSeconds = floor(duration / 1E3).toInt()
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

    private fun calculateProgressValues(currentProgress: Long) {
        progress = if (currentProgress > 0) (currentProgress.toFloat() / duration) else 0f
        progressString = formatDuration(currentProgress)
    }

    private fun convertCharset(data: String): String {
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

    internal fun onStart() {
        viewModelScope.launch {
            val preset = getRadioPreset(dataStore)
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
            radioMediaServiceHandler.addMediaItemList(mediaItemList, preset)
        }
    }

    internal fun setVolume(value: Float) {
        radioMediaServiceHandler.setVolume(value)
    }

    internal fun reStartVolumeHideTimer(callback: () -> Unit) {
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
}

sealed class UIEvent {
    object PlayPause : UIEvent()
    object Backward : UIEvent()
    object Forward : UIEvent()
    data class UpdateProgress(val newProgress: Float) : UIEvent()
}

sealed class UIState {
    object Initial : UIState()
    object Error : UIState()
    object Ready : UIState()
}

private const val PLAYLIST = "radio.m3u"
private const val VOLUME_HIDE_TIMER = 3000L //3s