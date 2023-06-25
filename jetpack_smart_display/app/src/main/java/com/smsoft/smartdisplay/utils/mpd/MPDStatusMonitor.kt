package com.smsoft.smartdisplay.utils.mpd

import com.smsoft.smartdisplay.utils.mpd.data.MPDState
import com.smsoft.smartdisplay.utils.mpd.event.StatusChangeListener
import de.dixieflatline.mpcw.client.CommunicationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val DEFAULT_STATUS_UPDATE_DELAY = 2000L

class MPDStatusMonitor(
    private val mpd: MPDHelper,
    private var statusChangeListener: StatusChangeListener,
    private val delay: Long = DEFAULT_STATUS_UPDATE_DELAY
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isTerminated = false

    init {
        startMonitor()
    }

    private fun startMonitor() {
        isTerminated = false
        coroutineScope.launch {
            processStatus()
        }
    }

    fun stopMonitor() {
        isTerminated = true
    }

    private suspend fun processStatus() {
        var oldConnectionState = true
        var oldPlaylistVersion = -1
        var oldSong = -1
        var oldElapsedTime = -1L
        var oldState = MPDState.UNKNOWN
        var oldVolume = -1
        var oldRepeat = false
        var oldRandom = false
        var oldUpdating = false

        while (!isTerminated) {
            val connectionState = mpd.isConnected
            var connectionStateChanged = false
            if (oldConnectionState != connectionState) {
                statusChangeListener.connectionStateChanged(connectionState)
                oldConnectionState = connectionState
                connectionStateChanged = true
            }
            if (!connectionState) {
                continue
            }
            try {
                var statusChanged = false
                if (connectionStateChanged) {
                    statusChanged = true
                } else {
                    val changes = mpd.waitForChanges()
                    if (changes.isEmpty()) {
                        continue
                    }
                    for (change in changes) {
                        if (change.startsWith("changed: database")
                            || change.startsWith("changed: playlist")
                            || change.startsWith("changed: player")
                            || change.startsWith("changed: mixer")
                            || change.startsWith("changed: output")
                            || change.startsWith("changed: options")
                        ) {
                            statusChanged = true
                            break
                        }
                    }
                }
                if (!statusChanged) {
                    continue
                }
                val status = mpd.getStatus()

                if (connectionStateChanged
                    || ((oldPlaylistVersion != status.playlistVersion) && (status.playlistVersion != -1))
                ) {
                    oldPlaylistVersion = status.playlistVersion
                    statusChangeListener.playlistChanged(status, oldPlaylistVersion)
                }
                if (connectionStateChanged || (oldSong != status.songPos)) {
                    oldSong = status.songPos
                    statusChangeListener.trackChanged(status, oldSong)
                }
                if (connectionStateChanged || (oldElapsedTime != status.elapsedTime)) {
                    statusChangeListener.trackPositionChanged(status)
                    oldElapsedTime = status.elapsedTime
                }
                if (connectionStateChanged || (oldState != status.state)) {
                    oldState = status.state
                    statusChangeListener.stateChanged(status, oldState)
                }
                if (connectionStateChanged || (oldVolume != status.volume)) {
                    oldVolume = status.volume
                    statusChangeListener.volumeChanged(status, oldVolume)
                }
                if (connectionStateChanged || (oldRepeat != status.isRepeat)) {
                    oldRepeat = status.isRepeat
                    statusChangeListener.repeatChanged(oldRepeat)
                }
                if (connectionStateChanged || (oldRandom != status.isRandom)) {
                    oldRandom = status.isRandom
                    statusChangeListener.randomChanged(oldRandom)
                }
                if (connectionStateChanged || (oldUpdating != status.isUpdating)) {
                    oldUpdating = status.isUpdating
                    statusChangeListener.libraryStateChanged(oldUpdating)
                }
            } catch (e: CommunicationException) {
                try {
                    mpd.reconnect()
                } catch (ignored: Exception) {
                }
            } catch (ignored: Exception) {
            }
            delay(delay)
        }
    }
}
