package com.smsoft.smartdisplay.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.smsoft.smartdisplay.data.database.dao.AlarmDao
import com.smsoft.smartdisplay.data.database.dao.SensorDao
import com.smsoft.smartdisplay.data.database.dao.TimerDao
import com.smsoft.smartdisplay.data.database.dao.WeatherCurrentDao
import com.smsoft.smartdisplay.data.database.dao.WeatherForecastDao
import com.smsoft.smartdisplay.data.database.entity.Alarm
import com.smsoft.smartdisplay.data.database.entity.Sensor
import com.smsoft.smartdisplay.data.database.entity.Timer
import com.smsoft.smartdisplay.data.database.entity.WeatherCurrent
import com.smsoft.smartdisplay.data.database.entity.WeatherForecast

@Database(
    version = 3,
    entities = [
        Alarm::class,
        Sensor::class,
        Timer::class,
        WeatherCurrent::class,
        WeatherForecast::class
    ],
    autoMigrations = [
        AutoMigration (
            from = 2,
            to = 3
        )
    ],
    exportSchema = true
)

abstract class SmartDisplayDatabase: RoomDatabase() {

    abstract fun alarmDao(): AlarmDao
    abstract fun sensorDao(): SensorDao
    abstract fun timerDao(): TimerDao
    abstract fun weatherCurrentDao(): WeatherCurrentDao
    abstract fun weatherForecastDao(): WeatherForecastDao
}