package com.smsoft.smartdisplay.data.database.dao

import androidx.room.*
import com.smsoft.smartdisplay.data.database.entity.Sensor
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDao {
    @Query("SELECT * FROM sensors ORDER BY id")
    fun getAll() : Flow<List<Sensor>>

    @Query("SELECT * FROM sensors WHERE id = :id")
    fun get(id: Long) : Sensor

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Sensor) : Long

    @Update
    suspend fun update(item: Sensor)

    @Delete
    suspend fun delete(item: Sensor)
}