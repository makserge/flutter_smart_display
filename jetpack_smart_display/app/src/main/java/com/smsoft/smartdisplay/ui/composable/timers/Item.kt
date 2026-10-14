package com.smsoft.smartdisplay.ui.composable.timers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.modifiers.TextAutoSizeLayoutScope
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.TimerState
import com.smsoft.smartdisplay.ui.common.COMPACT_SCREEN_DP
import com.smsoft.smartdisplay.ui.common.ItemCard
import com.smsoft.smartdisplay.data.database.entity.Timer

@Composable
fun Item(
    modifier: Modifier,
    itemTick: Long,
    itemState: TimerState,
    editMode: Boolean,
    onDelete: (item: Timer) -> Unit,
    onChangeState: (state: TimerState) -> Unit,
    onResetState: (item: Timer) -> Unit,
    onEnableModification: () -> Unit,
    onEdit: (item: Timer) -> Unit
) {
    // A narrow screen (the 4 inch panel is 360 dp) needs a smaller time and smaller buttons, or a
    // running timer's time wraps onto two lines next to its two buttons
    val compact = LocalConfiguration.current.screenWidthDp < COMPACT_SCREEN_DP
    val resetSize = if (compact) 52.dp else 70.dp
    val playSize = if (compact) 50.dp else 70.dp
    val duration = when (itemState) {
        is TimerState.Running -> itemTick
        is TimerState.Paused -> itemState.tick
        else -> itemState.timer.duration
    }
    val hours = duration / 3600
    val minutes = duration / 60 - (hours * 60)
    val seconds = duration - (hours * 3600) - (minutes * 60)
    // The time with its reset and play buttons, then the title with the edit buttons at its end
    // (ItemCard), so the time keeps its room in edit mode. A tap on the card no longer opens the
    // editor, also outside edit mode: the pencil does.
    ItemCard(
        editMode = editMode,
        editDescription = stringResource(R.string.edit_timer),
        onEnableModification = onEnableModification,
        onEdit = { onEdit(itemState.timer) },
        onDelete = { onDelete(itemState.timer) },
        rowSpacing = TIMER_ROW_SPACING,
        top = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Full size whenever it fits beside the buttons; it shrinks only if it does not
                val timeStyle = if (compact) MaterialTheme.typography.h3 else MaterialTheme.typography.h2
                val timeAutoSize = remember(timeStyle.fontSize) { TimeAutoSize(timeStyle.fontSize) }
                BasicText(
                    modifier = Modifier.weight(1F),
                    text = "%d:%02d:%02d".format(hours, minutes, seconds),
                    // Tabular figures: every digit has the same width (see TimeAutoSize)
                    style = timeStyle.copy(color = MaterialTheme.colors.primary, fontFeatureSettings = "tnum"),
                    maxLines = 1,
                    softWrap = false,
                    autoSize = timeAutoSize
                )
                // Always laid out, and only shown once the timer has started: appearing on start
                // used to push the time aside, so the row jumped
                val isIdle = itemState is TimerState.Idle
                val canStart = isIdle || (itemState is TimerState.Paused)
                // While hidden it takes no touches, or a long press on its spot did not reach the card
                Icon(
                    modifier = Modifier
                        .size(resetSize)
                        .alpha(if (isIdle) 0F else 1F)
                        .then(
                            if (isIdle) {
                                Modifier.clearAndSetSemantics { }
                            } else {
                                Modifier.clickable(
                                    role = Role.Button,
                                    onClick = {
                                        onResetState(itemState.timer)
                                    }
                                )
                            }
                        )
                        .padding(
                            end = 10.dp
                        ),
                    imageVector = Icons.Filled.Replay,
                    contentDescription = if (isIdle) null else stringResource(R.string.reset_timer_button)
                )
                Icon(
                    modifier = Modifier
                        .size(playSize)
                        .clickable(
                            role = Role.Button,
                            onClick = {
                                onChangeState(itemState)
                            }
                        ),
                    imageVector = if (canStart) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    // TalkBack said "Start timer" also on the pause button
                    contentDescription = stringResource(if (canStart) R.string.start_timer_button else R.string.pause_timer_button)
                )
            }
        },
        bottom = {
            Text(
                modifier = Modifier,
                text = itemState.timer.title,
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.primary
            )
        }
    )
}

// Keeps the trash at least 8 dp below the play/pause button's box (its glyph fills the box on the
// 8 inch panel), so a slip off pause does not delete the timer
private val TIMER_ROW_SPACING = 20.dp

// The time never gets smaller than this. It has the same room in and out of edit mode and keeps
// its full size on both panels, so this is only a guard.
private val TIME_MIN_FONT_SIZE = 24.sp

/**
 * The time's full size when it fits, else the largest that fits, found once per width and not on
 * every tick: the search lays the text out 8 or 9 times, and a running timer's text changes every
 * second. With tabular figures the width of the time depends only on its length, so the size found
 * for one value fits every other one.
 */
private class TimeAutoSize(private val maxFontSize: TextUnit) : TextAutoSize {
    private val search = TextAutoSize.StepBased(
        minFontSize = TIME_MIN_FONT_SIZE,
        maxFontSize = maxFontSize
    )
    private var key: List<Any>? = null
    private var fontSize = TextUnit.Unspecified

    override fun TextAutoSizeLayoutScope.getFontSize(
        constraints: Constraints,
        text: AnnotatedString
    ): TextUnit {
        val newKey = listOf(constraints.maxWidth, text.length, density, fontScale)
        if (newKey != key) {
            // The search alone ended a float step below the full size (47.75 of 48 sp)
            fontSize = if (!performLayout(constraints, text, maxFontSize).hasVisualOverflow) {
                maxFontSize
            } else {
                with(search) { getFontSize(constraints, text) }
            }
            key = newKey
        }
        return fontSize
    }

    override fun equals(other: Any?) = (other is TimeAutoSize) && (other.maxFontSize == maxFontSize)

    override fun hashCode() = maxFontSize.hashCode()
}