package com.smsoft.smartdisplay.ui.composable.settings

import android.content.Context
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScope
import com.jamal.composeprefs.ui.prefs.EditTextPref
import com.jamal.composeprefs.ui.prefs.ListPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.RadioType
import com.smsoft.smartdisplay.ui.screen.settings.MPD_SERVER_DEFAULT_HOST
import com.smsoft.smartdisplay.ui.screen.settings.MPD_SERVER_DEFAULT_PORT

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
fun radioSettings(
    modifier: Modifier,
    context: Context,
    scope: PrefsScope,
    host: String,
    port: String,
    password: String
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.radio)
        )
    }) {
        prefsItem {
            ListPref(
                modifier = modifier,
                key = PreferenceKey.RADIO_TYPE.key,
                title = stringResource(PreferenceKey.RADIO_TYPE.title),
                defaultValue = RadioType.getDefaultId(),
                useSelectedAsSummary = true,
                entries = RadioType.toMap(context)
            )
        }
        prefsGroup({
            GroupHeader(
                title = stringResource(R.string.mpd_server)
            )
        }) {
            prefsItem {
                EditTextPref(
                    modifier = modifier,
                    key = PreferenceKey.MPD_SERVER_HOST.key,
                    title = stringResource(PreferenceKey.MPD_SERVER_HOST.title),
                    defaultValue = MPD_SERVER_DEFAULT_HOST,
                    summary = host
                )
                EditTextPref(
                    modifier = modifier,
                    key = PreferenceKey.MPD_SERVER_PORT.key,
                    title = stringResource(PreferenceKey.MPD_SERVER_PORT.title),
                    defaultValue = MPD_SERVER_DEFAULT_PORT,
                    summary = port
                )
                EditTextPref(
                    modifier = modifier,
                    key = PreferenceKey.MPD_SERVER_PASSWORD.key,
                    title = stringResource(PreferenceKey.MPD_SERVER_PASSWORD.title),
                    summary = password
                )
            }
        }
    }
}