package com.smsoft.smartdisplay.ui.screen.dashboard.controller

import android.os.CountDownTimer
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.screen.settings.CLOCK_AUTO_RETURN_TIMEOUT_DEFAULT
import kotlinx.coroutines.flow.first

class ClockAutoReturnController(
    private val dataStore: DataStore<Preferences>,
    private val isExemptPage: () -> Boolean,
    private val onReturnToClock: () -> Unit,
) {
    private var enabled = false
    private var timeoutMinutes = CLOCK_AUTO_RETURN_TIMEOUT_DEFAULT
    private var timer: CountDownTimer? = null

    /**
     * Re-evaluates the timer for the current page. The exemption is checked after the settings
     * read (a suspension point), so a decision taken before the radio started cannot re-arm the
     * timer afterwards.
     */
    suspend fun update() {
        loadPreferences()
        if (!enabled || isExemptPage()) {
            timer?.cancel()
            return
        }
        restartTimer()
    }

    fun release() {
        timer?.cancel()
        timer = null
    }

    private suspend fun loadPreferences() {
        val data = dataStore.data.first()
        data[booleanPreferencesKey(PreferenceKey.CLOCK_AUTO_RETURN.key)]?.let {
            enabled = it
        }
        data[floatPreferencesKey(PreferenceKey.CLOCK_AUTO_RETURN_TIMEOUT.key)]?.let {
            timeoutMinutes = it
        }
    }

    private fun restartTimer() {
        timer?.cancel()
        timer = object : CountDownTimer((timeoutMinutes * 60000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                if (!isExemptPage()) {
                    onReturnToClock()
                }
            }
        }
        timer!!.start()
    }
}
