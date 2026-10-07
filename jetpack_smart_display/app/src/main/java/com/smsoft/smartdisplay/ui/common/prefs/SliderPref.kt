package com.smsoft.smartdisplay.ui.common.prefs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Float preference. Storage: `floatPreferencesKey(key)` holding the slider value (no rounding),
 * written once when the gesture ends (onValueChangeFinished), not on every drag step.
 * [toStored] and [fromStored] map between the thumb position (in [valueRange]) and the stored
 * value, e.g. for a volume slider that is even in loudness; [defaultValue] and the value passed
 * to [onValueChangeFinished] are stored values. Both are the identity by default.
 */
@Composable
fun SliderPref(
    key: String,
    title: String,
    modifier: Modifier = Modifier,
    defaultValue: Float = 0f,
    onValueChangeFinished: ((Float) -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    showValue: Boolean = false,
    steps: Int = 0,
    textColor: Color = MaterialTheme.colors.onBackground,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    toStored: (Float) -> Float = { it },
    fromStored: (Float) -> Float = { it },
) {
    val selectionKey = remember(key) { floatPreferencesKey(key) }
    val scope = rememberCoroutineScope()
    val dataStore = LocalPrefsDataStore.current

    // Local thumb position: follows the stored value, written back only on gesture end.
    var value by remember(key) { mutableFloatStateOf(fromStored(defaultValue)) }
    // The collector outlives recompositions: it reads the mapping of the latest one
    val currentFromStored by rememberUpdatedState(fromStored)
    LaunchedEffect(dataStore, selectionKey) {
        dataStore.data
            .map { it[selectionKey] }
            .distinctUntilChanged() // writes of other keys must not snap the thumb back mid-drag
            .collect { stored -> if (stored != null) value = currentFromStored(stored) }
    }

    Column(verticalArrangement = Arrangement.Center) {
        TextPref(
            title = title,
            modifier = modifier,
            minimalHeight = true,
            textColor = textColor,
            leadingIcon = leadingIcon,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Slider(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier
                    .weight(2.1f)
                    .padding(start = 16.dp, end = 16.dp),
                enabled = enabled,
                valueRange = valueRange,
                steps = steps,
                onValueChangeFinished = {
                    val newValue = toStored(value)
                    scope.launchPrefWrite(key) {
                        dataStore.edit { it[selectionKey] = newValue }
                        onValueChangeFinished?.invoke(newValue)
                    }
                },
            )
            if (showValue) {
                Text(
                    text = roundToDecimals(toStored(value), 2).toString(),
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(start = 8.dp),
                    color = textColor,
                )
            }
        }
    }
}

/** Same rounding as ComposePrefs' roundToDP (HALF_EVEN), used only for the displayed value. */
private fun roundToDecimals(value: Float, places: Int): Float =
    BigDecimal(value.toDouble()).setScale(places, RoundingMode.HALF_EVEN).toFloat()
