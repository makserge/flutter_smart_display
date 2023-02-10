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
import de.dixieflatline.mpcw.client.ConnectionPool.instance
import java.net.URI

object Connection {
    @RequiresApi(Build.VERSION_CODES.N)
    @Throws(Exception::class)
    fun create(connectionString: String?): IConnection? {
        val uri = URI(connectionString)
        return instance?.create(uri)
    }
}