package com.smsoft.smartdisplay.ui.composable.settings

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.smsoft.smartdisplay.ui.common.prefs.GroupHeader
import com.smsoft.smartdisplay.ui.common.prefs.PrefsScope
import com.smsoft.smartdisplay.ui.common.prefs.EditTextPref
import com.smsoft.smartdisplay.ui.common.prefs.SliderPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.screen.doorbell.isRtspsUrl
import com.smsoft.smartdisplay.ui.screen.settings.DOORBELL_ALARM_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.screen.settings.DOORBELL_BACK_TIMER_DEFAULT_DELAY
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
            // libVLC 3 cannot play rtsps://, the doorbell just stays black: the edit dialog says
            // so, and so does the summary while such a URL is set.
            val rtspsHint = stringResource(R.string.doorbell_stream_url_rtsps)
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.DOORBELL_STREAM_URL.key,
                title = stringResource(PreferenceKey.DOORBELL_STREAM_URL.title),
                summary = if (isRtspsUrl(streamURL)) "$streamURL\n$rtspsHint" else streamURL,
                dialogMessage = rtspsHint,
                defaultValue = DOORBELL_STREAM_DEFAULT_URL
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DOORBELL_BACK_TIMER_DELAY.key,
                title = stringResource(PreferenceKey.DOORBELL_BACK_TIMER_DELAY.title),
                valueRange = 5F..300F,
                showValue = true,
                defaultValue = DOORBELL_BACK_TIMER_DEFAULT_DELAY
            )
        }
    }
}