package com.smsoft.smartdisplay.service.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.database.repository.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class WeatherUpdateTaskWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {
            val lat = inputData.getDouble(PreferenceKey.WEATHER_CITY_LAT.key, 0.0)
            val lon = inputData.getDouble(PreferenceKey.WEATHER_CITY_LON.key, 0.0)
            if ((lat == 0.0) || (lon == 0.0)) {
                return@withContext
            }
            weatherRepository.updateWeather(
                lat = lat,
                lon = lon
            )
            return@withContext
        }
        return Result.success()
    }
}