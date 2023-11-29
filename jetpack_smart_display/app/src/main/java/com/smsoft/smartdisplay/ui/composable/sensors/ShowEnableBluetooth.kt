package com.smsoft.smartdisplay.ui.composable.sensors

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier

@Composable
fun ShowEnableBluetooth(
    modifier: Modifier.Companion,
    onResult: (result: Boolean) -> Unit
) {
    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        onResult(result.resultCode == Activity.RESULT_OK)
    }
    SideEffect {
        launcher.launch(intent)
    }
}