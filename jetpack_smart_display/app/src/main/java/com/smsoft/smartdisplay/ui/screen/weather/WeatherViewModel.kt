package com.smsoft.smartdisplay.ui.screen.weather

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.database.repository.WeatherRepository
import com.smsoft.smartdisplay.service.workers.WeatherUpdateTaskWorker
import com.smsoft.smartdisplay.ui.screen.settings.WEATHER_CITY_DEFAULT_LAT
import com.smsoft.smartdisplay.ui.screen.settings.WEATHER_CITY_DEFAULT_LON
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
@SuppressLint("StaticFieldLeak")
@HiltViewModel
class WeatherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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
        var lat = WEATHER_CITY_DEFAULT_LAT.toDouble()
        data[stringPreferencesKey(PreferenceKey.WEATHER_CITY_LAT.key)]?.let {
            lat = if (it.isNotEmpty()) {
                it.toDouble()
            } else {
                WEATHER_CITY_DEFAULT_LAT.toDouble()
            }
        }
        var lon = WEATHER_CITY_DEFAULT_LON.toDouble()
        data[stringPreferencesKey(PreferenceKey.WEATHER_CITY_LON.key)]?.let {
            lon = if (it.isNotEmpty()) {
                it.toDouble()
            } else {
                WEATHER_CITY_DEFAULT_LON.toDouble()
            }
        }
        return Pair(lat, lon)
    }

    fun onStart(callback: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val city = getWeatherCity()
            if ((city.first == 0.0) || (city.second == 0.0)) {
                withContext(Dispatchers.Main) {
                    callback()
                }
            } else {
                val data = Data.Builder()
                    .putDouble(PreferenceKey.WEATHER_CITY_LAT.key, city.first)
                    .putDouble(PreferenceKey.WEATHER_CITY_LON.key, city.second)
                    .build()
                val request = OneTimeWorkRequestBuilder<WeatherUpdateTaskWorker>()
                    .setInputData(data)
                    .build()
                CoroutineScope(Dispatchers.Default).launch {
                    val liveData =
                        WorkManager.getInstance(context).getWorkInfoByIdLiveData(request.id)
                    withContext(Dispatchers.Main) {
                        liveData.observeForever(object : Observer<WorkInfo> {
                            override fun onChanged(value: WorkInfo) {
                                if (value.state == WorkInfo.State.SUCCEEDED) {
                                    liveData.removeObserver(this)

                                    uiStateInt.value = UIState.Ready
                                    CoroutineScope(Dispatchers.IO).launch {
                                        startPeriodicalUpdate(data)
                                    }
                                }
                            }
                        })
                    }
                }
                WorkManager.getInstance(context).enqueue(request)
            }
        }
    }

    private fun startPeriodicalUpdate(data: Data) {
        val work = PeriodicWorkRequestBuilder<WeatherUpdateTaskWorker>(
            WEATHER_UPDATE_PERIOD,
            TimeUnit.MINUTES,
            WEATHER_UPDATE_PERIOD,
            TimeUnit.MINUTES
        )
            .setInputData(data)
            .build()
        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, work)
    }


    fun windDegreeToDirection(deg: Int) = windDirections[(deg % 360) / 45]

    fun getDayOfWeek(date: Long, timezone: String): String =
        DateTimeFormatter.ofPattern("EEEE")
            .format(Instant.ofEpochSecond(date).atZone(ZoneId.of(timezone)))

}

sealed class UIState {
    data object Initial : UIState()
    data object Ready : UIState()
}

const val WORK_NAME = "weatherUpdater"
const val WEATHER_UPDATE_PERIOD = 240L //4h
private val windDirections by lazy { listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW") }
