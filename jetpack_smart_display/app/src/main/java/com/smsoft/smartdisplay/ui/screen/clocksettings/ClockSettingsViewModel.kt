package com.smsoft.smartdisplay.ui.screen.clocksettings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ClockSettingsViewModel @Inject constructor(
    val dataStore: DataStore<Preferences>
) : ViewModel() {

}
