package com.smsoft.smartdisplay.utils.m3uparser

/**
 * Additional key-value data for an M3U entry. Basically just a wrapper around a regular Map with
 * some convenience accessors for commonly-used keys.
 */
class M3uMetadata(private val data: Map<String, String>) : Map<String, String> by data {
    companion object {
        /**
         * Obtain an empty instance of M3uMetadata.
         */
        fun empty() = M3uMetadata(emptyMap())
    }
}