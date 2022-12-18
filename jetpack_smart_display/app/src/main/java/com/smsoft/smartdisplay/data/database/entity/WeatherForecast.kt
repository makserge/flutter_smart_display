package com.smsoft.smartdisplay.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_forecast")
data class WeatherForecast(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val date: Long,
    val timezone: String,
    val temperatureMorning: Int,
    val temperatureDay: Int,
    val temperatureEvening: Int,
    val temperatureNight: Int,
    val humidity: Int,
    val icon: String,
    val windSpeed: Int,
    val windDirection: Int
)