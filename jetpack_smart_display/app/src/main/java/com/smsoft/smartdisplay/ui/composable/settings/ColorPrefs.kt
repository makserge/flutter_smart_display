package com.smsoft.smartdisplay.ui.composable.settings

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScope
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.PreferenceKey

fun colorPrefs(
    modifier: Modifier,
    scope: PrefsScope
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.colors)
        )
    }) {
        prefsItem {
            ColorPickerPref(
                modifier = modifier,
                key = PreferenceKey.PRIMARY_COLOR.key,
                title = stringResource(PreferenceKey.PRIMARY_COLOR.title),
                defaultValue = MaterialTheme.colors.primary
            )
            ColorPickerPref(
                modifier = modifier,
                key = PreferenceKey.SECONDARY_COLOR.key,
                title = stringResource(PreferenceKey.SECONDARY_COLOR.title),
                defaultValue = MaterialTheme.colors.secondary
            )
        }
    }
}