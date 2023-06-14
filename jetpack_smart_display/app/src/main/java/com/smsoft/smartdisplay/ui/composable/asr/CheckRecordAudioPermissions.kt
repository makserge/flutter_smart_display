package com.smsoft.smartdisplay.ui.composable.asr

import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.smsoft.smartdisplay.R

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
            AlertDialog(
                modifier = modifier,
                onDismissRequest = {
                    showDialog = false
                },
                text = {
                    Column(
                        modifier = modifier,
                    ) {
                        val textToShow = if (permissionState.status.shouldShowRationale) {
                            stringResource(R.string.asr_permission_rationale)
                        } else {
                            stringResource(R.string.asr_permission)
                        }
                        Text(
                            modifier = modifier,
                            text = textToShow
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        modifier = modifier,
                        onClick = {
                            showDialog = false
                            permissionState.launchPermissionRequest()
                        },
                    ) {
                        Text(
                            modifier = modifier,
                            text = stringResource(R.string.asr_permission_confirm)
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        modifier = modifier,
                        onClick = {
                            showDialog = false
                            onCancel()
                        },
                    ) {
                        Text(
                            modifier = modifier,
                            text = stringResource(R.string.cancel)
                        )
                    }
                },
                properties = DialogProperties(
                    usePlatformDefaultWidth = true
                )
            )
        }
    }
}