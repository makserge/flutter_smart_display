package com.smsoft.smartdisplay.ui.screen.weather

import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.database.repository.WeatherRepository
import com.smsoft.smartdisplay.service.workers.WeatherUpdateTaskWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    val dataStore: DataStore<Preferences>,
    val weatherRepository: WeatherRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val uiStateInt = MutableStateFlow<UIState>(UIState.Initial)
    val uiState = uiStateInt.asStateFlow()

    val currentForecast = weatherRepository.currentForecast
    val weatherForecast = weatherRepository.weatherForecast

    private suspend fun getWeatherCity(): Pair<Double, Double> {
        val data = dataStore.data.first()
        var lat = 0.0
        data[stringPreferencesKey(PreferenceKey.WEATHER_CITY_LAT.key)]?.let {
            lat = if (it.isNotEmpty()) {
                it.toDouble()
            } else {
                0.0
            }
        }
        var lon = 0.0
        data[stringPreferencesKey(PreferenceKey.WEATHER_CITY_LON.key)]?.let {
            lon = if (it.isNotEmpty()) {
                it.toDouble()
            } else {
                0.0
            }
        }
        return Pair(lat, lon)
    }

    suspend fun onStart(): Boolean {
        val city = getWeatherCity()
        if ((city.first == 0.0) || (city.second == 0.0)) {
            return true
        }
        startPeriodicalUpdate(city)
        uiStateInt.value = UIState.Ready
        return false
    }

    private fun startPeriodicalUpdate(city: Pair<Double, Double>) {
        val data = Data.Builder()
            .putDouble(PreferenceKey.WEATHER_CITY_LAT.key, city.first)
            .putDouble(PreferenceKey.WEATHER_CITY_LON.key, city.second)
            .build()
        val work = PeriodicWorkRequestBuilder<WeatherUpdateTaskWorker>(WEATHER_UPDATE_PERIOD, TimeUnit.MINUTES)
            .setInputData(data)
            .build()
        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, work)
    }

    fun windDegreeToDirection(deg: Int) = windDirections[(deg % 360) / 45]

    fun getDayOfWeek(date: Long, timezone: String): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        DateTimeFormatter.ofPattern("EEEE")
            .format(Instant.ofEpochSecond(date).atZone(ZoneId.of(timezone)))
    } else {
        val sdf = SimpleDateFormat("EEEE", Locale.US).apply {
            timeZone = TimeZone.getTimeZone(timezone)
        }
        sdf.format(date)
    }

}

sealed class UIState {
    object Initial : UIState()
    object Ready : UIState()
}

const val WORK_NAME = "weatherUpdater"
const val WEATHER_UPDATE_PERIOD = 240L //4h
private val windDirections by lazy { listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW") }
