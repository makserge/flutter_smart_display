package com.smsoft.smartdisplay.di

import android.content.Context
import androidx.room.Room
import com.smsoft.smartdisplay.data.database.SensorDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private val SENSOR_DATABASE = "sensor_database"

    @Singleton
    @Provides
    fun providesRoomDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        SensorDatabase::class.java,
        SENSOR_DATABASE)
        .build()

    @Provides
    fun providesSensorDao(database: SensorDatabase) = database.sensorDao()
}