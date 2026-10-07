package com.smsoft.smartdisplay.service.sensor

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.composable.settings.LIGHT_SENSOR_ENABLED_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.LIGHT_SENSOR_INTERVAL_DEFAULT
import com.smsoft.smartdisplay.utils.getForegroundNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class SensorService : Service() {
    @Inject
    lateinit var sensorHandler: SensorHandler

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    private val STICKY_NOTIFICATION_ID = 77

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    // Follow Settings: the light sensor used to be switched on or off (and its interval set) only
    // when the service started, i.e. at app start.
    private val isLightSensorEnabled = MutableStateFlow(LIGHT_SENSOR_ENABLED_DEFAULT)
    @Volatile
    private var lightSensorIntervalSec = LIGHT_SENSOR_INTERVAL_DEFAULT.toInt()
    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private var proximitySensor: Sensor? = null
    private var lightSensorListener: SensorEventListener? = null
    private var proximitySensorListener: SensorEventListener? = null
    private var lastLightSensorTimestamp = System.currentTimeMillis()

    override fun onCreate() {
        super.onCreate()
        startForeground()

        serviceScope.launch {
            var isInitialized = false
            dataStore.data.map(::readLightSensorSettings).distinctUntilChanged().collect { (isEnabled, intervalSec) ->
                lightSensorIntervalSec = intervalSec
                isLightSensorEnabled.value = isEnabled
                if (!isInitialized) {
                    isInitialized = true
                    initSensors()
                }
            }
        }
    }

    private fun initSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
        // Always created when the device has a light sensor; the listener is registered only
        // while the setting is on.
        sensorHandler.lightSensorState = lightSensor?.let { sensor ->
            channelFlow {
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        if ((event != null)
                            && (event.sensor.type == Sensor.TYPE_LIGHT)
                            && (System.currentTimeMillis() > (lastLightSensorTimestamp + (lightSensorIntervalSec * 1000L)))) {
                            lastLightSensorTimestamp = System.currentTimeMillis()
                            trySend(event.values[0].toInt())
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    }
                }
                lightSensorListener = listener
                send(0)
                try {
                    isLightSensorEnabled.collect { isEnabled ->
                        if (isEnabled) {
                            sensorManager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                        } else {
                            sensorManager?.unregisterListener(listener)
                            // No reading while off; consumers must not keep the last one.
                            send(0)
                        }
                    }
                } finally {
                    sensorManager?.unregisterListener(listener)
                }
            }
                .distinctUntilChanged()
                .flowOn(Dispatchers.IO)
        }
        sensorHandler.proximitySensorState = callbackFlow {
            proximitySensorListener = object: SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if ((event != null) && (event.sensor.type == Sensor.TYPE_PROXIMITY)) {
                        trySend(event.values[0].toInt())
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                }
            }
            sensorManager!!.registerListener(proximitySensorListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)

            trySend(0)
            awaitClose {
            }
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)
        sensorHandler.isServiceStarted.value = true
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        serviceScope.cancel()
        sensorManager?.unregisterListener(lightSensorListener)
        sensorManager?.unregisterListener(proximitySensorListener)
        super.onDestroy()
    }

    /**
     * Foreground keeps the sensors reporting while the screen is off. If Android refuses it
     * (missing prerequisite permission, app not in the foreground), the service keeps running as
     * a normal service instead of crashing the app; sensors then report while the app is shown.
     */
    private fun startForeground() {
        val notification = getForegroundNotification(
            context = this
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                startForeground(
                    STICKY_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            } else {
                startForeground(
                    STICKY_NOTIFICATION_ID,
                    notification
                )
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Sensor service runs without foreground state", e)
        } catch (e: IllegalStateException) {
            // ForegroundServiceStartNotAllowedException (Android 12+) is an IllegalStateException.
            Log.w(TAG, "Sensor service runs without foreground state", e)
        }
    }
}

private const val TAG = "SensorService"

/**
 * The light sensor switch and its reporting interval in seconds. The interval is free text: an
 * invalid value used to crash the app at every start (toInt() in an unguarded coroutine); it now
 * falls back to the default.
 */
private fun readLightSensorSettings(data: Preferences): Pair<Boolean, Int> = Pair(
    data[booleanPreferencesKey(PreferenceKey.LIGHT_SENSOR_ENABLED.key)] ?: LIGHT_SENSOR_ENABLED_DEFAULT,
    data[stringPreferencesKey(PreferenceKey.LIGHT_SENSOR_INTERVAL.key)]?.trim()?.toIntOrNull()?.takeIf { it >= 0 }
        ?: LIGHT_SENSOR_INTERVAL_DEFAULT.toInt()
)
