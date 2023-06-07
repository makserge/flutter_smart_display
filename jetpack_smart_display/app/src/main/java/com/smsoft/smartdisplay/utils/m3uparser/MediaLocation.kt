package com.smsoft.smartdisplay.utils.m3uparser

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.nio.file.FileSystemNotFoundException
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths

/**
 * The location of a media file referenced in a `.m3u` file.
 *
 * This is a sealed class with exactly two implementations:
 *
 * - [MediaPath]
 * - [MediaUrl]
 */
sealed class MediaLocation {
    /**
     * The URL pointing to the location.
     */
    abstract val url: URL

    override fun toString(): String {
        return url.toExternalForm()
    }

    companion object {
        private const val TAG = "M3uParser"
        private const val FILE_PROTOCOL = "file"

        /**
         * Creates a MediaLocation instance based on the given location.
         *
         * @param location the location from the m3u file
         * @param dir the base dir to resolve relative paths with
         * @return a MediaLocation instance, either [MediaPath] or [MediaUrl]
         * @throws IllegalArgumentException If the location does not have a valid format
         */
        @RequiresApi(Build.VERSION_CODES.O)
        operator fun invoke(location: String, dir: Path? = null): MediaLocation {
            return tryParseFileUrl(location)?.let { MediaPath(it) }
                ?: tryParseUrl(location)?.let { MediaUrl(it) }
                ?: tryParsePath(location, dir)?.let { MediaPath(it) }
                ?: throw IllegalArgumentException("Could not parse as URL or path: $location")
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun tryParsePath(location: String, dir: Path?): Path? {
            return try {
                if (dir == null) {
                    Paths.get(location)
                } else {
                    dir.resolve(location)
                }
            } catch (e: InvalidPathException) {
                Log.d(TAG, "Tried to parse an invalid path")
                null
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun tryParseFileUrl(location: String): Path? {
            return try {
                val url = tryParseUrl(location) ?: return null
                if (url.protocol == FILE_PROTOCOL) {
                    Paths.get(url.toURI())
                } else {
                    null
                }
            } catch (e: URISyntaxException) {
                Log.d(TAG, "Could not convert URL for $location to a URI")
                null
            } catch (e: FileSystemNotFoundException) {
                Log.d(TAG, "File system specified by $location couldn't be found")
                null
            } catch (e: IllegalArgumentException) {
                Log.d(TAG, "Could not get Path for $location")
                null
            }
        }

        private fun tryParseUrl(location: String): URL? {
            return try {
                URL(location)
            } catch (e: MalformedURLException) {
                Log.d(TAG,"Could not parse as URL: $location")
                null
            }
        }
    }
}

/**
 * A local media file location.
 *
 * Important: the underlying path may also refer to a directory.
 *
 * @param path the local file path
 */
@RequiresApi(Build.VERSION_CODES.O)
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

/**
 * A remote media file location.
 *
 * The [URL][url] may also point to a local file if it isn't using the "`file`" protocol.
 */
class MediaUrl internal constructor(override val url: URL) : MediaLocation()