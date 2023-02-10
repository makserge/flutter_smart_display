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
package de.dixieflatline.mpcw.client

import java.net.URI

class ConnectionFactory : IFactory<IConnection> {
    @Throws(DispatchException::class)
    override fun create(uri: URI?): IConnection {
        val className = buildClassName(uri)
        return try {
            val cls = Class.forName(className)
            val constructor = cls.getConstructor(URI::class.java)
            constructor.newInstance(uri) as IConnection
        } catch (ex: Exception) {
            throw DispatchException(ex)
        }
    }

    @Throws(Exception::class)
    override fun idle(connection: IConnection) {
        if (!connection.isIdle()) {
            connection.idle()
        }
    }

    @Throws(Exception::class)
    override fun recycle(connection: IConnection): IConnection {
        if (connection.isIdle()) {
            connection.noidle()
        }
        return connection
    }

    override fun dispose(connection: IConnection) {
        try {
            if (connection.isConnected()) {
                connection.disconnect()
            }
        } catch (ignored: CommunicationException) {
        }
    }

    override fun valid(connection: IConnection): Boolean {
        var connected = false
        try {
            if (connection.isConnected()) {
                val wasIdle = connection.isIdle()
                if (wasIdle) {
                    connection.noidle()
                }
                connected = connection.ping()
                if (wasIdle) {
                    connection.idle()
                }
            }
        } catch (ex: CommunicationException) {
            connected = false
        } catch (ex: ProtocolException) {
            connected = false
        }
        return connected
    }

    companion object {
        private fun buildClassName(uri: URI?): String {
            val packageName =
                Connection::class.java.getPackage()?.name
            return String.format("%s.%s.Connection", packageName, uri!!.scheme)
        }
    }
}