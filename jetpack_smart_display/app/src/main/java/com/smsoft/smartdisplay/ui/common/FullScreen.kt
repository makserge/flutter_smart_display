package com.smsoft.smartdisplay.ui.common

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified

/*
 * Full screens instead of pop-up windows. An editor, a chooser, a question or an alert covers the
 * whole app while it is shown, inside the app's own window:
 * - on the 4 inch panel a pop-up left little room, and the on-screen keyboard covered its buttons;
 * - every pop-up is a window of its own, and on Android 8 each one brought back the status and
 *   navigation bars that the app hides;
 * - a full screen keeps its title and its buttons in a bar at the top, above the keyboard.
 */

/** The full screens shown over the app, the last one on top. See [FullScreenHost]. */
@Stable
class FullScreens internal constructor() {
    internal val entries = mutableStateListOf<FullScreenEntry>()
}

internal class FullScreenEntry(content: @Composable () -> Unit) {
    var content by mutableStateOf(content)
}

private val LocalFullScreens = staticCompositionLocalOf<FullScreens?> { null }

/**
 * Holds [content] (the whole app) and draws the full screens over it. Used once, at the root.
 * [onKeyboardHidden] runs each time the on-screen keyboard has gone: Android 8 shows the system
 * bars with it and leaves them shown after it.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FullScreenHost(
    onKeyboardHidden: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val isKeyboardShown = WindowInsets.isImeVisible
    val currentOnKeyboardHidden by rememberUpdatedState(onKeyboardHidden)
    LaunchedEffect(isKeyboardShown) {
        if (!isKeyboardShown) {
            currentOnKeyboardHidden()
        }
    }
    val screens = remember { FullScreens() }
    CompositionLocalProvider(LocalFullScreens provides screens) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
            screens.entries.forEach { entry ->
                key(entry) {
                    entry.content()
                }
            }
        }
    }
}

/**
 * Shows [content] over the whole app while this composable is in the composition, the way a
 * Dialog shows while it is. [content] fills the screen and takes every touch, so nothing behind it
 * reacts. The back key (where there is one) calls [onDismissRequest]. Not [opaque]: the app shows
 * through, dimmed.
 */
@Composable
fun FullScreen(
    onDismissRequest: () -> Unit,
    opaque: Boolean = true,
    content: @Composable () -> Unit
) {
    val screens = LocalFullScreens.current ?: return
    val screen: @Composable () -> Unit = {
        BackHandler(onBack = onDismissRequest)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (opaque) MaterialTheme.colors.background else SCRIM_COLOR)
                // Touches stop here instead of reaching the page behind
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            content()
        }
    }
    val entry = remember { FullScreenEntry(screen) }
    SideEffect {
        entry.content = screen
    }
    DisposableEffect(screens, entry) {
        screens.entries.add(entry)
        onDispose {
            screens.entries.remove(entry)
        }
    }
}

/**
 * A full screen with a bar at the top: [dismissButton], the [title], and [confirmButton], then
 * [content], which scrolls (with a scroll bar) and stays above the on-screen keyboard while the
 * bar stays in place. On a compact screen (the 4 inch panel) the text is [COMPACT_TEXT_SCALE]
 * times the normal size; on a larger one the content is at most [MAX_CONTENT_WIDTH] wide.
 */
@Composable
fun BarScreen(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    confirmButton: (@Composable () -> Unit)? = null,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    FullScreen(onDismissRequest = onDismissRequest) {
        ScaledForScreen {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dismissButton()
                    Box(
                        modifier = Modifier
                            .weight(1F)
                            .padding(horizontal = 8.dp)
                    ) {
                        ProvideTextStyle(MaterialTheme.typography.h6) {
                            title()
                        }
                    }
                    confirmButton?.invoke()
                }
                Divider()
                Box(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .widthIn(max = MAX_CONTENT_WIDTH)
                            .fillMaxWidth()
                            .then(
                                if (scrollable) {
                                    Modifier
                                        .verticalScrollbar(scrollState)
                                        .verticalScroll(scrollState)
                                } else {
                                    Modifier
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        // No colour in the style: it would also paint the placeholders of the
                        // text fields like entered text (the surface gives the text colour).
                        // Left-aligned: the app's body text, which this style is merged into, is
                        // centred, and so were the text fields.
                        ProvideTextStyle(MaterialTheme.typography.subtitle1.copy(textAlign = TextAlign.Start)) {
                            content()
                        }
                    }
                }
            }
        }
    }
}

/**
 * A full screen with [content] in the middle, for an alert (an alarm or a timer going off, a
 * message). A tap anywhere calls [onTap], if given. Not [opaque] (the voice assistant listening):
 * the page shows through, dimmed, around [content].
 */
@Composable
fun AlertScreen(
    onDismissRequest: () -> Unit,
    onTap: (() -> Unit)? = null,
    opaque: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    FullScreen(onDismissRequest = onDismissRequest, opaque = opaque) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (onTap != null) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onTap
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

/** A line of text as the title of a [BarScreen], cut with an ellipsis when too long. */
@Composable
fun BarTitle(
    text: String
) {
    Text(
        text = text,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

/** [content] with the text [COMPACT_TEXT_SCALE] times larger on a compact screen. */
@Composable
fun ScaledForScreen(
    content: @Composable () -> Unit
) {
    val typography = MaterialTheme.typography
    val scale = if (LocalConfiguration.current.screenWidthDp < COMPACT_SCREEN_DP) COMPACT_TEXT_SCALE else 1F
    MaterialTheme(typography = remember(typography, scale) { typography.scaled(scale) }) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            content()
        }
    }
}

private fun Typography.scaled(factor: Float) = if (factor == 1F) this else copy(
    h1 = h1.scaled(factor),
    h2 = h2.scaled(factor),
    h3 = h3.scaled(factor),
    h4 = h4.scaled(factor),
    h5 = h5.scaled(factor),
    h6 = h6.scaled(factor),
    subtitle1 = subtitle1.scaled(factor),
    subtitle2 = subtitle2.scaled(factor),
    body1 = body1.scaled(factor),
    body2 = body2.scaled(factor),
    button = button.scaled(factor),
    caption = caption.scaled(factor),
    overline = overline.scaled(factor)
)

private fun TextStyle.scaled(factor: Float) = copy(
    fontSize = if (fontSize.isSpecified) fontSize * factor else fontSize,
    lineHeight = if (lineHeight.isSpecified) lineHeight * factor else lineHeight
)

// Over the app behind a screen that is not opaque, as behind a dialog
private val SCRIM_COLOR = Color.Black.copy(alpha = 0.6F)
// The text on a compact screen (the 4 inch panel), per normal size
private const val COMPACT_TEXT_SCALE = 1.3F
// The widest content, so the rows do not stretch across the 8 inch panel
private val MAX_CONTENT_WIDTH = 720.dp
