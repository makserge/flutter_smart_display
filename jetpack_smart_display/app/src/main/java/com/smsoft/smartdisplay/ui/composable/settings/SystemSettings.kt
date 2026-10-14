package com.smsoft.smartdisplay.ui.composable.settings

import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.ui.common.prefs.GroupHeader
import com.smsoft.smartdisplay.ui.common.prefs.PrefsScope
import com.smsoft.smartdisplay.ui.common.prefs.TextPref

/** "Open home screen": the way to the panel's own launcher (see utils/HomeApps.kt). */
fun systemSettings(
    modifier: Modifier,
    scope: PrefsScope,
    onOpenHome: () -> Unit
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.system)
        )
    }) {
        prefsItem {
            TextPref(
                modifier = modifier,
                title = stringResource(R.string.open_home),
                summary = stringResource(R.string.open_home_summary),
                enabled = true,
                onClick = onOpenHome
            )
        }
    }
}
