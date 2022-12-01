package com.smsoft.smartdisplay.data.database.repository

import androidx.annotation.WorkerThread
import com.smsoft.smartdisplay.data.database.SensorDatabase
import com.smsoft.smartdisplay.data.database.entity.Sensor
import javax.inject.Inject

class SensorRepository @Inject constructor(
    sensorDatabase: SensorDatabase
) {
    private val sensorDao = sensorDatabase.sensorDao()

    val getAll = sensorDao.getAll()

    fun get(id: Long): Sensor {
        return sensorDao.get(id)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(item: Sensor): Long {
        return sensorDao.insert(item)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(item: Sensor) {
        sensorDao.update(item)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(item: Sensor) {
        sensorDao.delete(item)
    }
}