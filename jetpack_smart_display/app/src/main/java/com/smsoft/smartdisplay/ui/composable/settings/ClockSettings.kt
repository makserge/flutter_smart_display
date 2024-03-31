package com.smsoft.smartdisplay.ui.composable.settings

import android.content.Context
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScope
import com.jamal.composeprefs.ui.prefs.ListPref
import com.jamal.composeprefs.ui.prefs.SliderPref
import com.jamal.composeprefs.ui.prefs.SwitchPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.ClockType
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.screen.settings.CLOCK_AUTO_RETURN_TIMEOUT_DEFAULT

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
            SwitchPref(
                modifier = modifier,
                key = PreferenceKey.CLOCK_AUTO_RETURN.key,
                title = stringResource(PreferenceKey.CLOCK_AUTO_RETURN.title)
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.CLOCK_AUTO_RETURN_TIMEOUT.key,
                title = stringResource(PreferenceKey.CLOCK_AUTO_RETURN_TIMEOUT.title),
                valueRange = 1.0F..15.0F,
                defaultValue = CLOCK_AUTO_RETURN_TIMEOUT_DEFAULT
            )
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