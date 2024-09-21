package com.smsoft.smartdisplay.ui.composable.settings

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScope
import com.jamal.composeprefs.ui.prefs.SliderPref
import com.jamal.composeprefs.ui.prefs.SwitchPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey

@OptIn(ExperimentalMaterialApi::class)
fun timerSettings(
    modifier: Modifier.Companion,
    scope: PrefsScope
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.timer)
        )
    }) {
        prefsItem {
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.TIMER_ASR_ENABLED.key,
                title = stringResource(PreferenceKey.TIMER_ASR_ENABLED.title),
                defaultChecked = TIMER_ASR_ENABLED_DEFAULT
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.TIMER_TIMEOUT.key,
                title = stringResource(PreferenceKey.TIMER_TIMEOUT.title),
                valueRange = 0.1F..1F,
                defaultValue = TIMER_TIMEOUT_DEFAULT
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.TIMER_SOUND_VOLUME.key,
                title = stringResource(PreferenceKey.TIMER_SOUND_VOLUME.title),
                valueRange = 0.1F..1F,
                defaultValue = TIMER_SOUND_VOLUME_DEFAULT
            )
        }
    }
}

const val TIMER_ASR_ENABLED_DEFAULT = false
const val TIMER_SOUND_VOLUME_DEFAULT = 1F
const val TIMER_TIMEOUT_DEFAULT = 0.5F