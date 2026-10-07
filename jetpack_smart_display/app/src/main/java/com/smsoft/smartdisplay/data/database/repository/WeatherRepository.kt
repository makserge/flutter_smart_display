package com.smsoft.smartdisplay.data.database.repository

import androidx.room.withTransaction
import com.smsoft.smartdisplay.data.database.SmartDisplayDatabase
import com.smsoft.smartdisplay.data.database.entity.WeatherCurrent
import com.smsoft.smartdisplay.data.database.entity.WeatherForecast
import com.smsoft.smartdisplay.network.WeatherApi
import com.smsoft.smartdisplay.network.WeatherFailureKind
import com.smsoft.smartdisplay.network.WeatherNetworkCurrent
import com.smsoft.smartdisplay.network.WeatherNetworkDaily
import com.smsoft.smartdisplay.network.WeatherResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

data class WeatherLocation(
    val lat: Double,
    val lon: Double
)

// Outcome of the update attempts in this process (times are System.currentTimeMillis())
data class WeatherUpdateStatus(
    val inProgress: Boolean = false,
    val lastAttemptAt: Long? = null,
    val lastSuccessAt: Long? = null,
    val lastFailure: WeatherResult.Failure? = null,
    val lastLocation: WeatherLocation? = null
)

class WeatherRepository@Inject constructor(
    private val smartDisplayDatabase: SmartDisplayDatabase,
    private val weatherApi: WeatherApi
) {
    val CURRENT_ID = 1L
    private val FORECAST_DAYS = 2

    private val weatherCurrentDao = smartDisplayDatabase.weatherCurrentDao()
    private val weatherForecastDao = smartDisplayDatabase.weatherForecastDao()

    val currentForecast = weatherCurrentDao.get(CURRENT_ID)
    val weatherForecast = weatherForecastDao.getAll()

    // Kept in memory: the repository is a singleton and the worker runs in this process
    private val updateStatusInt = MutableStateFlow(WeatherUpdateStatus())
    val updateStatus = updateStatusInt.asStateFlow()

    // The refresh and the periodic work may overlap, so count running updates
    private val runningUpdates = AtomicInteger(0)

    suspend fun updateWeather(
        lat: Double,
        lon:Double
    ): WeatherResult {
        runningUpdates.incrementAndGet()
        updateStatusInt.update { it.copy(inProgress = true) }
        var result: WeatherResult? = null
        try {
            result = try {
                fetchAndSave(lat, lon)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // For example a database error; reported instead of failing the work
                WeatherResult.Failure(e.toString(), WeatherFailureKind.UNKNOWN)
            }
            return result
        } finally {
            finishUpdate(result, WeatherLocation(lat, lon))
        }
    }

    private suspend fun fetchAndSave(
        lat: Double,
        lon: Double
    ): WeatherResult {
        val result = weatherApi.getForecast(lat, lon)
        if (result !is WeatherResult.Success) {
            return result
        }
        // Check before writing, so an incomplete response never replaces good data
        if (result.current.weather.isEmpty() || (result.daily.size < FORECAST_DAYS)) {
            return WeatherResult.Failure("incomplete response", WeatherFailureKind.PARSE)
        }
        smartDisplayDatabase.withTransaction {
            saveForecast(
                timezone = result.timezone,
                current = result.current,
                daily = result.daily
            )
        }
        return result
    }

    // result is null when the update was cancelled; then only the progress flag changes
    private fun finishUpdate(
        result: WeatherResult?,
        location: WeatherLocation
    ) {
        runningUpdates.decrementAndGet()
        val now = System.currentTimeMillis()
        updateStatusInt.update { status ->
            val inProgress = runningUpdates.get() > 0
            when (result) {
                null -> status.copy(
                    inProgress = inProgress
                )
                is WeatherResult.Success -> status.copy(
                    inProgress = inProgress,
                    lastAttemptAt = now,
                    lastSuccessAt = now,
                    lastFailure = null,
                    lastLocation = location
                )
                is WeatherResult.Failure -> status.copy(
                    inProgress = inProgress,
                    lastAttemptAt = now,
                    lastFailure = result,
                    lastLocation = location
                )
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
            icon = current.weather.firstOrNull()?.icon.orEmpty(),
            windSpeed = (current.windSpeed * 3.6).roundToInt(),
            windDirection = current.windDeg
        )
        // insert() ignores an existing row and returns -1; then the row is updated.
        // This avoids blocking reads inside the transaction.
        if (weatherCurrentDao.insert(currentWeather) == -1L) {
            weatherCurrentDao.update(currentWeather)
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
                icon = item.weather.firstOrNull()?.icon.orEmpty(),
                windSpeed = (item.windSpeed * 3.6).roundToInt(),
                windDirection = item.windDeg
            )
            if (weatherForecastDao.insert(weatherForecast) == -1L) {
                weatherForecastDao.update(weatherForecast)
            }
        }
        weatherForecastDao.deleteBeyond(FORECAST_DAYS.toLong())
    }
}
