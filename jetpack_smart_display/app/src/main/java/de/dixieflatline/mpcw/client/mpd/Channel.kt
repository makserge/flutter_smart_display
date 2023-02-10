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

import de.dixieflatline.mpcw.client.CommunicationException
import de.dixieflatline.mpcw.client.ProtocolException
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket
import java.nio.charset.StandardCharsets

class Channel(private var socket: Socket) {
    private val reader =
        BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
    private val writer = DataOutputStream(socket.getOutputStream())

    @Throws(CommunicationException::class, ProtocolException::class)
    fun send(vararg commands: String): IResponse {
        val isCommandList = commands.size > 1
        if (isCommandList) {
            writeLine("command_list_begin")
        }
        for (command in commands) {
            writeLine(command)
        }
        if (isCommandList) {
            writeLine("command_list_end")
        }
        return receive()
    }

    @Throws(CommunicationException::class)
    fun writeLine(line: String) {
        try {
            val bytes = line.toByteArray(StandardCharsets.UTF_8)
            writer.write(bytes)
            writer.write('\n'.code)
        } catch (ex: IOException) {
            throwCommunicationExceptionAndShutdown(ex)
        }
    }

    @Throws(CommunicationException::class, ProtocolException::class)
    fun receive(): IResponse {
        val lines = ArrayList<String>()
        var line: String? = null
        var response: IResponse? = null
        try {
            while (response == null) {
                try {
                    line = reader.readLine()
                    if (line == null) {
                        throw IOException("Receive failed.")
                    }
                    line = line.trim { it <= ' ' }
                } catch (ex: IOException) {
                    throwCommunicationExceptionAndShutdown(ex)
                }
                lines.add(line!!)
                if (line.startsWith("ACK")) {
                    throw AckException(Ack(line))
                } else if (line.startsWith("OK")) {
                    response = Ok(lines)
                }
            }
        } catch (ex: AckException) {
            throw ProtocolException(ex)
        }
        return response
    }

    @Throws(CommunicationException::class)
    private fun throwCommunicationExceptionAndShutdown(cause: Exception) {
        try {
            socket.close()
        } catch (ignored: IOException) {
        }
        throw CommunicationException(cause)
    }

    val isConnected: Boolean
        get() = !socket.isClosed

    @Throws(CommunicationException::class)
    fun close() {
        try {
            socket.close()
        } catch (ex: IOException) {
            throw CommunicationException(ex)
        }
    }
}