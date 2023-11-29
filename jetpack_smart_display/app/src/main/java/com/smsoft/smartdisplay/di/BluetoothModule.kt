package com.smsoft.smartdisplay.di

import android.content.Context
import com.smsoft.smartdisplay.service.ble.BleBluetoothHandler
import com.smsoft.smartdisplay.service.ble.BluetoothHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class BluetoothModule {

    @Provides
    @Singleton
    fun provideBluetoothHandler(
        @ApplicationContext context: Context
    ): BluetoothHandler = BleBluetoothHandler(context)
}