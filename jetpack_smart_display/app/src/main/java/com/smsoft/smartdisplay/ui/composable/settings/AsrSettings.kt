package com.smsoft.smartdisplay.ui.composable.settings

import android.content.Context
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.smsoft.smartdisplay.ui.common.prefs.GroupHeader
import com.smsoft.smartdisplay.ui.common.prefs.PrefsScope
import com.smsoft.smartdisplay.ui.common.prefs.ListPref
import com.smsoft.smartdisplay.ui.common.prefs.SliderPref
import com.smsoft.smartdisplay.ui.common.prefs.SwitchPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.AsrWakeWord
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.utils.VOLUME_SETTING_SCALE

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
fun asrSettings(
    modifier: Modifier.Companion,
    scope: PrefsScope,
    context: Context,
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.asr)
        )
    }) {
        prefsItem {
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.ASR_ENABLED.key,
                title = stringResource(PreferenceKey.ASR_ENABLED.title)
            )
            ListPref(
                modifier = Modifier,
                key = PreferenceKey.ASR_WAKE_WORD.key,
                title = stringResource(PreferenceKey.ASR_WAKE_WORD.title),
                defaultValue = AsrWakeWord.getDefaultId(),
                useSelectedAsSummary = true,
                entries = AsrWakeWord.toMap(context),
            )
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.ASR_SOUND_ENABLED.key,
                title = stringResource(PreferenceKey.ASR_SOUND_ENABLED.title),
                defaultChecked = ASR_SOUND_ENABLED_DEFAULT
            )
            // Stores the player's gain (0.1..1) as before; the slider is even in loudness
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.ASR_SOUND_VOLUME.key,
                title = stringResource(PreferenceKey.ASR_SOUND_VOLUME.title),
                valueRange = 0F..1F,
                defaultValue = ASR_SOUND_VOLUME_DEFAULT,
                toStored = VOLUME_SETTING_SCALE::volume,
                fromStored = VOLUME_SETTING_SCALE::position
            )
        }
    }
}

const val ASR_SOUND_ENABLED_DEFAULT = true
const val ASR_SOUND_VOLUME_DEFAULT = 1F