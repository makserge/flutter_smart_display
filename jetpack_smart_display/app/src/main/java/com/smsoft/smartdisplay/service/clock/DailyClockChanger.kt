package com.smsoft.smartdisplay.service.clock

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.data.ClockType
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.composable.settings.RANDOM_CLOCK_DAILY_DEFAULT
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * With "Random clock every day" on (the default), switches the clock page to a random other clock
 * type every day at 00:00. The day of the last switch is stored, so a panel that was off at
 * midnight switches when the app starts again. Turning the setting on switches first at the next
 * midnight.
 */
@Singleton
class DailyClockChanger @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val coroutineScope: CoroutineScope
) {
    private var job: Job? = null

    fun start() {
        if (job != null) {
            return
        }
        job = coroutineScope.launch {
            while (isActive) {
                try {
                    checkDay()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Daily clock change failed", e)
                }
                delay(nextCheckDelayMs())
            }
        }
    }

    private suspend fun checkDay() {
        val today = LocalDate.now().toEpochDay()
        dataStore.edit { preferences ->
            val current = preferences[stringPreferencesKey(PreferenceKey.CLOCK_TYPE.key)]
                ?: ClockType.getDefaultId()
            updateDailyClock(preferences, today)?.let { next ->
                Log.i(TAG, "New clock for the day: ${next.id} (was $current)")
            }
        }
    }

    private companion object {
        const val TAG = "DailyClockChanger"
    }
}

// The day (epoch day) the random clock last switched or started counting.
internal val LAST_DAY_KEY = longPreferencesKey("randomClockLastDay")
internal const val MIDNIGHT_MARGIN_MS = 1_000L
internal const val MAX_CHECK_INTERVAL_MS = 15 * 60_000L

/**
 * One daily check on [preferences] for [today] (epoch day). Returns the clock type it switched
 * to, or null when the day did not change the clock.
 */
internal fun updateDailyClock(
    preferences: MutablePreferences,
    today: Long,
    random: Random = Random
): ClockType? {
    val isEnabled = preferences[booleanPreferencesKey(PreferenceKey.RANDOM_CLOCK_DAILY.key)]
        ?: RANDOM_CLOCK_DAILY_DEFAULT
    val lastDay = preferences[LAST_DAY_KEY]
    when {
        !isEnabled -> preferences.remove(LAST_DAY_KEY)
        // Just switched on, or the date was set back: start counting from today.
        (lastDay == null) || (lastDay > today) -> preferences[LAST_DAY_KEY] = today
        lastDay < today -> {
            val current = preferences[stringPreferencesKey(PreferenceKey.CLOCK_TYPE.key)]
                ?: ClockType.getDefaultId()
            val next = ClockType.entries.filter { it.id != current }.random(random)
            preferences[stringPreferencesKey(PreferenceKey.CLOCK_TYPE.key)] = next.id
            preferences[LAST_DAY_KEY] = today
            return next
        }
    }
    return null
}

/**
 * Until just after the next midnight, but at most [MAX_CHECK_INTERVAL_MS]: a date or time zone
 * change, or a delay stretched by deep sleep, is then noticed within that interval.
 */
internal fun nextCheckDelayMs(now: LocalDateTime = LocalDateTime.now()): Long {
    val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
    val untilMidnight = Duration.between(now, nextMidnight).toMillis() + MIDNIGHT_MARGIN_MS
    return untilMidnight.coerceIn(MIDNIGHT_MARGIN_MS, MAX_CHECK_INTERVAL_MS)
}
