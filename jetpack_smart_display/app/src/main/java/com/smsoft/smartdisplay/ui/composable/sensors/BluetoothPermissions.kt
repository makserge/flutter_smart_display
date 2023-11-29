package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = blePermissionsList
    )
    if (multiplePermissionsState.allPermissionsGranted) {
        if (isEnabled) {
            onAllowed()
        } else {
            ShowEnableBluetooth(
                modifier = Modifier
            ) {
                if (it) {
                    onAllowed()
                } else {
                    onDenied()
                }
            }
        }
    } else {
        ShowBluetoothPermissions(
            modifier = Modifier,
            multiplePermissionsState = multiplePermissionsState,
            onCancel = {
                onDenied()
            }
        )
    }
}