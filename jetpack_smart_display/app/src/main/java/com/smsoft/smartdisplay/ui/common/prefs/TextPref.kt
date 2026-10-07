package com.smsoft.smartdisplay.ui.common.prefs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * Basic preference row (title, optional summary, optional trailing content).
 * Like ComposePrefs, [enabled] defaults to false: the row is only clickable when enabled.
 */
@Composable
fun TextPref(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    darkenOnDisable: Boolean = false,
    minimalHeight: Boolean = false,
    onClick: () -> Unit = {},
    textColor: Color = MaterialTheme.colors.onBackground,
    enabled: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    PrefsListItem(
        modifier = if (enabled) modifier.clickable(onClick = onClick) else modifier,
        enabled = enabled,
        darkenOnDisable = darkenOnDisable,
        icon = leadingIcon,
        secondaryText = if (summary != null) {
            { Text(text = summary) }
        } else {
            null
        },
        trailing = trailingContent,
        textColor = textColor,
        minimalHeight = minimalHeight,
        text = { Text(text = title) },
    )
}

@Composable
internal fun PrefsListItem(
    modifier: Modifier,
    enabled: Boolean,
    darkenOnDisable: Boolean,
    icon: @Composable (() -> Unit)?,
    secondaryText: @Composable (() -> Unit)?,
    trailing: @Composable (() -> Unit)?,
    textColor: Color,
    minimalHeight: Boolean,
    text: @Composable () -> Unit,
) {
    val typography = MaterialTheme.typography
    val dimmed = !enabled && darkenOnDisable
    Row(
        modifier = modifier
            .heightIn(min = if (minimalHeight) 32.dp else 48.dp)
            .padding(
                start = 16.dp,
                top = if (secondaryText == null && !minimalHeight) 4.dp else 12.dp,
                end = 16.dp,
                bottom = when {
                    minimalHeight -> 0.dp
                    secondaryText == null -> 4.dp
                    else -> 12.dp
                },
            )
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.sizeIn(minWidth = 40.dp, minHeight = 40.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                icon()
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            StyledContent(typography.subtitle1, textColor, if (dimmed) ContentAlpha.disabled else ContentAlpha.high, text)
            if (secondaryText != null) {
                StyledContent(typography.body2, textColor, if (dimmed) ContentAlpha.disabled else ContentAlpha.medium, secondaryText)
            }
        }
        if (trailing != null) {
            StyledContent(typography.caption, textColor, if (dimmed) ContentAlpha.disabled else ContentAlpha.high, trailing)
        }
    }
}

@Composable
private fun StyledContent(
    style: TextStyle,
    color: Color,
    alpha: Float,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalContentAlpha provides alpha,
        LocalTextStyle provides style.copy(color = color.copy(alpha = alpha)),
        content = content,
    )
}
