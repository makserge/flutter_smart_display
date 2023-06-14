package com.smsoft.smartdisplay.ui.composable.settings

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScope
import com.jamal.composeprefs.ui.prefs.SwitchPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey

@OptIn(ExperimentalMaterialApi::class)
fun asrSettings(
    modifier: Modifier.Companion,
    scope: PrefsScope,
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
        }
    }
}