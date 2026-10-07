package com.smsoft.smartdisplay.service.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.database.repository.WeatherRepository
import com.smsoft.smartdisplay.network.WeatherResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class WeatherUpdateTaskWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository,
    private val workManager: WorkManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val lat = inputData.getDouble(PreferenceKey.WEATHER_CITY_LAT.key, Double.NaN)
        val lon = inputData.getDouble(PreferenceKey.WEATHER_CITY_LON.key, Double.NaN)
        if (!isValidLocation(lat, lon)) {
            // Retrying can't fix the input; new work is scheduled when the setting is fixed
            Log.w(TAG, "attempt=$runAttemptCount invalid location")
            return Result.failure()
        }
        val result = withContext(Dispatchers.IO) {
            weatherRepository.updateWeather(
                lat = lat,
                lon = lon
            )
        }
        return when (result) {
            is WeatherResult.Success -> {
                Log.i(TAG, "attempt=$runAttemptCount ok")
                Result.success()
            }
            is WeatherResult.Failure -> {
                Log.i(TAG, "attempt=$runAttemptCount kind=${result.kind} http=${result.httpCode}")
                if (tags.contains(PERIODIC_TAG)) {
                    // Periodic work has no useful backoff; hand over to the one-time refresh,
                    // so there is only one retry chain. The delay avoids an instant second call.
                    enqueueRefresh(
                        workManager = workManager,
                        lat = lat,
                        lon = lon,
                        policy = ExistingWorkPolicy.KEEP,
                        delaySeconds = BACKOFF_DELAY_SECONDS
                    )
                    Result.success()
                } else {
                    Result.retry()
                }
            }
        }
    }

    companion object {
        // Same name as before, so the periodic work persisted by older versions is updated
        const val PERIODIC_WORK_NAME = "weatherUpdater"
        const val REFRESH_WORK_NAME = "weatherRefresh"
        const val PERIODIC_TAG = "weatherPeriodic"
        const val WEATHER_UPDATE_PERIOD = 240L //4h
        private const val BACKOFF_DELAY_SECONDS = 30L
        private const val TAG = "WeatherWorker"

        // 0.0 means "not set", as before
        fun isValidLocation(lat: Double, lon: Double) =
            (lat in -90.0..90.0) && (lon in -180.0..180.0) && (lat != 0.0) && (lon != 0.0)

        // refreshNow requests an update now (with retry and backoff); force restarts a pending one.
        // The periodic work is always (re)enqueued with the current location.
        fun schedule(
            workManager: WorkManager,
            lat: Double,
            lon: Double,
            refreshNow: Boolean,
            force: Boolean
        ) {
            if (refreshNow) {
                enqueueRefresh(
                    workManager = workManager,
                    lat = lat,
                    lon = lon,
                    policy = if (force) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP
                )
            }
            val work = PeriodicWorkRequestBuilder<WeatherUpdateTaskWorker>(
                WEATHER_UPDATE_PERIOD,
                TimeUnit.MINUTES
            )
                // The refresh covers "now"; without the delay the first periodic run would repeat it
                .setInitialDelay(WEATHER_UPDATE_PERIOD, TimeUnit.MINUTES)
                .setConstraints(networkConstraints())
                .setInputData(locationData(lat, lon))
                .addTag(PERIODIC_TAG)
                .build()
            workManager.enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                work
            )
        }

        private fun enqueueRefresh(
            workManager: WorkManager,
            lat: Double,
            lon: Double,
            policy: ExistingWorkPolicy,
            delaySeconds: Long = 0L
        ) {
            val work = OneTimeWorkRequestBuilder<WeatherUpdateTaskWorker>()
                .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                .setInputData(locationData(lat, lon))
                .setConstraints(networkConstraints())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY_SECONDS, TimeUnit.SECONDS)
                .build()
            workManager.enqueueUniqueWork(REFRESH_WORK_NAME, policy, work)
        }

        // Without a network the work waits instead of failing and using up retries
        private fun networkConstraints() = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        private fun locationData(lat: Double, lon: Double) = Data.Builder()
            .putDouble(PreferenceKey.WEATHER_CITY_LAT.key, lat)
            .putDouble(PreferenceKey.WEATHER_CITY_LON.key, lon)
            .build()
    }
}
