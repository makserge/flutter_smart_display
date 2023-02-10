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

import de.dixieflatline.mpcw.client.mpd.IResponse
import de.dixieflatline.mpcw.client.mpd.TagMapper

class TagIterator(tag: ETag, response: IResponse) : MutableIterator<Tag> {
    private val tagName = TagMapper.map(tag)
    private val linePrefix = String.format("%s:", tagName)
    private val source = response.iterator() as Iterator<String>
    private val tag = Tag(tag)
    private var lineCount = response.size() - 1
    private var count = 0

    override fun hasNext(): Boolean {
        return count < lineCount
    }

    override fun next(): Tag {
        val line = source.next()
        if (line.startsWith(linePrefix)) {
            val value = line.substring(linePrefix.length)
                .trim { it <= ' ' }
            tag.value = value
        } else {
            throw RuntimeException(ProtocolException("Unexpected line: $line"))
        }
        ++count
        return tag
    }

    override fun remove() {
        TODO("Not yet implemented")
    }
}