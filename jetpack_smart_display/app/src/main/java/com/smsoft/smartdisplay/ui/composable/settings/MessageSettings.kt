package com.smsoft.smartdisplay.ui.composable.settings

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.smsoft.smartdisplay.ui.common.prefs.GroupHeader
import com.smsoft.smartdisplay.ui.common.prefs.PrefsScope
import com.smsoft.smartdisplay.ui.common.prefs.EditTextPref
import com.smsoft.smartdisplay.ui.common.prefs.SliderPref
import com.smsoft.smartdisplay.ui.common.prefs.SwitchPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.utils.VOLUME_SETTING_SCALE

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
            // Stores the player's gain (0.1..1) as before; the slider is even in loudness
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.MESSAGE_SOUND_VOLUME.key,
                title = stringResource(PreferenceKey.MESSAGE_SOUND_VOLUME.title),
                valueRange = 0F..1F,
                defaultValue = MESSAGE_SOUND_VOLUME_DEFAULT,
                toStored = VOLUME_SETTING_SCALE::volume,
                fromStored = VOLUME_SETTING_SCALE::position
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