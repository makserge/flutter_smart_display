package com.smsoft.smartdisplay.data.database.repository

import androidx.annotation.WorkerThread
import com.smsoft.smartdisplay.data.database.SmartDisplayDatabase
import com.smsoft.smartdisplay.data.database.entity.Timer
import javax.inject.Inject

class TimerRepository @Inject constructor(
    smartDisplayDatabase: SmartDisplayDatabase
) {
    private val timerDao = smartDisplayDatabase.timerDao()

    val getAll = timerDao.getAll()

    fun get(id: Long): Timer {
        return timerDao.get(id)
    }

    @WorkerThread
    suspend fun insert(item: Timer): Long {
        return timerDao.insert(item)
    }

    @WorkerThread
    suspend fun update(item: Timer) {
        timerDao.update(item)
    }

    @WorkerThread
    suspend fun delete(item: Timer) {
        timerDao.delete(item)
    }
}