package com.smsoft.smartdisplay.utils.m3uparser

import java.net.URL
import java.nio.file.Path

/**
 * A local media file location.
 *
 * Important: the underlying path may also refer to a directory.
 *
 * @param path the local file path
 */
class MediaPath internal constructor(val path: Path) : MediaLocation() {
    override val url: URL by lazy { path.toUri().toURL() }

    /**
     * Whether this path points to another `.m3u` file. If so, it can be passed into the parser
     * again.
     *
     * Please note that this only detects a playlist file if its name ends with the .m3u
     * file name extension.
     */
    val isPlaylistPath: Boolean
        get() = path.fileName.toString().endsWith(m3uExtension)

    override fun toString(): String {
        return path.toString()
    }

    private companion object {
        const val m3uExtension = ".m3u"
    }
}