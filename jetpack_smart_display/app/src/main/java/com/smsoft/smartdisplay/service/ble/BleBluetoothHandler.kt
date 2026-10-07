package com.smsoft.smartdisplay.service.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.smsoft.smartdisplay.data.BluetoothDevice
import com.smsoft.smartdisplay.data.BluetoothDeviceType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

private const val TAG = "BleBluetoothHandler"

/**
 * BLE advertisement scanner for the supported sensor beacons.
 *
 * One scan callback lives as long as this singleton, and results go into a hot StateFlow, so they
 * keep arriving no matter when or how often the flow is collected. Android turns a scan that runs
 * longer than 30 minutes into an "opportunistic" one that no longer delivers results, so a running
 * scan is restarted every [SCAN_RESTART_INTERVAL_MS]. Start/stop must be called on the main thread.
 */
class BleBluetoothHandler @Inject constructor(
    @ApplicationContext private val context: Context
) : BluetoothHandler {
    private val scanStateInt = MutableStateFlow<BluetoothScanState>(BluetoothScanState.Initial)
    override val scanState: StateFlow<BluetoothScanState> = scanStateInt.asStateFlow()

    private val deviceMap = ConcurrentHashMap<String, BluetoothDevice>()

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .build()

    private val mainHandler = Handler(Looper.getMainLooper())
    private val restartRunnable = Runnable { restartRunningScan() }
    private var isScanning = false

    // Scan callbacks are delivered on the main thread.
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let { onDeviceFound(it) }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { onDeviceFound(it) }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.w(TAG, "BLE scan failed: $errorCode")
            when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> {}
                SCAN_FAILED_FEATURE_UNSUPPORTED -> {
                    stopScan()
                    scanStateInt.value = BluetoothScanState.Result(
                        devices = emptyList(),
                        time = System.currentTimeMillis()
                    )
                }
                else -> {
                    isScanning = false
                    scanStateInt.value = BluetoothScanState.Error(
                        message = errorCode.toString()
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun onDeviceFound(result: ScanResult) {
        val deviceName = try {
            result.device.name
        } catch (e: SecurityException) {
            null
        }.takeUnless { it.isNullOrEmpty() } ?: result.scanRecord?.deviceName ?: ""
        if (BluetoothDeviceType.toList().none { deviceName.startsWith(it) }) {
            return
        }
        deviceMap[result.device.address] = BluetoothDevice(
            deviceName = deviceName,
            address = result.device.address,
            rssi = result.rssi,
            bytes = result.scanRecord?.bytes
        )
        scanStateInt.value = BluetoothScanState.Result(
            devices = deviceMap.values.toList(),
            time = System.currentTimeMillis()
        )
    }

    override fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    override fun startScan() {
        if (isScanning) {
            // Starting again would only fail with SCAN_FAILED_ALREADY_STARTED, and frequent
            // restarts make Android throttle the app (at most 5 scan starts per 30 seconds).
            return
        }
        deviceMap.clear()
        startPlatformScan()
    }

    override fun rescan() {
        stopPlatformScan()
        deviceMap.clear()
        startPlatformScan()
    }

    override fun stopScan() {
        stopPlatformScan()
    }

    private fun restartRunningScan() {
        if (!isScanning) {
            return
        }
        stopPlatformScan()
        startPlatformScan()
    }

    @SuppressLint("MissingPermission")
    private fun startPlatformScan() {
        val scanner = bluetoothAdapter?.takeIf { it.isEnabled }?.bluetoothLeScanner ?: return
        try {
            scanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
            mainHandler.removeCallbacks(restartRunnable)
            mainHandler.postDelayed(restartRunnable, SCAN_RESTART_INTERVAL_MS)
        } catch (e: SecurityException) {
            Log.w(TAG, "BLE scan not permitted", e)
            scanStateInt.value = BluetoothScanState.Error(message = e.message.toString())
        } catch (e: IllegalStateException) {
            // Bluetooth was switched off in the meantime.
            Log.w(TAG, "BLE scan not started", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopPlatformScan() {
        mainHandler.removeCallbacks(restartRunnable)
        if (!isScanning) {
            return
        }
        isScanning = false
        try {
            bluetoothAdapter?.takeIf { it.isEnabled }?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (ignored: Exception) {
        }
    }
}

sealed class BluetoothScanState {
    data object Initial: BluetoothScanState()
    data class Result(val devices: List<BluetoothDevice>, val time: Long) : BluetoothScanState()
    data class Error(val message: String) : BluetoothScanState()
}

// Restart well before Android's 30-minute limit, and rarely enough to stay far below its
// limit of 5 scan starts per 30 seconds.
private const val SCAN_RESTART_INTERVAL_MS = 10L * 60 * 1000

val blePermissionsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    listOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )
} else {
    listOf(
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}
