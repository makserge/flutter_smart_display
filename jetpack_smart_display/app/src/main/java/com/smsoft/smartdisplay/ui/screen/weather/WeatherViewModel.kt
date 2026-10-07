package com.smsoft.smartdisplay.ui.screen.weather

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.database.entity.WeatherCurrent
import com.smsoft.smartdisplay.data.database.entity.WeatherForecast
import com.smsoft.smartdisplay.data.database.repository.WeatherLocation
import com.smsoft.smartdisplay.data.database.repository.WeatherRepository
import com.smsoft.smartdisplay.network.WeatherResult
import com.smsoft.smartdisplay.service.workers.WeatherUpdateTaskWorker
import com.smsoft.smartdisplay.ui.screen.settings.WEATHER_CITY_DEFAULT_LAT
import com.smsoft.smartdisplay.ui.screen.settings.WEATHER_CITY_DEFAULT_LON
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val weatherRepository: WeatherRepository,
    private val workManager: WorkManager
) : ViewModel() {

    // True while the screen waits for a requested update; always cleared after AWAIT_TIMEOUT_MS
    private val awaiting = MutableStateFlow(true)
    private var awaitingJob: Job? = null

    // Location of the last update requested here, to notice a changed setting
    private var lastRequestedLocation: WeatherLocation? = null

    private val weatherLocation: Flow<WeatherLocation?> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { preferences ->
            parseLocation(
                lat = preferences[stringPreferencesKey(PreferenceKey.WEATHER_CITY_LAT.key)],
                lon = preferences[stringPreferencesKey(PreferenceKey.WEATHER_CITY_LON.key)]
            )
        }
        .distinctUntilChanged()

    // Re-checks "stale" when the day changes and no new data arrives
    private val clock = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(CLOCK_TICK_MS)
        }
    }

    val uiState: StateFlow<WeatherUiState> = combine(
        combine(
            weatherRepository.currentForecast,
            weatherRepository.weatherForecast
        ) { current, forecast ->
            current to forecast
        },
        weatherRepository.updateStatus,
        weatherLocation,
        awaiting,
        clock
    ) { (current, forecast), status, location, isAwaiting, now ->
        when {
            location == null -> WeatherUiState.NoLocation
            // The last good data is shown instead of a spinner or an error
            current != null -> WeatherUiState.Content(
                current = current,
                forecast = forecast,
                failure = status.lastFailure,
                lastSuccessAt = status.lastSuccessAt,
                isStale = isStale(forecast, now)
            )
            isAwaiting -> WeatherUiState.Loading
            else -> WeatherUiState.Error(status.lastFailure)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = WeatherUiState.Loading
    )

    init {
        // Bounds the first spinner even if onStart() never runs
        startAwaiting()
    }

    fun onStart() {
        viewModelScope.launch {
            val location = weatherLocation.first()
            if (location == null) {
                stopAwaiting()
                return@launch
            }
            val status = weatherRepository.updateStatus.value
            val previousLocation = lastRequestedLocation ?: status.lastLocation
            val locationChanged = (previousLocation != null) && (previousLocation != location)
            val lastSuccessAt = status.lastSuccessAt
            val age = if (lastSuccessAt == null) null else System.currentTimeMillis() - lastSuccessAt
            // The page is entered often (auto-return, voice jumps), so only refresh old data
            val refreshNow = locationChanged || (age == null) || (age < 0) || (age > REFRESH_AFTER_MS)
            if (refreshNow || (lastRequestedLocation == null)) {
                requestUpdate(
                    location = location,
                    refreshNow = refreshNow,
                    // The first request of this process replaces a refresh that WorkManager kept
                    // from an earlier one, which may still carry the old location.
                    force = locationChanged || (previousLocation == null)
                )
            }
            // No spinner while a failed update is backing off; its error stays visible
            if (refreshNow && (locationChanged || (status.lastAttemptAt == null))) {
                startAwaiting()
            } else {
                stopAwaiting()
            }
        }
    }

    fun retry() {
        viewModelScope.launch {
            val location = weatherLocation.first() ?: return@launch
            requestUpdate(
                location = location,
                refreshNow = true,
                force = true
            )
            startAwaiting()
        }
    }

    private fun requestUpdate(
        location: WeatherLocation,
        refreshNow: Boolean,
        force: Boolean
    ) {
        lastRequestedLocation = location
        WeatherUpdateTaskWorker.schedule(
            workManager = workManager,
            lat = location.lat,
            lon = location.lon,
            refreshNow = refreshNow,
            force = force
        )
    }

    // Shows the spinner until the next update attempt finishes, but never longer than AWAIT_TIMEOUT_MS
    private fun startAwaiting() {
        val previousAttemptAt = weatherRepository.updateStatus.value.lastAttemptAt
        awaitingJob?.cancel()
        awaiting.value = true
        awaitingJob = viewModelScope.launch {
            withTimeoutOrNull(AWAIT_TIMEOUT_MS) {
                weatherRepository.updateStatus.first { it.lastAttemptAt != previousAttemptAt }
            }
            awaiting.value = false
        }
    }

    private fun stopAwaiting() {
        awaitingJob?.cancel()
        awaitingJob = null
        awaiting.value = false
    }

    fun windDegreeToDirection(deg: Int) = windDirections[(deg % 360) / 45]

    fun getDayOfWeek(date: Long, timezone: String): String =
        DateTimeFormatter.ofPattern("EEEE")
            .format(Instant.ofEpochSecond(date).atZone(zoneOf(timezone)))

    // When the shown data was fetched; the first forecast day if the fetch time is unknown (after a restart)
    fun getDataTime(lastSuccessAt: Long?, forecast: List<WeatherForecast>): String? {
        if (lastSuccessAt != null) {
            return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .format(Instant.ofEpochMilli(lastSuccessAt).atZone(ZoneId.systemDefault()))
        }
        val first = forecast.firstOrNull() ?: return null
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .format(Instant.ofEpochSecond(first.date).atZone(zoneOf(first.timezone)))
    }
}

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data object NoLocation : WeatherUiState
    // failure == null: no attempt has finished yet, for example while waiting for a network
    data class Error(
        val failure: WeatherResult.Failure?
    ) : WeatherUiState
    data class Content(
        val current: WeatherCurrent,
        val forecast: List<WeatherForecast>,
        val failure: WeatherResult.Failure?,
        val lastSuccessAt: Long?,
        val isStale: Boolean
    ) : WeatherUiState
}

