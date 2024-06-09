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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.utils.getForegroundNotification
import com.smsoft.smartdisplay.utils.getLightSensorSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
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

    private lateinit var settings: Pair<Boolean, Int>
    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private var proximitySensor: Sensor? = null
    private var lightSensorListener: SensorEventListener? = null
    private var proximitySensorListener: SensorEventListener? = null
    private var lastLightSensorTimestamp = System.currentTimeMillis()

    override fun onCreate() {
        super.onCreate()
        startForeground()

        CoroutineScope(Dispatchers.IO).launch {
            settings = getLightSensorSettings(dataStore)
            initSensors()
        }
    }

    private fun initSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        if (settings.first) {
            lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
            sensorHandler.lightSensorState = callbackFlow {
                lightSensorListener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        if ((event != null)
                            && (event.sensor.type == Sensor.TYPE_LIGHT)
                            && (System.currentTimeMillis() > (lastLightSensorTimestamp + (settings.second * 1000)))) {
                            lastLightSensorTimestamp = System.currentTimeMillis()
                            trySend(event.values[0].toInt())
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    }
                }
                sensorManager!!.registerListener(
                    lightSensorListener,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )

                trySend(0)
                awaitClose {
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
        sensorManager?.unregisterListener(lightSensorListener)
        sensorManager?.unregisterListener(proximitySensorListener)
        super.onDestroy()
    }

    private fun startForeground() {
        val notification = getForegroundNotification(
            context = this
        )
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
    }
}