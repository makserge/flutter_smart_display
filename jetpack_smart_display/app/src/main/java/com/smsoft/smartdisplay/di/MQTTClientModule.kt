package com.smsoft.smartdisplay.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.smsoft.smartdisplay.service.mqtt.MqttCallbackDispatcher
import com.smsoft.smartdisplay.utils.getMQTTHostCredentials
import com.smsoft.smartdisplay.utils.getMQTTClientId
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
        dataStore: DataStore<Preferences>,
        mqttCallbackDispatcher: MqttCallbackDispatcher
    ): MqttAndroidClient {
        return MqttAndroidClient(
            context = context,
            serverURI = getMQTTHostCredentials(dataStore),
            clientId = getMQTTClientId(context)
        ).apply {
            addCallback(mqttCallbackDispatcher)
        }
    }
}