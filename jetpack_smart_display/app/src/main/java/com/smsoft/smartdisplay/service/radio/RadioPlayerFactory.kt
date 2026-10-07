package com.smsoft.smartdisplay.service.radio

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.RadioType
import com.smsoft.smartdisplay.ui.screen.radio.PLAYLIST
import com.smsoft.smartdisplay.ui.screen.settings.MPD_SERVER_DEFAULT_HOST
import com.smsoft.smartdisplay.ui.screen.settings.MPD_SERVER_DEFAULT_PORT
import com.smsoft.smartdisplay.utils.m3uparser.M3uParser
import com.smsoft.smartdisplay.utils.mpd.data.MPDCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * The radio settings the radio player is built from. [mpd] is null for the internal radio, so
 * editing the MPD server while the internal radio is selected changes nothing.
 */
data class RadioConfig(
    val type: RadioType,
    val mpd: MPDCredentials?
)

/**
 * Reads the [RadioConfig] from the settings; RadioMediaServiceHandler follows it.
 * The MPD server is left out unless MPD is the radio type, so editing it then changes nothing.
 */
fun readRadioConfig(data: Preferences): RadioConfig {
    val type = RadioType.getById(
        data[stringPreferencesKey(PreferenceKey.RADIO_TYPE.key)] ?: RadioType.getDefaultId()
    )
    if (type != RadioType.MPD) {
        return RadioConfig(type = type, mpd = null)
    }
    val host = data[stringPreferencesKey(PreferenceKey.MPD_SERVER_HOST.key)] ?: MPD_SERVER_DEFAULT_HOST
    val port = data[stringPreferencesKey(PreferenceKey.MPD_SERVER_PORT.key)] ?: MPD_SERVER_DEFAULT_PORT
    val password = data[stringPreferencesKey(PreferenceKey.MPD_SERVER_PASSWORD.key)] ?: ""
    return RadioConfig(
        type = type,
        mpd = MPDCredentials(
            host = host.trim(),
            // Free-text setting: an invalid port must not crash the app when the radio is opened.
            port = port.trim().toIntOrNull() ?: MPD_SERVER_DEFAULT_PORT.toInt(),
            password = password
        )
    )
}

/**
 * Builds the radio player for RadioMediaServiceHandler. Called on the main thread: every radio
 * player must use the main looper, because the media session moves between them (setPlayer).
 */
@UnstableApi
class RadioPlayerFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioAttributes: AudioAttributes
) {
    fun create(config: RadioConfig): Player = when (config.type) {
        // Every internal player gets the stations; MPD's playlist is its own queue.
        RadioType.INTERNAL -> ExoPlayerImpl.getExoPlayer(
            context = context,
            audioAttributes = audioAttributes
        ).apply {
            setMediaItems(loadStations())
        }
        RadioType.MPD -> MPDPlayer(
            credentials = checkNotNull(config.mpd)
        )
    }

    private fun loadStations(): List<MediaItem> {
        val m3uStream = context.assets.open(PLAYLIST)
        return M3uParser.parse(m3uStream.reader()).map {
            MediaItem.Builder()
                .setUri(it.location.url.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setDisplayTitle(it.title)
                        .build()
                ).build()
        }
    }
}
