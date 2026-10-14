package com.smsoft.smartdisplay.utils

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

/*
 * The way out of the app. It is the panel's home app and runs in full screen, so there is no home
 * button that leads anywhere else; Settings has "Open home screen" for Android's settings and the
 * other home apps.
 */

private const val TAG = "HomeApps"

/** The home apps other than this one (the panel's own app, a launcher), with their labels. */
fun otherHomeApps(context: Context): List<Pair<ComponentName, String>> {
    val packageManager = context.packageManager
    val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    return packageManager.queryIntentActivities(home, 0)
        .map { it.activityInfo }
        .filter { info ->
            // Settings' "FallbackHome" only bridges the start of the device and shows nothing
            (info.packageName != context.packageName) && !info.name.endsWith("FallbackHome")
        }
        .map { info ->
            ComponentName(info.packageName, info.name) to info.loadLabel(packageManager).toString()
        }
        .also { Log.i(TAG, "Other home apps: ${it.joinToString { (component, label) -> "$label ($component)" }}") }
}

/** Opens the home screen of [home]; this app stays running behind it. */
fun openHomeApp(context: Context, home: ComponentName) {
    val intent = Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_HOME)
        .setComponent(home)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.w(TAG, "Home app $home not started", e)
    }
}

/**
 * Opens Android's own settings, the way into the Android system on a panel whose only other home
 * app is the panel maker's (on the 4 inch Sonoff panel "Open home screen" led back into Sonoff's app).
 */
fun openAndroidSettings(context: Context) {
    try {
        context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        Log.w(TAG, "Android settings not available", e)
    }
}
