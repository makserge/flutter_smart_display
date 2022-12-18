package com.smsoft.smartdisplay.data.database.dao

import androidx.room.*
import com.smsoft.smartdisplay.data.database.entity.WeatherForecast
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherForecastDao {
    @Query("SELECT * FROM weather_forecast ORDER BY id")
    fun getAll() : Flow<List<WeatherForecast>>

    @Query("SELECT * FROM weather_forecast WHERE id = :id")
    fun get(id: Long) : WeatherForecast?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: WeatherForecast) : Long

    @Update
    suspend fun update(item: WeatherForecast)
}