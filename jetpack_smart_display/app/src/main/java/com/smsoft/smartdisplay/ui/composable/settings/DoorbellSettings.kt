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
import com.smsoft.smartdisplay.ui.screen.settings.DOORBELL_ALARM_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.screen.settings.DOORBELL_STREAM_DEFAULT_URL

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
fun doorbellSettings(
    modifier: Modifier,
    scope: PrefsScope,
    alarmTopic: String,
    streamURL: String
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.doorbell)
        )
    }) {
        prefsItem {
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.DOORBELL_ALARM_TOPIC.key,
                title = stringResource(PreferenceKey.DOORBELL_ALARM_TOPIC.title),
                summary = alarmTopic,
                defaultValue = DOORBELL_ALARM_DEFAULT_TOPIC
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.DOORBELL_STREAM_URL.key,
                title = stringResource(PreferenceKey.DOORBELL_STREAM_URL.title),
                summary = streamURL,
                defaultValue = DOORBELL_STREAM_DEFAULT_URL
            )
        }
    }
}
//topic:
//url: