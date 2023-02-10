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

import de.dixieflatline.mpcw.diff.ITransformation
import java.util.*

interface IPlaylist {
    @Throws(CommunicationException::class, ProtocolException::class)
    fun synchronize(): Iterable<ITransformation?>?
    fun selectedIndex(): Int
    val playlistItems: Iterable<PlaylistItem?>?

    @Throws(CommunicationException::class, ProtocolException::class)
    fun appendSong(song: Song?)

    @Throws(CommunicationException::class, ProtocolException::class)
    fun appendSong(filename: String?)

    @Throws(CommunicationException::class, ProtocolException::class)
    fun appendSongs(songs: Iterable<Song?>?)

    @Throws(CommunicationException::class, ProtocolException::class)
    fun selectAndPlay(
        offset: Int,
        flags: EnumSet<SelectAndPlayFlags>?
    ): Iterable<ITransformation?>?
}