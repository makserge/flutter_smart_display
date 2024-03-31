package com.smsoft.smartdisplay.di

import com.smsoft.smartdisplay.service.sensor.SensorHandler
import com.smsoft.smartdisplay.service.sensor.SensorServiceHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SensorModule {

    @Provides
    @Singleton
    fun provideSensorHandler(): SensorHandler = SensorServiceHandler()
}