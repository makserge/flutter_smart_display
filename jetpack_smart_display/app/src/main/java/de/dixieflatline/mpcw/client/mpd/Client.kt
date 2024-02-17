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
import de.dixieflatline.mpcw.client.IBrowser
import de.dixieflatline.mpcw.client.IClient
import de.dixieflatline.mpcw.client.IPlayer
import de.dixieflatline.mpcw.client.IPlaylist
import de.dixieflatline.mpcw.client.PlaylistItem
import de.dixieflatline.mpcw.client.ProtocolException
import java.util.function.Consumer

class Client(private val channel: Channel) : IClient {

    override fun getPlayer(): IPlayer {
        return Player(channel)
    }

    @Throws(CommunicationException::class, ProtocolException::class)
    override fun getCurrentPlaylist(): IPlaylist {
        return CurrentPlaylist(channel)
    }

    @Throws(CommunicationException::class, ProtocolException::class)
    override fun resyncCurrentPlaylist(playlist: IPlaylist): IPlaylist {
        val list: MutableList<PlaylistItem> = ArrayList()
        playlist.playlistItems?.forEach(Consumer { item: PlaylistItem -> list.add(item) } as (PlaylistItem?) -> Unit)
        return CurrentPlaylist(channel, list.toTypedArray())
    }

    override fun getBrowser(): IBrowser {
        return Browser(channel)
    }
}