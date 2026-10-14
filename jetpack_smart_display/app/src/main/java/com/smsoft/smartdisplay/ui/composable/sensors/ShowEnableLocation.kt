package com.smsoft.smartdisplay.ui.composable.sensors

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.ui.common.QuestionScreen

/**
 * False when Location is off on Android 11 or older, which then drops every BLE scan result without
 * an error: on the Lenovo panel (Android 11) the scan found nothing. Android 8.1 does the same (its
 * Bluetooth service has a strict location check), so the 4 inch panel needs it too. Android 12 and
 * later scan without Location, since the app declares BLUETOOTH_SCAN as never used for location.
 */
fun isLocationOnForBleScan(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return true
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return true
        return locationManager.isLocationEnabled
    }
    // Before Android 9 the location mode is the only way to tell
    @Suppress("DEPRECATION")
    return Settings.Secure.getInt(
        context.contentResolver,
        Settings.Secure.LOCATION_MODE,
        Settings.Secure.LOCATION_MODE_OFF
    ) != Settings.Secure.LOCATION_MODE_OFF
}

/**
 * Asks to turn Location on for the BLE scan and opens Android's Location settings. [onResult] gets
 * whether Location is on when the user comes back, or false when the user cancels.
 */
@Composable
fun ShowEnableLocation(
    onResult: (isOn: Boolean) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        onResult(isLocationOnForBleScan(context))
    }
    QuestionScreen(
        title = stringResource(R.string.location_for_ble_title),
        message = stringResource(R.string.location_for_ble),
        confirmText = stringResource(R.string.location_for_ble_confirm),
        onConfirm = {
            try {
                launcher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } catch (e: ActivityNotFoundException) {
                onResult(false)
            }
        },
        onDismissRequest = {
            onResult(false)
        }
    )
}
