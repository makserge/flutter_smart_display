package com.smsoft.smartdisplay.utils.m3uparser

import android.util.Log
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
        private const val FILE_PROTOCOL = "file"

        /**
         * Creates a MediaLocation instance based on the given location.
         *
         * @param location the location from the m3u file
         * @param dir the base dir to resolve relative paths with
         * @return a MediaLocation instance, either [MediaPath] or [MediaUrl]
         * @throws IllegalArgumentException If the location does not have a valid format
         */
        operator fun invoke(location: String, dir: Path? = null): MediaLocation {
            return tryParseFileUrl(location)?.let { MediaPath(it) }
                ?: tryParseUrl(location)?.let { MediaUrl(it) }
                ?: tryParsePath(location, dir)?.let { MediaPath(it) }
                ?: throw IllegalArgumentException("Could not parse as URL or path: $location")
        }

        private fun tryParsePath(location: String, dir: Path?): Path? {
            return try {
                if (dir == null) Paths.get(location)
                else dir.resolve(location)
            } catch (e: InvalidPathException) {
                Log.d("MediaLocation", "Tried to parse an invalid path")
                null
            }
        }

        private fun tryParseFileUrl(location: String): Path? {
            return try {
                val url = tryParseUrl(location) ?: return null
                if (url.protocol == FILE_PROTOCOL) Paths.get(url.toURI())
                else null
            } catch (e: URISyntaxException) {
                Log.d("MediaLocation", "Could not convert URL for $location to a URI" )
                null
            } catch (e: FileSystemNotFoundException) {
                Log.d("MediaLocation", "File system specified by $location couldn't be found")
                null
            } catch (e: IllegalArgumentException) {
                Log.d("MediaLocation", "Could not get Path for $location")
                null
            }
        }

        private fun tryParseUrl(location: String): URL? {
            return try {
                URL(location)
            } catch (e: MalformedURLException) {
                Log.d("MediaLocation", "Could not parse as URL: $location")
                null
            }
        }
    }
}