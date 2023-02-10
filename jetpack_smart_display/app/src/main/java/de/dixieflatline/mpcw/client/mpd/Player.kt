/***************************************************************************
 * begin........: September 2018
 * copyright....: Sebastian Fedrau
 * email........: sebastian.fedrau@gmail.com
 */
/***************************************************************************
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License v3 for more details.
 */
package de.dixieflatline.mpcw.client.mpd

import android.os.Build
import androidx.annotation.RequiresApi
import de.dixieflatline.mpcw.client.*
import de.dixieflatline.mpcw.client.mpd.ResponseParser.reponseToMap

class Player(private val channel: Channel) : IPlayer {
    @get:Throws(
        CommunicationException::class,
        ProtocolException::class
    )
    @get:RequiresApi(api = Build.VERSION_CODES.N)
    override val status: Status
        get() {
            val status = Status()
            try {
                var response = channel.send("status")
                var m: Map<String, String> = reponseToMap(response)
                val state = parseState(m["state"])
                status.state = state
                if (m.containsKey("song") && m["song"] != "0") {
                    status.setHasPrevious(true)
                }
                if (m.containsKey("nextsong")) {
                    status.setHasNext(true)
                }
                var playlistCommand = "playlistinfo 0"
                if (m.containsKey("songid")) {
                    playlistCommand = "playlistid " + m["songid"]
                }
                if ((m["playlistlength"] ?: "0") != "0") {
                    response = channel.send(playlistCommand)
                    m = reponseToMap(response)
                    if (m.containsKey("Artist")) {
                        status.artist = m["Artist"]
                    }
                    if (m.containsKey("Title")) {
                        status.title = m["Title"]
                    }
                }
            } catch (ex: InvalidFormatException) {
                throw ProtocolException(ex)
            }
            return status
        }

    @Throws(CommunicationException::class, ProtocolException::class)
    override fun play() {
        channel.send("play")
    }

    @Throws(CommunicationException::class, ProtocolException::class)
    override fun pause() {
        channel.send("pause")
    }

    @Throws(CommunicationException::class, ProtocolException::class)
    override fun stop() {
        channel.send("stop")
    }

    @Throws(CommunicationException::class, ProtocolException::class)
    override fun next() {
        channel.send("play", "next")
    }

    @Throws(CommunicationException::class, ProtocolException::class)
    override fun previous() {
        channel.send("play", "previous")
    }

    @Throws(CommunicationException::class, ProtocolException::class)
    override fun clear() {
        channel.send("clear")
    }

    companion object {
        @Throws(ProtocolException::class)
        fun parseState(name: String?): EState {
            return when (name) {
                "stop" -> {
                    EState.Stop
                }
                "play" -> {
                    EState.Play
                }
                "pause" -> {
                    EState.Pause
                }
                else -> {
                    throw ProtocolException("Unknown state: $name")
                }
            }
        }
    }
}