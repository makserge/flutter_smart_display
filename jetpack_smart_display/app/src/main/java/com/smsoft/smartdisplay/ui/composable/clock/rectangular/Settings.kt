package com.smsoft.smartdisplay.ui.composable.clock.rectangular

import android.content.Context
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScope
import com.jamal.composeprefs.ui.prefs.ListPref
import com.jamal.composeprefs.ui.prefs.SliderPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.Font
import com.smsoft.smartdisplay.data.PreferenceKey

fun analogRectangularPrefs(
    modifier: Modifier,
    scope: PrefsScope,
    context: Context
) {
    drawDigitsRectangular(
        modifier = modifier,
        scope = scope,
        context = context
    )
    drawHandsRectangular(
        modifier = modifier,
        scope = scope
    )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
fun drawDigitsRectangular(
    modifier: Modifier,
    scope: PrefsScope,
    context: Context
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.digits)
        )
    }) {
        prefsItem {
            ListPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_FONT_AR.key,
                title = stringResource(PreferenceKey.DIGIT_FONT_AR.title),
                defaultValue = Font.getDefault().toString(),
                useSelectedAsSummary = true,
                entries = Font.toMap(context),
            )
        }
        prefsItem {
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.DIGIT_FONT_SIZE_AR.key,
                title = stringResource(PreferenceKey.DIGIT_FONT_SIZE_AR.title),
                defaultValue = DEFAULT_DIGIT_FONT_SIZE_AR,
                valueRange = 60F..120F,
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
fun drawHandsRectangular(
    modifier: Modifier,
    scope: PrefsScope
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.hands)
        )
    }) {
        prefsItem {
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_LENGTH_MINUTES_AR.key,
                title = stringResource(PreferenceKey.HAND_LENGTH_MINUTES_AR.title),
                defaultValue = DEFAULT_HAND_LEN_MINUTES_AR,
                valueRange = 0.3F..1F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_LENGTH_HOURS_AR.key,
                title = stringResource(PreferenceKey.HAND_LENGTH_HOURS_AR.title),
                defaultValue = DEFAULT_HAND_LEN_HOURS_AR,
                valueRange = 0.2F..0.8F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_WIDTH_MINUTES_AR.key,
                title = stringResource(PreferenceKey.HAND_WIDTH_MINUTES_AR.title),
                defaultValue = DEFAULT_HAND_WIDTH_MINUTES_AR,
                valueRange = 8F..20F,
            )
            SliderPref(
                modifier = modifier,
                key = PreferenceKey.HAND_WIDTH_HOURS_AR.key,
                title = stringResource(PreferenceKey.HAND_WIDTH_HOURS_AR.title),
                defaultValue = DEFAULT_HAND_WIDTH_HOURS_AR,
                valueRange = 5F..15F,
            )
        }
    }
}