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

interface IPlayer {
    @get:Throws(
        CommunicationException::class,
        ProtocolException::class
    )
    val status: Status?

    @Throws(CommunicationException::class, ProtocolException::class)
    fun play()

    @Throws(CommunicationException::class, ProtocolException::class)
    fun pause()

    @Throws(CommunicationException::class, ProtocolException::class)
    fun stop()

    @Throws(CommunicationException::class, ProtocolException::class)
    operator fun next()

    @Throws(CommunicationException::class, ProtocolException::class)
    fun previous()

    @Throws(CommunicationException::class, ProtocolException::class)
    fun clear()
}