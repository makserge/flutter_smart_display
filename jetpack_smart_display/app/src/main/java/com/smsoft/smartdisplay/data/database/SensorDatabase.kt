package com.smsoft.smartdisplay.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smsoft.smartdisplay.data.database.dao.SensorDao
import com.smsoft.smartdisplay.data.database.entity.Sensor

@Database(entities = [Sensor::class], version = 1, exportSchema = false)
abstract class SensorDatabase: RoomDatabase() {
    abstract fun sensorDao(): SensorDao
}