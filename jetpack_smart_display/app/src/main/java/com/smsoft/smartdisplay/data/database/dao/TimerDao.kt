package com.smsoft.smartdisplay.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smsoft.smartdisplay.data.database.entity.Timer
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerDao {
    @Query("SELECT * FROM timers ORDER BY id")
    fun getAll() : Flow<List<Timer>>

    @Query("SELECT * FROM timers WHERE id = :id")
    fun get(id: Long) : Timer

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Timer) : Long

    @Update
    suspend fun update(item: Timer)

    @Delete
    suspend fun delete(item: Timer)
}