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
import com.smsoft.smartdisplay.data.BluetoothDevice
import com.smsoft.smartdisplay.data.BluetoothDeviceType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

class BleBluetoothHandler @Inject constructor(
    @ApplicationContext private val context: Context): BluetoothHandler
{
    override var scanState: Flow<BluetoothScanState>

    private val deviceMap: MutableMap<String, BluetoothDevice> = mutableMapOf()

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .build()

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var scanCallback: ScanCallback

    init {
        scanState = callbackFlow {
            scanCallback = object : ScanCallback() {
                @SuppressLint("MissingPermission")
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)

                    result?.let {
                        coroutineScope.launch {
                            val deviceName = if (!it.device.name.isNullOrEmpty()) it.device.name else ""
                            if (BluetoothDeviceType.toList().any {
                                deviceName.startsWith(it)
                            }) {
                                val device = BluetoothDevice(
                                    deviceName = deviceName,
                                    address = it.device.address,
                                    rssi = it.rssi,
                                    bytes = result.scanRecord?.bytes
                                )
                                deviceMap[it.device.address] = device
                                trySend(BluetoothScanState.Result(
                                    devices = deviceMap.values.toList(),
                                    time = System.currentTimeMillis())
                                )
                            }
                        }
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    when (errorCode) {
                        SCAN_FAILED_ALREADY_STARTED -> {
                            stopScan()
                            startScan()
                        }
                        SCAN_FAILED_FEATURE_UNSUPPORTED -> {
                            stopScan()
                            trySend(BluetoothScanState.Result(
                                devices = emptyList(),
                                time = System.currentTimeMillis()
                            ))
                        }
                        else -> {
                            trySend(BluetoothScanState.Error(
                                message = errorCode.toString()
                            ))
                        }
                    }
                }
            }
            awaitClose {
            }
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)
    }

    override fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    @SuppressLint("MissingPermission")
    override fun startScan() {
        deviceMap.clear()

        bluetoothAdapter.bluetoothLeScanner.startScan(null, scanSettings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    override fun stopScan() {
        try {
            if (bluetoothAdapter.isEnabled) {
                bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
            }
        } catch (ignored: Exception) {
        }
    }
}

sealed class BluetoothScanState {
    data object Initial: BluetoothScanState()
    data class Result(val devices: List<BluetoothDevice>, val time: Long) : BluetoothScanState()
    data class Error(val message: String) : BluetoothScanState()
}

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