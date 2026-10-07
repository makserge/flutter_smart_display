package com.smsoft.smartdisplay.ui.screen.doorbell

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.screen.settings.DOORBELL_BACK_TIMER_DEFAULT_DELAY
import com.smsoft.smartdisplay.ui.screen.settings.DOORBELL_STREAM_DEFAULT_URL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.videolan.libvlc.util.VLCVideoLayout
import javax.inject.Inject

@HiltViewModel
class DoorbellViewModel @Inject constructor(
    private val streamPlayer: DoorbellStreamPlayer,
    val dataStore: DataStore<Preferences>
) : ViewModel() {

    // The current visit of this screen and its stream start and back timer. onStop() cancels the
    // job, so a visit that ends while the settings are still being read never starts the stream.
    private var visit: DoorbellStreamPlayer.Visit? = null
    private var visitJob: Job? = null

    private val backRequestsInt = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    /** Emits once when the back timer of the current visit has run out. */
    val backRequests: SharedFlow<Unit> = backRequestsInt.asSharedFlow()

    fun onStart(
        videoLayout: VLCVideoLayout,
        isBackTimerEnabled: Boolean
    ): DoorbellStreamPlayer.Visit {
        visitJob?.cancel()
        visit?.end()
        val newVisit = streamPlayer.startVisit(videoLayout)
        visit = newVisit
        visitJob = viewModelScope.launch {
            // Read here instead of with runBlocking on the main thread.
            val data = dataStore.data.first()
            newVisit.play(getDoorbellStreamUrl(data))
            if (isBackTimerEnabled) {
                val delaySeconds = data[floatPreferencesKey(PreferenceKey.DOORBELL_BACK_TIMER_DELAY.key)]
                    ?: DOORBELL_BACK_TIMER_DEFAULT_DELAY
                delay((delaySeconds * 1000L).toLong())
                backRequestsInt.tryEmit(Unit)
            }
        }
        return newVisit
    }

    /** Ends [visit]; the stream keeps playing if another doorbell screen took the player over. */
    fun onStop(visit: DoorbellStreamPlayer.Visit) {
        visit.end()
        if (visit === this.visit) {
            visitJob?.cancel()
            visitJob = null
            this.visit = null
        }
    }

    override fun onCleared() {
        visit?.end()
        visit = null
        super.onCleared()
    }

    private fun getDoorbellStreamUrl(data: Preferences): String {
        val url = data[stringPreferencesKey(PreferenceKey.DOORBELL_STREAM_URL.key)]?.trim()
        return if (url.isNullOrEmpty()) DOORBELL_STREAM_DEFAULT_URL else url
    }
}
