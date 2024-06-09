package com.smsoft.smartdisplay.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smsoft.smartdisplay.data.database.entity.Alarm
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY id")
    fun getAll() : Flow<List<Alarm>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    fun get(id: Long) : Alarm

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Alarm) : Long

    @Update
    suspend fun update(item: Alarm)

    @Delete
    suspend fun delete(item: Alarm)
}