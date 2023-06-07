package com.smsoft.smartdisplay.utils.mpd

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.smsoft.smartdisplay.utils.mpd.data.MPDCommand
import com.smsoft.smartdisplay.utils.mpd.data.MPDCredentials
import com.smsoft.smartdisplay.utils.mpd.data.MPDStatus
import com.smsoft.smartdisplay.utils.mpd.event.StatusChangeListener
import de.dixieflatline.mpcw.client.AuthenticationException
import de.dixieflatline.mpcw.client.CommunicationException
import de.dixieflatline.mpcw.client.ProtocolException
import java.net.ConnectException
import java.util.*

class MPDHelper {
    val isConnected: Boolean
        get() = this::statusConnection.isInitialized && statusConnection.isConnected

    var statusChangedListener: StatusChangeListener? = null

    private val TAG = "MPDHelper"

    private lateinit var commandConnection: Connection
    private lateinit var statusConnection: Connection
    private lateinit var statusMonitor: MPDStatusMonitor

    fun connect(credentials: MPDCredentials): Boolean {
        commandConnection = Connection(
            host = credentials.host,
            port = credentials.port,
            password = credentials.password
        )
        statusConnection = Connection(
            host = credentials.host,
            port = credentials.port,
            password = credentials.password
        )
        return try {
            reconnect()
            true
        } catch (e: CommunicationException) {
            if (e.cause is ConnectException) {
                return false
            }
            return connect(credentials)
        }
        catch (e: AuthenticationException) {
            Log.e(TAG, e.message.toString())
            false
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
            false
        }
    }

    @Throws(CommunicationException::class, AuthenticationException::class)
    fun reconnect() {
        if (commandConnection.isConnected) {
            commandConnection.disconnect()
        }
        commandConnection.connect()
        if (statusConnection.isConnected) {
            statusConnection.disconnect()
        }
        statusConnection.connect()
    }

    @Throws(CommunicationException::class)
    fun disconnect() {
        try {
            if (commandConnection.isConnected) {
                commandConnection.disconnect()
            }
        } catch(e: Exception) {
            Log.e(TAG, e.message.toString())
        }
        try {
            if (statusConnection.isConnected) {
                statusConnection.disconnect()
            }
        } catch(e: Exception) {
            Log.e(TAG, e.message.toString())
        }
    }

    fun startMonitor() {
        statusMonitor = MPDStatusMonitor(
            mpd = this,
            statusChangeListener = statusChangedListener!!
        )
    }

    fun stopMonitor() {
        if (this::statusMonitor.isInitialized) {
            statusMonitor.stopMonitor()
        }
    }

    @Throws(CommunicationException::class, ProtocolException::class)
    fun waitForChanges(): List<String> {
        return statusConnection.sendCommand(MPDCommand.IDLE)
    }

    @Throws(CommunicationException::class, ProtocolException::class)
    fun getStatus(): MPDStatus {
        val response = statusConnection.sendCommand(MPDCommand.STATUS)
        return MPDStatus(response)
    }

    @Throws(CommunicationException::class, ProtocolException::class)
    fun getPlaylist(): List<MediaItem> {
        val response = statusConnection.sendCommand(MPDCommand.PLAYLIST)
        return parsePlayList(response)
    }

    @Throws(CommunicationException::class, ProtocolException::class)
    fun updatePlaylist(playlistVersion: Int = -1): List<MediaItem> {
        val response = statusConnection.sendCommand(MPDCommand.PLAYLIST_CHANGES,
            playlistVersion.toString()
        )
        return parsePlayList(response)
    }

    fun play() {
        try {
            if (commandConnection.isConnected) {
                commandConnection.sendCommand(MPDCommand.PLAY)
            } else {
                reconnect()
                play()
            }
        } catch(E: CommunicationException) {
            reconnect()
            play()
        } catch(ignored: ProtocolException) {
        }
    }

    fun pause() {
        try {
            if (commandConnection.isConnected) {
                commandConnection.sendCommand(MPDCommand.PAUSE)
            } else {
                reconnect()
                pause()
            }
        } catch(e: CommunicationException) {
            reconnect()
            pause()
        } catch(ignored: ProtocolException) {
        }
    }

    fun previous() {
        try {
            if (commandConnection.isConnected) {
                commandConnection.sendCommand(MPDCommand.PREVIOUS)
            } else {
                reconnect()
                previous()
            }
        } catch(e: CommunicationException) {
            reconnect()
            previous()
        } catch(ignored: ProtocolException) {
        }
    }

    fun next() {
        try {
            if (commandConnection.isConnected) {
                commandConnection.sendCommand(MPDCommand.NEXT)
            } else {
                reconnect()
                next()
            }
        } catch(e: CommunicationException) {
            reconnect()
            next()
        } catch(ignored: ProtocolException) {
        }
    }

    fun playId(mediaId: String) {
        try {
            if (commandConnection.isConnected) {
                commandConnection.sendCommand(MPDCommand.PLAY_ID, mediaId)
            } else {
                reconnect()
                playId(mediaId)
            }
        } catch(e: CommunicationException) {
            reconnect()
            playId(mediaId)
        } catch(ignored: ProtocolException) {
        }
    }

    fun setVolume(volume: Int) {
        try {
            if (commandConnection.isConnected) {
                commandConnection.sendCommand(MPDCommand.SET_VOLUME, volume.toString())
            } else {
                reconnect()
                setVolume(volume)
            }
        } catch(e: CommunicationException) {
            reconnect()
            setVolume(volume)
        } catch(ignored: ProtocolException) {
        }
    }

    private fun parsePlayList(response: List<String>): ArrayList<MediaItem> {
        val result = ArrayList<MediaItem>()
        val lineCache = LinkedList<String>()
        for (line in response) {
            if (line.startsWith("file: ")) {
                if (lineCache.size != 0) {
                    result.add(parsePlayListItem(lineCache))
                    lineCache.clear()
                }
            }
            lineCache.add(line)
        }
        if (lineCache.size != 0) {
            result.add(parsePlayListItem(lineCache))
        }
        return result
    }

    private fun parsePlayListItem(lineCache: LinkedList<String>): MediaItem {
        var songId = "0"
        var uri = ""
        var name = ""
        var title = ""
        for (line in lineCache) {
            if (line.startsWith("Id:")) {
                try {
                    songId = line.substring("Id: ".length)
                } catch (ignored: NumberFormatException) {
                }
            } else if (line.startsWith("file:")) {
                uri = line.substring("file: ".length)
                if (uri.contains("://")) {
                    getStreamName(uri)?.let {
                        name = it
                    }
                }
            }
            else if (line.startsWith("Name:")) {
                name = line.substring("Name: ".length)
            }
            else if (title.isEmpty() &&line.startsWith("Title:")) {
                title = line.substring("Title: ".length)
            }
        }
        if (name.isEmpty()) {
            name = title
        }
        return MediaItem.Builder()
            .setMediaId(songId)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setDisplayTitle(name)
                    .setTitle(title)
                    .build()
            ).build()
    }

    private fun getStreamName(path: String): String? {
        if (path.isNotEmpty()) {
            val pos = path.indexOf("#")
            if (pos > 1) {
                return path.substring(pos + 1, path.length)
            }
        }
        return null
    }

}