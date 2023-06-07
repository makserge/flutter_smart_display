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

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
fun pushButtonSettings(
    modifier: Modifier.Companion,
    scope: PrefsScope,
    topic: String
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.push_button)
        )
    }) {
        prefsItem {
            EditTextPref(
                modifier = modifier,
                key = PreferenceKey.PUSH_BUTTON_TOPIC.key,
                title = stringResource(PreferenceKey.PUSH_BUTTON_TOPIC.title),
                summary = topic
            )
        }
    }
}