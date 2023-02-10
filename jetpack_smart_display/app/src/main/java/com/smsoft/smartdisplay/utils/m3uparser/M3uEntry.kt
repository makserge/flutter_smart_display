package com.smsoft.smartdisplay.utils.m3uparser

import java.time.Duration

/**
 * An entry in a `.m3u` file.
 *
 * @param location the location of the file
 * @param duration the media item's duration, or null
 * @param title the media item's title, or null
 * @param metadata any key-value metadata
 */
data class M3uEntry constructor(
    val location: MediaLocation,
    val duration: Duration? = null,
    val title: String? = null,
    val metadata: M3uMetadata = M3uMetadata.empty(),
)
