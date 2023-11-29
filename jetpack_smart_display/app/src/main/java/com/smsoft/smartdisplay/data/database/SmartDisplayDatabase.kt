package com.smsoft.smartdisplay.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.smsoft.smartdisplay.data.database.dao.SensorDao
import com.smsoft.smartdisplay.data.database.dao.WeatherCurrentDao
import com.smsoft.smartdisplay.data.database.dao.WeatherForecastDao
import com.smsoft.smartdisplay.data.database.entity.Sensor
import com.smsoft.smartdisplay.data.database.entity.WeatherCurrent
import com.smsoft.smartdisplay.data.database.entity.WeatherForecast

@Database(
    version = 2,
    entities = [
        Sensor::class,
        WeatherCurrent::class,
        WeatherForecast::class
    ],
    autoMigrations = [
        AutoMigration (
            from = 1,
            to = 2
        )
    ],
    exportSchema = true
)

abstract class SmartDisplayDatabase: RoomDatabase() {
    abstract fun sensorDao(): SensorDao
    abstract fun weatherCurrentDao(): WeatherCurrentDao
    abstract fun weatherForecastDao(): WeatherForecastDao
}