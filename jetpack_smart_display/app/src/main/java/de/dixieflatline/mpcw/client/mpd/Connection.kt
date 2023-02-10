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
import java.net.Socket
import java.net.URI

@RequiresApi(api = Build.VERSION_CODES.N)
class Connection constructor(uri: URI) : IConnection {
    private val uri: URI
    private val hostname: String
    private var port = DEFAULT_PORT
    private val password: String?
    private var channel: Channel? = null
    var version: Version? = null
        private set
    private var _isIdle = false

    init {
        hostname = uri.host
        val port = uri.port
        if (port != -1) {
            this.port = port
        }
        val params = QueryParser.parse(uri.query)
        password = params.getOrDefault("password", null)
        this.uri = uri
    }

    override fun getURI(): URI {
        return uri
    }

    override fun isConnected(): Boolean {
        return channel != null && channel!!.isConnected
    }

    @Throws(CommunicationException::class, AuthenticationException::class)
    override fun connect() {
        if (!isConnected()) {
            openChannel()
            try {
                detectVersion()
                authenticate()
            } catch (ex: AckException) {
                throw CommunicationException(ex)
            } catch (ex: ProtocolException) {
                throw CommunicationException(ex)
            }
        }
    }

    @Throws(CommunicationException::class)
    private fun openChannel() {
        channel = try {
            val socket = Socket(hostname, port)
            Channel(socket)
        } catch (ex: Exception) {
            throw CommunicationException(ex)
        }
    }

    @Throws(CommunicationException::class, AckException::class, ProtocolException::class)
    private fun detectVersion() {
        val response = channel!!.receive()
        version = if (response.size() == 1) {
            val line = response.iterator().next()
            try {
                Version.parse(line!!)
            } catch (ex: InvalidFormatException) {
                throw ProtocolException(ex.message)
            }
        } else {
            throw ProtocolException("Couldn't detect MPD version.")
        }
    }

    @Throws(AuthenticationException::class, CommunicationException::class)
    private fun authenticate() {
        if (password != null) {
            try {
                channel!!.send("password $password")
            } catch (ex: ProtocolException) {
                channel!!.close()
                throw AuthenticationException("Authentication failed.")
            }
        }
    }

    @Throws(CommunicationException::class)
    override fun disconnect() {
        try {
            ConnectionPool.instance?.dispose(this)
        } catch (ex: Exception) {
            throw CommunicationException(ex)
        }
    }

    @Throws(CommunicationException::class, ProtocolException::class)
    override fun ping(): Boolean {
        val response = channel!!.send("ping")
        return response is Ok
    }

    @Throws(CommunicationException::class)
    override fun idle() {
        if (!_isIdle) {
            channel!!.writeLine("idle")
            _isIdle = true
        }
    }

    @Throws(CommunicationException::class, ProtocolException::class)
    override fun noidle() {
        if (_isIdle) {
            val response = channel!!.send("noidle")
            if (response is Ok) {
                _isIdle = false
            }
        }
    }

    override fun isIdle(): Boolean {
        return _isIdle
    }

    override fun getClient(): IClient {
        return Client(channel!!)
    }

    companion object {
        private const val DEFAULT_PORT = 6600
    }
}