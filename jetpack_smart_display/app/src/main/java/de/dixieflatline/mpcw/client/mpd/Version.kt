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

import java.util.regex.Pattern

class Version(private val major: Int, private val minor: Int, private val revision: Int) {
    override fun toString(): String {
        return String.format("%d.%d.%d", major, minor, revision)
    }

    companion object {
        @Throws(InvalidFormatException::class)
        fun parse(version: String): Version {
            val pattern = Pattern.compile("^OK MPD (\\d+)\\.(\\d+)\\.(\\d+)$")
            val matcher = pattern.matcher(version)
            if (matcher.find()) {
                val major = matcher.group(1)?.toInt()
                val minor = matcher.group(2)?.toInt()
                val revision = matcher.group(3)?.toInt()
                return Version(major!!, minor!!, revision!!)
            }
            throw InvalidFormatException(version)
        }
    }
}