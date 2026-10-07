package com.smsoft.smartdisplay.di

import android.app.AlarmManager
import android.app.Application
import android.app.Service
import androidx.annotation.OptIn
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.data.database.repository.AlarmRepository
import com.smsoft.smartdisplay.service.alarm.AlarmHandler
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AlarmModule {

    @Singleton
    @Provides
    fun providesCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideAlarmManager(app: Application): AlarmManager =
        app.getSystemService(Service.ALARM_SERVICE) as AlarmManager

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun providesAlarmHandler(
        app: Application,
        alarmManager: AlarmManager,
        alarmRepository: AlarmRepository,
        dataStore: DataStore<Preferences>,
        // Lazy: AlarmHandler is created when the app starts, and creating the client reads the
        // broker settings blocking.
        mqttClient: Lazy<MqttAndroidClient>,
        coroutineScope: CoroutineScope
    ): AlarmHandler = AlarmHandler(app, alarmManager, alarmRepository, dataStore, mqttClient, coroutineScope)
}