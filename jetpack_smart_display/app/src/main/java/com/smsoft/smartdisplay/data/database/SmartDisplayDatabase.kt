package com.smsoft.smartdisplay.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smsoft.smartdisplay.data.database.dao.SensorDao
import com.smsoft.smartdisplay.data.database.dao.WeatherCurrentDao
import com.smsoft.smartdisplay.data.database.dao.WeatherForecastDao
import com.smsoft.smartdisplay.data.database.entity.Sensor
import com.smsoft.smartdisplay.data.database.entity.WeatherCurrent
import com.smsoft.smartdisplay.data.database.entity.WeatherForecast

@Database(
    entities = [Sensor::class, WeatherCurrent::class, WeatherForecast::class],
    version = 1,
    exportSchema = false
)
abstract class SmartDisplayDatabase: RoomDatabase() {
    abstract fun sensorDao(): SensorDao
    abstract fun weatherCurrentDao(): WeatherCurrentDao
    abstract fun weatherForecastDao(): WeatherForecastDao
}