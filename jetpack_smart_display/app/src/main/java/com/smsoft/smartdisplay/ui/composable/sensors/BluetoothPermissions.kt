package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.smsoft.smartdisplay.service.ble.blePermissionsList

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BluetoothPermissions(
    modifier: Modifier,
    isEnabled: Boolean,
    onAllowed: () -> Unit,
    onDenied: () -> Unit
) {
    val context = LocalContext.current
    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = blePermissionsList
    )
    // The permissions, then Bluetooth, then Location (see isLocationOnForBleScan), each asked once
    var isBluetoothTurnedOn by remember { mutableStateOf(false) }
    var isLocationTurnedOn by remember { mutableStateOf(false) }
    var isLocationDeclined by remember { mutableStateOf(false) }
    when {
        !multiplePermissionsState.allPermissionsGranted -> {
            ShowBluetoothPermissions(
                modifier = Modifier,
                multiplePermissionsState = multiplePermissionsState,
                onCancel = {
                    onDenied()
                }
            )
        }
        !isEnabled && !isBluetoothTurnedOn -> {
            ShowEnableBluetooth(
                modifier = Modifier
            ) {
                if (it) {
                    isBluetoothTurnedOn = true
                } else {
                    onDenied()
                }
            }
        }
        !isLocationTurnedOn && !isLocationOnForBleScan(context) -> {
            if (!isLocationDeclined) {
                ShowEnableLocation { isOn ->
                    if (isOn) {
                        isLocationTurnedOn = true
                    } else {
                        isLocationDeclined = true
                        onDenied()
                    }
                }
            }
        }
        else -> {
            // A side effect, not a call during composition (that ran on every recomposition).
            LaunchedEffect(Unit) {
                onAllowed()
            }
        }
    }
}