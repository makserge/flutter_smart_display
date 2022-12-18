package com.smsoft.smartdisplay.data.database.repository

import androidx.annotation.WorkerThread
import com.smsoft.smartdisplay.data.database.SmartDisplayDatabase
import com.smsoft.smartdisplay.data.database.entity.Sensor
import javax.inject.Inject

class SensorRepository @Inject constructor(
    smartDisplayDatabase: SmartDisplayDatabase
) {
    private val sensorDao = smartDisplayDatabase.sensorDao()

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