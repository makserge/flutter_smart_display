package com.smsoft.smartdisplay.data.database.repository

import androidx.annotation.WorkerThread
import com.smsoft.smartdisplay.data.database.SmartDisplayDatabase
import com.smsoft.smartdisplay.data.database.entity.Sensor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SensorRepository @Inject constructor(
    smartDisplayDatabase: SmartDisplayDatabase
) {
    private val sensorDao = smartDisplayDatabase.sensorDao()

    val getAll = sensorDao.getAll()

    fun getByType(type: String): Flow<List<Sensor>> {
        return sensorDao.getByType(type)
    }

    fun get(id: Long): Sensor {
        return sensorDao.get(id)
    }

    @WorkerThread
    suspend fun insert(item: Sensor): Long {
        return sensorDao.insert(item)
    }

    @WorkerThread
    suspend fun update(item: Sensor) {
        sensorDao.update(item)
    }

    @WorkerThread
    suspend fun delete(item: Sensor) {
        sensorDao.delete(item)
    }
}