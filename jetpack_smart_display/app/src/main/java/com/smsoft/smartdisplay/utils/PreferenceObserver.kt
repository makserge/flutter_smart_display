package com.smsoft.smartdisplay.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Follows a value derived from the settings for as long as [scope] lives: [onValue] runs with the
 * current value and again whenever a change in Settings changes it. Settings used to be read once,
 * so a change only took effect after the app was restarted, which a wall panel without buttons
 * hardly ever is.
 */
fun <T> DataStore<Preferences>.observe(
    scope: CoroutineScope,
    read: (Preferences) -> T,
    onValue: suspend (T) -> Unit
): Job = scope.launch {
    data.map(read).distinctUntilChanged().collect { onValue(it) }
}
