package com.smsoft.smartdisplay.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.smsoft.smartdisplay.ui.screen.sensors.MQTT_CLIENT_ID
import com.smsoft.smartdisplay.utils.getMQTTHostCredentials
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import info.mqtt.android.service.MqttAndroidClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MQTTClientModule {

    @Provides
    @Singleton
    fun provideMQTTClient(
        @ApplicationContext context: Context,
        dataStore: DataStore<Preferences>
    ): MqttAndroidClient {
        return MqttAndroidClient(
            context = context,
            serverURI = getMQTTHostCredentials(dataStore),
            clientId = MQTT_CLIENT_ID
        )
    }
}