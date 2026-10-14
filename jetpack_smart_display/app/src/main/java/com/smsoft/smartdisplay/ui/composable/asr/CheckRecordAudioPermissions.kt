package com.smsoft.smartdisplay.ui.composable.asr

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.ui.common.QuestionScreen

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckRecordAudioPermission(
    modifier: Modifier,
    onGranted: () -> Unit,
    onCancel: () -> Unit
) {
    var showDialog by rememberSaveable { mutableStateOf(true) }

    val permissionState = rememberPermissionState(
        android.Manifest.permission.RECORD_AUDIO
    )

    if (permissionState.status.isGranted) {
        onGranted()
    } else {
        if (showDialog) {
            QuestionScreen(
                title = stringResource(R.string.permission_title),
                message = if (permissionState.status.shouldShowRationale) {
                    stringResource(R.string.asr_permission_rationale)
                } else {
                    stringResource(R.string.asr_permission)
                },
                confirmText = stringResource(R.string.asr_permission_confirm),
                onConfirm = {
                    showDialog = false
                    permissionState.launchPermissionRequest()
                },
                onDismissRequest = {
                    showDialog = false
                    onCancel()
                }
            )
        }
    }
}