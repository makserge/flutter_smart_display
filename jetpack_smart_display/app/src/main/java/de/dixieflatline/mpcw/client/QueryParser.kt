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

import java.io.UnsupportedEncodingException
import java.net.URLDecoder

object QueryParser {
    fun parse(query: String?): Map<String, String?> {
        val m: MutableMap<String, String?> = HashMap()
        if (query != null && query.isNotEmpty()) {
            for (pair in query.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                val offset = pair.indexOf("=")
                if (offset != -1) {
                    var key: String?
                    var value: String?
                    try {
                        key = URLDecoder.decode(pair.substring(0, offset), "UTF-8")
                            .trim { it <= ' ' }
                        value = URLDecoder.decode(pair.substring(offset + 1), "UTF-8")
                            .trim { it <= ' ' }
                    } catch (e: UnsupportedEncodingException) {
                        key = null
                        value = null
                    }
                    if (key != null) {
                        m[key] = value
                    }
                }
            }
        }
        return m
    }
}