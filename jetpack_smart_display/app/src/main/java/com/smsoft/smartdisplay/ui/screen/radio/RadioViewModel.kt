package com.smsoft.smartdisplay.ui.screen.radio

import android.content.Context
import android.os.Build
import android.text.Html
import androidx.compose.runtime.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.smsoft.smartdisplay.service.MediaState
import com.smsoft.smartdisplay.service.PlayerEvent
import com.smsoft.smartdisplay.service.RadioMediaServiceHandler
import com.smsoft.smartdisplay.ui.screen.clocksettings.ClockSettingsViewModel
import com.smsoft.smartdisplay.utils.m3uparser.M3uParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class RadioViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val radioMediaServiceHandler: RadioMediaServiceHandler,
    savedStateHandle: SavedStateHandle,
    userPreferencesRepository: ClockSettingsViewModel.UserPreferencesRepository
) : ViewModel() {

    val dataStore = userPreferencesRepository.dataStore

    class UserPreferencesRepository @Inject constructor(
        val dataStore: DataStore<Preferences>
    )

    var presetTitle by savedStateHandle.saveable { mutableStateOf("") }
    var duration by savedStateHandle.saveable { mutableStateOf(0L) }
    var progress by savedStateHandle.saveable { mutableStateOf(0f) }
    var progressString by savedStateHandle.saveable { mutableStateOf("00:00") }
    var isPlaying by savedStateHandle.saveable { mutableStateOf(false) }
    var metaTitle by savedStateHandle.saveable { mutableStateOf("") }

    private val uiStateInt = MutableStateFlow<UIState>(UIState.Initial)
    val uiState = uiStateInt.asStateFlow()

    val prefs = dataStore.data

    init {
        viewModelScope.launch {
            loadData(PLAYLIST)

            radioMediaServiceHandler.mediaState.collect { mediaState ->
                when (mediaState) {
                    is MediaState.Buffering -> calculateProgressValues(mediaState.progress)
                    MediaState.Initial -> uiStateInt.value = UIState.Initial
                    is MediaState.Playing -> isPlaying = mediaState.isPlaying
                    is MediaState.Progress -> calculateProgressValues(mediaState.progress)
                    is MediaState.Ready -> {
                        saveCurrentPreset(mediaState.currentMediaItemIndex)
                        presetTitle = if (mediaState.currentMediaItem != null) mediaState.currentMediaItem.mediaMetadata.displayTitle.toString() else ""
                        duration = if (mediaState.duration > 0) mediaState.duration else 0
                        uiStateInt.value = UIState.Ready
                    }
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
            radioMediaServiceHandler.mediaMetadata.collect { mediaMetadata ->
                mediaMetadata.title?.let {
                    metaTitle = convertCharset(it as String)
                }
            }
        }
    }

    private fun saveCurrentPreset(currentMediaItemIndex: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[intPreferencesKey(PLAYLIST_POSITION)] = currentMediaItemIndex
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
        val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS)
                - minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun calculateProgressValues(currentProgress: Long) {
        progress = if (currentProgress > 0) (currentProgress.toFloat() / duration) else 0f
        progressString = formatDuration(currentProgress)
    }

    private fun loadData(playlist: String) {
        val m3uStream = context.getAssets().open(playlist)
        val m3uReader: InputStreamReader = m3uStream.reader()
        val streamEntries = M3uParser.parse(m3uReader)
        val mediaItemList = mutableListOf<MediaItem>()
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

        viewModelScope.launch {
            prefs.collect { data ->
                var position = 1
                data.get(intPreferencesKey(PLAYLIST_POSITION))?.let {
                    position = it
                }
                radioMediaServiceHandler.playPlaylistItem(position)
            }
        }
    }

    fun fadeInVolume() {
        radioMediaServiceHandler.fadeInVolume()
    }

    @Suppress("DEPRECATION")
    private fun convertCharset(data: String): String {
        if (data.isNotEmpty()) {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(data, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(data)
            }.toString()
        }
        return data
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
    object Ready : UIState()
}

private const val PLAYLIST = "radio.m3u"
private const val PLAYLIST_POSITION = "radio_playlist_pos"