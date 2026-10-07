package com.smsoft.smartdisplay.service.radio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Whether the radio is switched on (playing, buffering or starting a station).
 *
 * Kept apart from RadioMediaServiceHandler so the dashboard can observe it without creating the
 * radio player: creating the handler creates the player from the radio type and MPD settings
 * (and the handler then follows them), which should happen on the first visit to the radio
 * page, not at app start.
 */
@Singleton
class RadioActiveState @Inject constructor() {
    internal val state = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = state.asStateFlow()
}
