package com.smsoft.smartdisplay.ui.composable.settings

import android.content.Context
import androidx.compose.ui.Modifier
import com.jamal.composeprefs.ui.PrefsScope
import com.smsoft.smartdisplay.data.ClockType
import com.smsoft.smartdisplay.ui.composable.clock.clockview.analogClockViewPrefs
import com.smsoft.smartdisplay.ui.composable.clock.clockview2.analogClockView2Prefs
import com.smsoft.smartdisplay.ui.composable.clock.digitalclock.digitalClockPrefs
import com.smsoft.smartdisplay.ui.composable.clock.digitalclock2.digitalClockPrefs2
import com.smsoft.smartdisplay.ui.composable.clock.digitalmatrixclock.digitalMatrixClockPrefs
import com.smsoft.smartdisplay.ui.composable.clock.jetalarm.analogJetAlarmPrefs
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.analogNightdreamPrefs
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.digitalFlipClockPrefs
import com.smsoft.smartdisplay.ui.composable.clock.rectangular.analogRectangularPrefs

fun clockTypePrefs(
    modifier: Modifier,
    clockType: ClockType,
    scope: PrefsScope,
    context: Context
) {
    when (clockType) {
        ClockType.ANALOG_NIGHTDREAM -> analogNightdreamPrefs(
            modifier = modifier,
            scope = scope,
            context = context
        )
        ClockType.ANALOG_CLOCKVIEW -> analogClockViewPrefs(
            modifier = modifier,
            scope = scope,
            context = context
        )
        ClockType.ANALOG_CLOCKVIEW2 -> analogClockView2Prefs(
            modifier = modifier,
            scope = scope,
            context = context
        )
        ClockType.ANALOG_RECTANGULAR -> analogRectangularPrefs(
            modifier = modifier,
            scope = scope,
            context = context
        )
        ClockType.ANALOG_JETALARM -> analogJetAlarmPrefs(
            modifier = modifier,
            scope = scope
        )
        ClockType.ANALOG_FSCLOCK -> {}
        ClockType.DIGITAL_FLIPCLOCK -> digitalFlipClockPrefs(
            modifier = modifier,
            scope = scope
        )
        ClockType.DIGITAL_MATRIXCLOCK -> digitalMatrixClockPrefs(
            modifier = modifier,
            scope = scope,
            context = context
        )
        ClockType.DIGITAL_CLOCK -> digitalClockPrefs(
            modifier = modifier,
            scope = scope,
            context = context
        )
        ClockType.DIGITAL_CLOCK2 -> digitalClockPrefs2(
            modifier = modifier,
            scope = scope
        )
    }
}