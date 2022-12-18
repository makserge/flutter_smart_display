package com.smsoft.smartdisplay.data.database.repository

import com.smsoft.smartdisplay.data.database.SmartDisplayDatabase
import com.smsoft.smartdisplay.data.database.entity.WeatherCurrent
import com.smsoft.smartdisplay.data.database.entity.WeatherForecast
import com.smsoft.smartdisplay.network.WeatherApi
import com.smsoft.smartdisplay.network.WeatherNetworkCurrent
import com.smsoft.smartdisplay.network.WeatherNetworkDaily
import com.smsoft.smartdisplay.network.WeatherResult
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.math.roundToInt

class WeatherRepository@Inject constructor(
    smartDisplayDatabase: SmartDisplayDatabase,
    private val weatherApi: WeatherApi
) {
    val CURRENT_ID = 1L
    private val FORECAST_DAYS = 4

    private val weatherCurrentDao = smartDisplayDatabase.weatherCurrentDao()
    private val weatherForecastDao = smartDisplayDatabase.weatherForecastDao()

    val currentForecast = weatherCurrentDao.get(CURRENT_ID)
    val weatherForecast = weatherForecastDao.getAll()

    suspend fun updateWeather(
        lat: Double,
        lon:Double
    ) {
        weatherApi.getForecast(lat, lon).run {
            when (this) {
                is WeatherResult.Failure -> {
                }
                is WeatherResult.Success -> {
                    this.run {
                        saveForecast(
                            timezone = timezone,
                            current = current,
                            daily = daily
                        )
                    }
                }
            }
        }
    }

    private suspend fun saveForecast(
        timezone: String,
        current: WeatherNetworkCurrent,
        daily: List<WeatherNetworkDaily>
    ) {
        val currentWeather = WeatherCurrent(
            id = CURRENT_ID,
            temperature = current.temp.roundToInt(),
            humidity = current.humidity.roundToInt(),
            icon = current.weather.first().icon,
            windSpeed = (current.windSpeed * 3.6).roundToInt(),
            windDirection = current.windDeg
        )
        val oldCurrentWeather = weatherCurrentDao.get(CURRENT_ID).first()
        if (oldCurrentWeather != null) {
            weatherCurrentDao.update(currentWeather)
        } else {
            weatherCurrentDao.insert(currentWeather)
        }
        for (i in 0 until FORECAST_DAYS) {
            val id = (i + 1).toLong()
            val item = daily[i]
            val weatherForecast = WeatherForecast(
                id = id,
                date = item.dt,
                timezone = timezone,
                temperatureMorning = item.temp.morn.roundToInt(),
                temperatureDay = item.temp.day.roundToInt(),
                temperatureEvening = item.temp.eve.roundToInt(),
                temperatureNight = item.temp.night.roundToInt(),
                humidity = item.humidity.roundToInt(),
                icon = item.weather.first().icon,
                windSpeed = (item.windSpeed * 3.6).roundToInt(),
                windDirection = item.windDeg
            )
            val oldForecast = weatherForecastDao.get(id)
            if (oldForecast != null) {
                weatherForecastDao.update(weatherForecast)
            } else {
                weatherForecastDao.insert(weatherForecast)
            }
        }
    }
}