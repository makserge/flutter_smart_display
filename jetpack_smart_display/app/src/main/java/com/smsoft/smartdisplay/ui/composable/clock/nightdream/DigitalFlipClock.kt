package com.smsoft.smartdisplay.ui.composable.clock.nightdream

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.smsoft.smartdisplay.utils.getStateFromFlow
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.digit.TabDigit
import com.smsoft.smartdisplay.ui.screen.clock.ClockViewModel
import kotlin.math.min

@Composable
fun DigitalFlipClock(
    modifier: Modifier = Modifier,
    viewModel: ClockViewModel,
    scale: Float,
    primaryColor: Color,
    secondaryColor: Color,
    hour: Int,
    minute: Int,
    is24Hour: Boolean
) {
    val primaryColorArgb = primaryColor.toArgb()
    val secondaryColorArgb = secondaryColor.toArgb()

    val reverseRotation = getStateFromFlow(
        flow = viewModel.reverseRotationFC,
        defaultValue = DEFAULT_REVERSE_ROTATION_FC
    ) as Boolean

    // Padding of a card in px at the default 440 px text; it is scaled with the text below
    val padding = getStateFromFlow(
        flow = viewModel.paddingFC,
        defaultValue = DEFAULT_PADDING_FC
    ) as Float

    // Slider 200..450: share of the largest digit size that fits the page (450 = all of it)
    val fontSize = getStateFromFlow(
        flow = viewModel.fontSizeFC,
        defaultValue = DEFAULT_TEXT_SIZE_FC
    ) as Float

    val digitSizeRatio = remember { TabDigit.digitSizeRatio() }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(all = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = constraints.maxWidth.toFloat()
        val availableHeight = constraints.maxHeight.toFloat()
        // Portrait: hours above minutes, which gives about twice the digit size of one row
        val stacked = availableHeight > availableWidth

        // Everything below is per 1 px of text size, so one division gives the size that fits
        val paddingRatio = padding / DEFAULT_TEXT_SIZE_FC
        val slotWidthRatio = (digitSizeRatio.x + paddingRatio) * TabDigit.SLOT_WIDTH_RATIO
        val slotHeightRatio = (digitSizeRatio.y + paddingRatio) * TabDigit.SLOT_HEIGHT_RATIO
        val fittingTextSize = if (stacked) {
            min(
                availableWidth / (2 * slotWidthRatio),
                availableHeight / (2 * slotHeightRatio + ROW_GAP_RATIO)
            )
        } else {
            min(
                availableWidth / (4 * slotWidthRatio + COLON_WIDTH_RATIO),
                availableHeight / slotHeightRatio
            )
        }
        val sizeFactor = (fontSize / MAX_TEXT_SIZE_FC).coerceIn(0.1F, 1F) * scale.coerceIn(0.1F, 1F)
        val textSize = (fittingTextSize * sizeFactor).coerceIn(1F, MAX_TEXT_PX)
        val cardPadding = padding * textSize / DEFAULT_TEXT_SIZE_FC

        // Exact sizes, so the Row can never squeeze a digit and the group always fits and stays centred
        val digitModifier = with(density) {
            Modifier.size(
                width = (textSize * slotWidthRatio).toDp(),
                height = (textSize * slotHeightRatio).toDp()
            )
        }

        val (hourTens, hourUnits) = hourCards(hour, is24Hour)
        val hours = @Composable {
            // TabDigit takes its characters only when it is created: a 12/24-hour switch
            // recreates the two cards, which then show the hour at once without a flip
            key(is24Hour) {
                FlipDigit(
                    modifier = digitModifier,
                    chars = hourTens.chars,
                    index = hourTens.index,
                    textSize = textSize,
                    padding = cardPadding,
                    reverseRotation = reverseRotation,
                    textColor = primaryColorArgb,
                    dividerColor = secondaryColorArgb
                )
                FlipDigit(
                    modifier = digitModifier,
                    chars = hourUnits.chars,
                    index = hourUnits.index,
                    textSize = textSize,
                    padding = cardPadding,
                    reverseRotation = reverseRotation,
                    textColor = primaryColorArgb,
                    dividerColor = secondaryColorArgb
                )
            }
        }
        val minutes = @Composable {
            FlipDigit(
                modifier = digitModifier,
                chars = HIGH_MINUTES,
                index = minute / 10,
                textSize = textSize,
                padding = cardPadding,
                reverseRotation = reverseRotation,
                textColor = primaryColorArgb,
                dividerColor = secondaryColorArgb
            )
            FlipDigit(
                modifier = digitModifier,
                chars = LOW_MINUTES,
                index = minute % 10,
                textSize = textSize,
                padding = cardPadding,
                reverseRotation = reverseRotation,
                textColor = primaryColorArgb,
                dividerColor = secondaryColorArgb
            )
        }

        if (stacked) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    with(density) { (textSize * ROW_GAP_RATIO).toDp() }
                )
            ) {
                Row { hours() }
                Row { minutes() }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                hours()
                FlipColon(
                    modifier = with(density) {
                        Modifier.size(
                            width = (textSize * COLON_WIDTH_RATIO).toDp(),
                            height = (textSize * slotHeightRatio).toDp()
                        )
                    },
                    color = primaryColor,
                    textSize = textSize
                )
                minutes()
            }
        }
    }
}

