package com.smsoft.smartdisplay.utils

import android.content.Context
import android.database.ContentObserver
import android.provider.Settings
import android.text.format.DateFormat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Whether the time is shown in 24-hour format: the system setting "Use 24-hour format", or the
 * language's default when it is "Use locale default". Emits the current value on collection and
 * again whenever a write of the setting changes it - by the Settings app, adb or a device policy;
 * unlike ACTION_TIME_CHANGED this also sees writes that send no broadcast. A language change
 * writes no setting, so only a new collection reads the new locale default.
 */
fun Context.is24HourFormatFlow(): Flow<Boolean> {
    val context = applicationContext
    return callbackFlow {
        // No handler: onChange runs on a binder thread, which trySend is fine with
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                trySend(DateFormat.is24HourFormat(context))
            }
        }
        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.TIME_12_24),
            false,
            observer
        )
        // Read after registering, so a change in between is not missed
        trySend(DateFormat.is24HourFormat(context))
        awaitClose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }.distinctUntilChanged()
}
