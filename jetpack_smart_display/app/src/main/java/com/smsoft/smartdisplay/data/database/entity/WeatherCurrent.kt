package com.smsoft.smartdisplay.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_current")
data class WeatherCurrent(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val temperature: Int,
    val humidity: Int,
    val icon: String,
    val windSpeed: Int,
    val windDirection: Int
)