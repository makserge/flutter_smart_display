package com.smsoft.smartdisplay.service.radio

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.smsoft.smartdisplay.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Exposes the radio player as a media session while the radio page is shown.
 *
 * Media3 creates and updates the media notification and moves the service in and out of the
 * foreground as playback starts and stops, so the service is started with a plain startService().
 * The session belongs to this service instance. The player belongs to RadioMediaServiceHandler
 * and is shared with the radio page, so it is stopped here but never released. The handler
 * replaces it when the radio settings change; the session then moves to the new one, or, when
 * the new one is the MPD client (which never uses this service), the service ends.
 */
@UnstableApi
@AndroidEntryPoint
class RadioMediaService : MediaSessionService() {
    @Inject
    lateinit var radioMediaServiceHandler: RadioMediaServiceHandler

    private var mediaSession: MediaSession? = null
    private val serviceScope = MainScope()

    override fun onCreate() {
        super.onCreate()
        // Channel used by the notification code that Media3 replaced.
        NotificationManagerCompat.from(this).deleteNotificationChannel(LEGACY_NOTIFICATION_CHANNEL_ID)

        setMediaNotificationProvider(RadioNotificationProvider(this))
        // A stopped radio needs no notification: the page stops this service when it is left.
        setShowNotificationForIdlePlayer(SHOW_NOTIFICATION_FOR_IDLE_PLAYER_NEVER)

        followPlayer(radioMediaServiceHandler.currentPlayer.value)
        // The radio settings may replace the player while this service runs.
        serviceScope.launch {
            radioMediaServiceHandler.currentPlayer.collect { newPlayer ->
                followPlayer(newPlayer)
            }
        }
    }

    /**
     * Gives the session [player]: builds it on the first call, then moves it (same main looper, as
     * setPlayer requires) before the handler releases the old player.
     * This service is for the internal player only. For MPD the session is released at once and
     * the service stops itself: the handler may already have resumed MPD, and a session holding
     * the playing MPD player would show a media notification until onDestroy.
     */
    private fun followPlayer(player: Player) {
        if (player is MPDPlayer) {
            // Not built again by a later value: this instance is done.
            serviceScope.cancel()
            releaseSession()
            stopSelf()
            return
        }
        val session = mediaSession
        if (session == null) {
            // No session activity: the app is the HOME screen and is always shown; an explicit
            // MainActivity intent from the notification would start a second instance of it.
            mediaSession = MediaSession.Builder(this, player)
                .build()
                .also {
                    // Registering the session lets Media3 manage the notification without a controller.
                    addSession(it)
                }
        } else if (session.player !== player) {
            session.player = player
        }
    }

    private fun releaseSession() {
        mediaSession?.release()
        mediaSession = null
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // The radio only plays while its page is visible. Do not let the system recreate an
        // empty service after the process was killed.
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        radioMediaServiceHandler.stop()
        stopSelf()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        releaseSession()
        super.onDestroy()
    }
}

/** Default Media3 notification showing the station name as the title and the current song as the text. */
@UnstableApi
private class RadioNotificationProvider(context: Context) : DefaultMediaNotificationProvider(
    context,
    { NOTIFICATION_ID },
    NOTIFICATION_CHANNEL_ID,
    R.string.radio
) {
    init {
        setSmallIcon(R.drawable.ic_small_notification)
    }

    override fun getNotificationContentTitle(metadata: MediaMetadata): CharSequence? =
        metadata.displayTitle ?: metadata.title

    override fun getNotificationContentText(metadata: MediaMetadata): CharSequence? =
        if (metadata.displayTitle != null) metadata.title else null
}

private const val NOTIFICATION_ID = 200
private const val NOTIFICATION_CHANNEL_ID = "radio_playback"
private const val LEGACY_NOTIFICATION_CHANNEL_ID = "radio player channel id 1"