@Composable
private fun FlipDigit(
    modifier: Modifier,
    chars: CharArray,
    index: Int,
    textSize: Float,
    padding: Float,
    reverseRotation: Boolean,
    textColor: Int,
    dividerColor: Int
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TabDigit(context).apply {
                this.chars = chars
                this.textSize = textSize
                this.textColor = textColor
                setChar(index)
            }
        },
        update = { view ->
            view.reverseRotation = reverseRotation
            view.cornerSize = DEFAULT_CORNER_SIZE
            view.background = DEFAULT_BACKGROUND
            view.padding = padding
            view.textSize = textSize
            view.dividerColor = dividerColor
            view.textColor = textColor
            // A single step flips, any other change jumps, so the digit is always the current time
            view.flipTo(index)
        }
    )
}

@Composable
private fun FlipColon(
    modifier: Modifier,
    color: Color,
    textSize: Float
) {
    // Two dots drawn relative to the digit size: exactly centred on the cards and, unlike a
    // ":" in sp, independent of density and the system font scale
    Canvas(
        modifier = modifier
    ) {
        val dotRadius = textSize * COLON_DOT_RADIUS_RATIO
        val dotOffset = textSize * COLON_DOT_OFFSET_RATIO
        drawCircle(
            color = color,
            radius = dotRadius,
            center = Offset(center.x, center.y - dotOffset)
        )
        drawCircle(
            color = color,
            radius = dotRadius,
            center = Offset(center.x, center.y + dotOffset)
        )
    }
}

private val HIGH_MINUTES = charArrayOf('0', '1', '2', '3', '4', '5')
private val LOW_MINUTES = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

// Upper end of the font size slider: the digits fill the page
private const val MAX_TEXT_SIZE_FC = 450F
// Guard against unbounded constraints
private const val MAX_TEXT_PX = 4000F
// Per 1 px of text size
private const val COLON_WIDTH_RATIO = 0.3F
private const val COLON_DOT_RADIUS_RATIO = 0.032F
private const val COLON_DOT_OFFSET_RATIO = 0.12F
private const val ROW_GAP_RATIO = 0.12F

const val DEFAULT_REVERSE_ROTATION_FC = true
const val DEFAULT_CORNER_SIZE = 0F
val DEFAULT_BACKGROUND = android.graphics.Color.parseColor("#2C2C2C")
const val DEFAULT_TEXT_SIZE_FC = 440F
const val DEFAULT_PADDING_FC = 12F