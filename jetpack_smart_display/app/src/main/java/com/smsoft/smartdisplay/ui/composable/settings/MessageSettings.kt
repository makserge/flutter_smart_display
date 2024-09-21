package com.smsoft.smartdisplay.ui.composable.settings

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScope
import com.jamal.composeprefs.ui.prefs.EditTextPref
import com.jamal.composeprefs.ui.prefs.SliderPref
import com.jamal.composeprefs.ui.prefs.SwitchPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
fun messageSettings(
    modifier: Modifier,
    scope: PrefsScope,
    messageTopic: String
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.message)
        )
    }) {
        prefsItem {
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.MESSAGE_ENABLED.key,
                title = stringResource(PreferenceKey.MESSAGE_ENABLED.title),
                defaultChecked = MESSAGE_ENABLED_DEFAULT
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.MESSAGE_TIMEOUT.key,
                title = stringResource(PreferenceKey.MESSAGE_TIMEOUT.title),
                valueRange = 0.1F..1F,
                defaultValue = MESSAGE_TIMEOUT_DEFAULT
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.MESSAGE_SOUND_VOLUME.key,
                title = stringResource(PreferenceKey.MESSAGE_SOUND_VOLUME.title),
                valueRange = 0.1F..1F,
                defaultValue = MESSAGE_SOUND_VOLUME_DEFAULT
            )
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.MESSAGE_TOPIC.key,
                title = stringResource(PreferenceKey.MESSAGE_TOPIC.title),
                summary = messageTopic,
                defaultValue = MESSAGE_DEFAULT_TOPIC
            )
        }
    }
}

const val MESSAGE_ENABLED_DEFAULT = false
const val MESSAGE_SOUND_VOLUME_DEFAULT = 1F
const val MESSAGE_TIMEOUT_DEFAULT = 0.5F
const val MESSAGE_DEFAULT_TOPIC = "message"