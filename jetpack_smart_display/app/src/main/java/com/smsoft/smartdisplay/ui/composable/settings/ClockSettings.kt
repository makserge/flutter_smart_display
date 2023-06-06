package com.smsoft.smartdisplay.ui.composable.settings

import android.content.Context
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScope
import com.jamal.composeprefs.ui.prefs.ListPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.ClockType
import com.smsoft.smartdisplay.data.PreferenceKey

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
fun clockSettings(
    modifier: Modifier,
    scope: PrefsScope,
    context: Context,
    clockType: ClockType
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.clock)
        )
    }) {
        prefsItem {
            ListPref(
                modifier = Modifier,
                key = PreferenceKey.CLOCK_TYPE.key,
                title = stringResource(PreferenceKey.CLOCK_TYPE.title),
                defaultValue = ClockType.getDefaultId(),
                useSelectedAsSummary = true,
                entries = ClockType.toMap(context),
            )
        }
        colorPrefs(
            modifier = modifier,
            scope = this
        )
        clockTypePrefs(
            modifier = modifier,
            clockType = clockType,
            scope = this,
            context = context
        )
    }
}