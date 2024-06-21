package com.smsoft.smartdisplay.utils.mpd

import com.smsoft.smartdisplay.utils.mpd.data.MPDCommand
import de.dixieflatline.mpcw.client.AuthenticationException
import de.dixieflatline.mpcw.client.CommunicationException
import de.dixieflatline.mpcw.client.IClient
import de.dixieflatline.mpcw.client.ProtocolException
import de.dixieflatline.mpcw.client.mpd.*
import java.net.Socket

class Connection(
    private val host: String,
    private val port: Int,
    private val password: String
) {
    private lateinit var channel: Channel

    val client: IClient
        get() = Client(channel)

    var version: Version? = null

    val isConnected: Boolean
        get() = this::channel.isInitialized && channel.isConnected

    @Throws(CommunicationException::class, AuthenticationException::class)
    fun connect() {
        if (!isConnected) {
            if (host.isEmpty()) {
                throw AuthenticationException("Invalid host")
            }
            openChannel()
            detectVersion()
            if (password.isNotEmpty()) {
                try {
                    authenticate()
                } catch (e: AckException) {
                    throw CommunicationException(e)
                } catch (e: ProtocolException) {
                    throw CommunicationException(e)
                }
            }
        }
    }

    @Throws(CommunicationException::class)
    private fun openChannel() {
        channel = try {
            val socket = Socket(host, port)
            Channel(socket)
        } catch (e: Exception) {
            throw CommunicationException(e)
        }
    }

    @Throws(CommunicationException::class, AckException::class, ProtocolException::class)
    private fun detectVersion() {
        val response = channel.receive()
        if (response.size() == 1) {
            val line = response.iterator().next()
            try {
                version = line?.let { Version.parse(it) }
            } catch (e: InvalidFormatException) {
                throw ProtocolException(e.message)
            }
        } else {
            throw ProtocolException("Couldn't detect MPD version.")
        }
    }

    @Throws(AuthenticationException::class, CommunicationException::class)
    private fun authenticate() {
        try {
            channel.send("password $password")
        } catch (e: ProtocolException) {
            channel.close()
            throw AuthenticationException("Authentication failed.")
        }
    }

    @Throws(CommunicationException::class)
    fun disconnect() {
        try {
            channel.close()
        } catch (e: Exception) {
            throw CommunicationException(e)
        }
    }
    @Throws(CommunicationException::class, ProtocolException::class)
    fun sendCommand(command: MPDCommand): List<String> {
        if (this::channel.isInitialized) {
            return (channel.send(command.id) as Ok).response
        } else {
            throw CommunicationException("")
        }
    }

    @Throws(CommunicationException::class, ProtocolException::class)
    fun sendCommand(command: MPDCommand, vararg args: String): List<String> {
        if (!this::channel.isInitialized) {
            throw CommunicationException("")
        }
        val outBuf = StringBuffer().apply {
            append(command.id)
            for (arg in args) {
                append(" \"")
                append(arg.replace("\"".toRegex(), "\\\\\""))
                append("\"")
            }
            append("\n")
        }
        return (channel.send(outBuf.toString().trim()) as Ok).response
    }
}