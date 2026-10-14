package com.smsoft.smartdisplay.ui.composable.alarms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.ui.common.BarScreen
import com.smsoft.smartdisplay.ui.common.BarTitle
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The alarm time, shown as text; a tap opens a full screen with the hours and the minutes, each
 * with a button up and a button down that repeat while held. It was Android's time picker pop-up,
 * small on the 4 inch panel and a window of its own.
 */
@Composable
fun TimeChooser(
    modifier: Modifier,
    time: Int,
    onChange: (time: Int) -> Unit
) {
    var isOpen by rememberSaveable { mutableStateOf(false) }
    Text(
        modifier = Modifier
            .padding(start = 10.dp)
            .clickable(onClick = {
                isOpen = true
            }),
        text = formatAlarmTime(time),
        style = MaterialTheme.typography.h5,
        color = MaterialTheme.colors.primary
    )
    if (isOpen) {
        TimeScreen(
            time = time,
            onDone = {
                onChange(it)
                isOpen = false
            },
            onCancel = {
                isOpen = false
            }
        )
    }
}

@Composable
private fun TimeScreen(
    time: Int,
    onDone: (time: Int) -> Unit,
    onCancel: () -> Unit
) {
    var hours by remember { mutableIntStateOf(time / 60) }
    var minutes by remember { mutableIntStateOf(time % 60) }
    BarScreen(
        onDismissRequest = onCancel,
        title = {
            BarTitle(text = stringResource(R.string.alarm_time))
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = { onDone(hours * 60 + minutes) }) {
                Text(text = stringResource(R.string.save_button))
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimePart(
                value = hours,
                onStep = { hours = (hours + it).mod(24) }
            )
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = ":",
                style = MaterialTheme.typography.h2
            )
            TimePart(
                value = minutes,
                onStep = { minutes = (minutes + it).mod(60) }
            )
        }
    }
}

/** One part of the time with a button up and a button down. */
@Composable
private fun TimePart(
    value: Int,
    onStep: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StepButton(onStep = { onStep(1) }) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = null
            )
        }
        Text(
            text = "%02d".format(value),
            style = MaterialTheme.typography.h2,
            color = MaterialTheme.colors.primary
        )
        StepButton(onStep = { onStep(-1) }) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null
            )
        }
    }
}

/** Calls [onStep] on a press, then again and again, faster, while the press is held. */
@Composable
private fun StepButton(
    onStep: () -> Unit,
    content: @Composable () -> Unit
) {
    val currentOnStep by rememberUpdatedState(onStep)
    Box(
        modifier = Modifier
            .size(72.dp)
            .pointerInput(Unit) {
                coroutineScope {
                    awaitEachGesture {
                        awaitFirstDown()
                        currentOnStep()
                        val repeat = launch {
                            delay(REPEAT_FIRST_MS)
                            while (true) {
                                currentOnStep()
                                delay(REPEAT_NEXT_MS)
                            }
                        }
                        waitForUpOrCancellation()
                        repeat.cancel()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

private fun formatAlarmTime(time: Int) = "%d:%02d".format(time / 60, time % 60)

private const val REPEAT_FIRST_MS = 450L
private const val REPEAT_NEXT_MS = 90L