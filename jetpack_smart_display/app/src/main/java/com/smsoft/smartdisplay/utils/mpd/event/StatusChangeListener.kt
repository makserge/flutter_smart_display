package com.smsoft.smartdisplay.utils.mpd.event

import com.smsoft.smartdisplay.utils.mpd.data.MPDState
import com.smsoft.smartdisplay.utils.mpd.data.MPDStatus

interface StatusChangeListener {
    fun connectionStateChanged(isConnected: Boolean)
    fun libraryStateChanged(isUpdating: Boolean) {}
    fun playlistChanged(newStatus: MPDStatus, playlistVersion: Int)
    fun randomChanged(isRandom: Boolean) {}
    fun repeatChanged(isRepeating: Boolean) {}
    fun stateChanged(newStatus: MPDStatus, state: MPDState) {}
    fun trackChanged(newStatus: MPDStatus, track: Int)
    fun trackPositionChanged(newStatus: MPDStatus)
    fun volumeChanged(newStatus: MPDStatus, volume: Int) {}
}
