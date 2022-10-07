package com.smsoft.smartdisplay.ui.screen.clock

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import com.smsoft.smartdisplay.data.ClockType
import com.smsoft.smartdisplay.data.PreferenceKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@HiltViewModel
class ClockViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val TIMER_INTERVAL = 100L //100ms

    private val uiStatePrivate = MutableStateFlow(getTime())
    val uiState = uiStatePrivate.asStateFlow()

    private var isTimerStarted = true
    val dataStore = userPreferencesRepository.dataStore

    class UserPreferencesRepository @Inject constructor(
        val dataStore: DataStore<Preferences>
    )

    val clockType: Flow<ClockType> = dataStore.data
        .map { preferences ->
            ClockType.getById(preferences[stringPreferencesKey(PreferenceKey.CLOCK_TYPE.key)] ?: ClockType.getDefault().id)
        }

    fun onStart() {
        fixedRateTimer(
            name= "default",
            daemon = false,
            initialDelay = 0L,
            period = TIMER_INTERVAL
        ) {

            val cal = Calendar.getInstance()
            uiStatePrivate.update {
                it.copy(
                    year = cal.get(Calendar.YEAR),
                    month = cal.get(Calendar.MONTH),
                    day = cal.get(Calendar.DATE),
                    dayOfWeek = cal.get(Calendar.DAY_OF_WEEK),
                    hour = cal.get(Calendar.HOUR_OF_DAY),
                    minute = cal.get(Calendar.MINUTE),
                    second = cal.get(Calendar.SECOND),
                    milliSecond = cal.get(Calendar.MILLISECOND)
                )
            }

            if (!isTimerStarted) {
                cancel()
            }
        }
    }

    fun onStop() {
        isTimerStarted = false
    }

    private fun getTime(): ClockUiState {
        val cal = Calendar.getInstance()
        return ClockUiState(
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH),
            day = cal.get(Calendar.DATE),
            dayOfWeek = cal.get(Calendar.DAY_OF_WEEK),
            hour = cal.get(Calendar.HOUR_OF_DAY),
            minute = cal.get(Calendar.MINUTE),
            second = cal.get(Calendar.SECOND),
            milliSecond = cal.get(Calendar.MILLISECOND)
        )
    }
}

data class ClockUiState(
    val year: Int,
    val month: Int,
    val day: Int,
    val dayOfWeek: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val milliSecond: Int
)