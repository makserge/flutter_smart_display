package com.smsoft.smartdisplay.utils.m3uparser

import java.net.URL

/**
 * A remote media file location.
 *
 * The [URL][url] may also point to a local file if it isn't using the "`file`" protocol.
 */
class MediaUrl internal constructor(override val url: URL) : MediaLocation()