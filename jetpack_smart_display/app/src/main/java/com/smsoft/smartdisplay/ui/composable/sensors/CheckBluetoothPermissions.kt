package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.ui.common.QuestionScreen

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ShowBluetoothPermissions(
    modifier: Modifier,
    multiplePermissionsState: MultiplePermissionsState,
    onCancel: () -> Unit
) {
    var showDialog by rememberSaveable { mutableStateOf(true) }

    if (!showDialog) {
        return
    }

    QuestionScreen(
        title = stringResource(R.string.permission_title),
        message = if (multiplePermissionsState.shouldShowRationale) {
            stringResource(R.string.bluetooth_permission_rationale)
        } else {
            stringResource(R.string.bluetooth_permission)
        },
        confirmText = stringResource(R.string.asr_permission_confirm),
        onConfirm = {
            showDialog = false
            multiplePermissionsState.launchMultiplePermissionRequest()
        },
        onDismissRequest = {
            showDialog = false
            onCancel()
        }
    )
}