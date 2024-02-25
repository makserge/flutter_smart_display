package com.smsoft.smartdisplay.ui.composable.settings

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScope
import com.jamal.composeprefs.ui.prefs.EditTextPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.screen.settings.WEATHER_CITY_DEFAULT_LAT
import com.smsoft.smartdisplay.ui.screen.settings.WEATHER_CITY_DEFAULT_LON

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
fun weatherSettings(
    modifier: Modifier,
    scope: PrefsScope,
    cityLat: String,
    cityLon: String
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.weather_city)
        )
    }) {
        prefsItem {
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.WEATHER_CITY_LAT.key,
                title = stringResource(PreferenceKey.WEATHER_CITY_LAT.title),
                summary = cityLat,
                defaultValue = WEATHER_CITY_DEFAULT_LAT
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.WEATHER_CITY_LON.key,
                title = stringResource(PreferenceKey.WEATHER_CITY_LON.title),
                summary = cityLon,
                defaultValue = WEATHER_CITY_DEFAULT_LON
            )
        }
    }
}