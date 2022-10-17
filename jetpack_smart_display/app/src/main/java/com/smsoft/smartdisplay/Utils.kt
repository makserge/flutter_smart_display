package com.smsoft.smartdisplay

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.map

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun getParam(dataStore: DataStore<Preferences>, defaultValue: Any?, getter: (preferences: Preferences) -> Any?): Any? {
    return dataStore.data.map {
        getter(it) ?: defaultValue
    }.collectAsState(initial = defaultValue).value
}