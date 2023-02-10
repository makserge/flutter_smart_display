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

import android.util.Pair

object ResponseParser {
    @JvmStatic
	@Throws(InvalidFormatException::class)
    fun reponseToMap(response: IResponse): Map<String, String> {
        val m: MutableMap<String, String> = HashMap()
        for (line in response) {
            if (line != "OK") {
                val pair = splitLineIntoPair(line!!)
                m[pair.first] = pair.second
            }
        }
        return m
    }

    @JvmStatic
	@Throws(InvalidFormatException::class)
    fun splitLineIntoPair(line: String): Pair<String, String> {
        val offset = line.indexOf(':')
        if (offset == -1) {
            throw InvalidFormatException("No colon found in line.")
        }
        val key = line.substring(0, offset)
        val value = line.substring(offset + 1).trim { it <= ' ' }
        return Pair(key, value)
    }
}