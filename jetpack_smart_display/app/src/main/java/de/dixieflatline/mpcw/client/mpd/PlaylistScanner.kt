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
import de.dixieflatline.mpcw.client.PlaylistItem
import java.util.function.Consumer

class PlaylistScanner {
    private var firstItemFound = false
    private var artist: String? = null
    private var title: String? = null
    private val listeners: MutableList<IPlaylistScannerListener>

    init {
        listeners = ArrayList()
    }

    fun reset() {
        firstItemFound = false
        artist = null
        title = null
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Throws(InvalidFormatException::class)
    fun feed(line: String) {
        val pair = ResponseParser.splitLineIntoPair(line)
        if (pair.first.equals("file")) {
            if (firstItemFound) {
                raiseOnPlaylistItemFound()
            }
            firstItemFound = true
            artist = null
            title = null
        } else if (pair.first.equals("Artist")) {
            artist = pair.second
        } else if (pair.first.equals("Title")) {
            title = pair.second
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun flush() {
        raiseOnPlaylistItemFound()
    }

    fun addListener(listener: IPlaylistScannerListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: IPlaylistScannerListener) {
        listeners.remove(listener)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun raiseOnPlaylistItemFound() {
        if (artist != null && !artist!!.isEmpty() || title != null && !title!!.isEmpty()) {
            val item = PlaylistItem(artist, title)
            listeners.forEach(Consumer { l: IPlaylistScannerListener -> l.onPlaylistItemFound(item) })
        }
    }
}