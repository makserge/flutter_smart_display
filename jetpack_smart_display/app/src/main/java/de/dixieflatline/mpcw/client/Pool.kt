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

import android.os.Build
import androidx.annotation.RequiresApi
import java.net.URI
import java.util.*

class Pool<T : IURI?>(private val factory: IFactory<T>) {
    private val pool = HashMap<URI, Queue<T>>()
    private var autocleanCounter = 0
    @RequiresApi(Build.VERSION_CODES.N)
    @Synchronized
    @Throws(Exception::class)
    fun create(uri: URI): T? {
        autoclean()
        var obj = tryRecycle(uri)
        if (obj == null) {
            obj = factory.create(uri)
        }
        return obj
    }

    @Synchronized
    @Throws(Exception::class)
    fun dispose(obj: T) {
        if (factory.valid(obj)) {
            //val uri = obj!!.uri
            //if (!pool.containsKey(uri)) {
            //    pool[uri] = LinkedList()
            //}
            //factory.idle(obj)
            //pool[uri]!!.add(obj)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun autoclean() {
        ++autocleanCounter
        if (autocleanCounter == AUTOCLEAN_INTERVAL) {
            removeInvalidResources()
            autocleanCounter = 0
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun removeInvalidResources() {
        val obsoleteQueues: MutableList<URI> = ArrayList()
        for (uri in pool.keys) {
            val validResourcesLeft = popInvalidResources(uri)
            if (validResourcesLeft == 0) {
                obsoleteQueues.add(uri)
            }
        }
        for (uri in obsoleteQueues) {
            pool.remove(uri)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun popInvalidResources(uri: URI): Int {
        val currentQueue = pool[uri]!!
        val newQueue = LinkedList<T>()
        while (!currentQueue.isEmpty()) {
            val resource = currentQueue.remove()
            if (factory.valid(resource)) {
                newQueue.addFirst(resource)
            }
        }
        pool.replace(uri, newQueue)
        return newQueue.size
    }

    @Throws(Exception::class)
    private fun tryRecycle(uri: URI): T? {
        var obj: T? = null
        if (pool.containsKey(uri)) {
            val resources = pool[uri]!!
            while (!resources.isEmpty() && obj == null) {
                val resource = resources.remove()
                if (factory.valid(resource)) {
                    obj = factory.recycle(resource)
                }
            }
        }
        return obj
    }

    companion object {
        private const val AUTOCLEAN_INTERVAL = 15
    }
}