package com.smsoft.smartdisplay.di

import android.content.Context
import androidx.room.Room
import com.smsoft.smartdisplay.data.database.SmartDisplayDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val DATABASE = "smartdisplay_database"

    @Singleton
    @Provides
    fun providesRoomDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        SmartDisplayDatabase::class.java,
        DATABASE)
        .build()

    @Provides
    fun providesSensorDao(database: SmartDisplayDatabase) = database.sensorDao()

    @Provides
    fun providesWeatherDao(database: SmartDisplayDatabase) = database.weatherCurrentDao()
}