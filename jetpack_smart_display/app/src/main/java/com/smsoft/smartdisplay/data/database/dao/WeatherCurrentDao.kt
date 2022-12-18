package com.smsoft.smartdisplay.data.database.dao

import androidx.room.*
import com.smsoft.smartdisplay.data.database.entity.WeatherCurrent
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherCurrentDao {
    @Query("SELECT * FROM weather_current ORDER BY id")
    fun getAll() : Flow<List<WeatherCurrent>>

    @Query("SELECT * FROM weather_current WHERE id = :id")
    fun get(id: Long) : Flow<WeatherCurrent?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: WeatherCurrent) : Long

    @Update
    suspend fun update(item: WeatherCurrent)
}