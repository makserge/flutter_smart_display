package com.smsoft.smartdisplay

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.smsoft.smartdisplay.utils.initMQTT
import dagger.hilt.android.HiltAndroidApp
import info.mqtt.android.service.MqttAndroidClient
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.MediaPlayer
import javax.inject.Inject

@HiltAndroidApp
class SmartDisplayApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var mqttClient: MqttAndroidClient

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    @Inject
    lateinit var libVlc: LibVLC

    @Inject
    lateinit var mediaPlayer: MediaPlayer

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        initMQTT(
            context = this,
            mqttClient = mqttClient,
            dataStore = dataStore
        )
    }

    override fun getWorkManagerConfiguration() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()
}