// The setting is free text: empty means the default, and a comma works as decimal separator ("48,13")
private fun parseLocation(lat: String?, lon: String?): WeatherLocation? {
    val latitude = parseCoordinate(lat, WEATHER_CITY_DEFAULT_LAT) ?: return null
    val longitude = parseCoordinate(lon, WEATHER_CITY_DEFAULT_LON) ?: return null
    return if (WeatherUpdateTaskWorker.isValidLocation(latitude, longitude)) {
        WeatherLocation(latitude, longitude)
    } else {
        null
    }
}

private fun parseCoordinate(value: String?, default: String): Double? =
    value.orEmpty().trim().ifEmpty { default }.replace(',', '.').toDoubleOrNull()

// Stale means the first forecast day is already over
private fun isStale(forecast: List<WeatherForecast>, now: Long): Boolean {
    val first = forecast.firstOrNull() ?: return false
    val zone = zoneOf(first.timezone)
    val day = Instant.ofEpochSecond(first.date).atZone(zone).toLocalDate()
    return day.isBefore(Instant.ofEpochMilli(now).atZone(zone).toLocalDate())
}

private fun zoneOf(timezone: String): ZoneId = try {
    ZoneId.of(timezone)
} catch (e: DateTimeException) {
    ZoneId.systemDefault()
}

private const val REFRESH_AFTER_MS = 30 * 60 * 1000L
private const val AWAIT_TIMEOUT_MS = 15_000L
private const val CLOCK_TICK_MS = 10 * 60 * 1000L
private const val STOP_TIMEOUT_MS = 5_000L
private val windDirections by lazy { listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW") }
