package com.smsoft.smartdisplay.data.database.repository

import androidx.annotation.WorkerThread
import com.smsoft.smartdisplay.data.database.SmartDisplayDatabase
import com.smsoft.smartdisplay.data.database.entity.Alarm
import javax.inject.Inject

class AlarmRepository @Inject constructor(
    smartDisplayDatabase: SmartDisplayDatabase
) {
    private val alarmDao = smartDisplayDatabase.alarmDao()

    val getAll = alarmDao.getAll()

    fun get(id: Long): Alarm {
        return alarmDao.get(id)
    }

    @WorkerThread
    suspend fun insert(item: Alarm): Long {
        return alarmDao.insert(item)
    }

    @WorkerThread
    suspend fun update(item: Alarm) {
        alarmDao.update(item)
    }

    @WorkerThread
    suspend fun delete(item: Alarm) {
        alarmDao.delete(item)
    }
}