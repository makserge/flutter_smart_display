package com.smsoft.smartdisplay.ui.composable.clock.nightdream

import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.smsoft.smartdisplay.ui.composable.clock.nightdream.digit.TabDigit
import com.smsoft.smartdisplay.ui.screen.clock.ClockViewModel
import com.smsoft.smartdisplay.utils.getStateFromFlow
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * The flaps of a 1970s flip alarm clock: one card for the hours and one for the minutes, each split
 * across the middle. The cards fill the screen: on a wide screen the hours and minutes sit side by
 * side, on a square or portrait screen the minutes are below the hours. The numerals are as tall as
 * the cards allow and condensed where a card is narrower than they are, as the tall numerals of the
 * old clocks. Each flap is a TabDigit, so each digit flips on its own; a card drawn behind each pair
 * closes the gap between its two flaps. A physical object, so the clock colors of Settings do not
 * apply: dark cards with light numerals.
 */
@Composable
fun DigitalFlipClock(
    modifier: Modifier = Modifier,
    viewModel: ClockViewModel,
    scale: Float,
    hour: Int,
    minute: Int,
    is24Hour: Boolean
) {
    val cardColor = CARD_COLOR
    val digitColor = DIGIT_COLOR
    val splitColor = SPLIT_COLOR
    val cardArgb = cardColor.toArgb()
    val digitArgb = digitColor.toArgb()
    val splitArgb = splitColor.toArgb()

    val reverseRotation = getStateFromFlow(
        flow = viewModel.reverseRotationFC,
        defaultValue = DEFAULT_REVERSE_ROTATION_FC
    ) as Boolean

    // Space around a numeral inside its card, in px at the default 440 px text; it is scaled with the text
    val padding = getStateFromFlow(
        flow = viewModel.paddingFC,
        defaultValue = DEFAULT_PADDING_FC
    ) as Float

    // Slider 200..450: share of the largest numeral size that fits its card (450 = all of it)
    val fontSize = getStateFromFlow(
        flow = viewModel.fontSizeFC,
        defaultValue = DEFAULT_TEXT_SIZE_FC
    ) as Float

    val digitSizeRatio = remember { TabDigit.digitSizeRatio() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(all = OUTER_PADDING),
        contentAlignment = Alignment.Center
    ) {
        val geometry = flipGeometry(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        val sizeFactor = (fontSize / MAX_TEXT_SIZE_FC).coerceIn(0.1F, 1F) * scale.coerceIn(0.1F, 1F)
        val numerals = numeralsFor(geometry, digitSizeRatio, padding / DEFAULT_TEXT_SIZE_FC, sizeFactor)

        val (hourTens, hourUnits) = hourCards(hour, is24Hour)
        // In 12-hour format the hour card says AM or PM in its corner, as on the old clocks
        val meridiem = when {
            is24Hour -> null
            hour >= 12 -> "PM"
            else -> "AM"
        }
        val flapDigit = @Composable { chars: CharArray, index: Int ->
            FlipDigit(
                chars = chars,
                index = index,
                numerals = numerals,
                geometry = geometry,
                reverseRotation = reverseRotation,
                cardColor = cardArgb,
                textColor = digitArgb,
                dividerColor = splitArgb
            )
        }
        FlapGroup(
            geometry = geometry,
            modifier = Modifier.flapCards(geometry, cardColor, digitColor, splitColor, meridiem)
        ) {
            // TabDigit takes its characters only when it is created: a 12/24-hour switch
            // recreates the two hour flaps, which then show the hour at once without a flip
            key(is24Hour) {
                flapDigit(hourTens.chars, hourTens.index)
                flapDigit(hourUnits.chars, hourUnits.index)
            }
            flapDigit(HIGH_MINUTES, minute / 10)
            flapDigit(LOW_MINUTES, minute % 10)
        }
    }
}

/**
 * The geometry of the clock in px: the size of a card, the gap between the hour and the minute
 * pair, and whether the minutes are below the hours (stacked) or to their right. The digits are
 * numbered 0 to 3: hour tens, hour units, minute tens, minute units.
 */
private class FlipGeometry(
    val cardWidth: Float,
    val cardHeight: Float,
    val stacked: Boolean
) {
    val gap = cardWidth * PAIR_GAP_RATIO
    // A slot is the view of one flap: its card plus the room a flip needs on every side
    val slotWidth = cardWidth * TabDigit.SLOT_WIDTH_RATIO
    val slotHeight = cardHeight * TabDigit.SLOT_HEIGHT_RATIO
    private val slotMarginX = (slotWidth - cardWidth) / 2F
    private val slotMarginY = (slotHeight - cardHeight) / 2F

    // The slots overlap by their margins, so the cards of a pair touch and the group is the cards
    val groupWidth = if (stacked) 2 * cardWidth else 4 * cardWidth + gap
    val groupHeight = if (stacked) 2 * slotHeight + gap else slotHeight

    /** The card of digit [index]. */
    fun card(index: Int): Rect {
        val pair = index / 2
        val left: Float
        val top: Float
        if (stacked) {
            left = (index % 2) * cardWidth
            top = pair * (slotHeight + gap) + slotMarginY
        } else {
            left = index * cardWidth + pair * gap
            top = slotMarginY
        }
        return Rect(Offset(left, top), Size(cardWidth, cardHeight))
    }

    /** The slot of digit [index]. */
    fun slot(index: Int): Rect = card(index).let {
        Rect(Offset(it.left - slotMarginX, it.top - slotMarginY), Size(slotWidth, slotHeight))
    }

    /** The card behind [pair] (0 hours, 1 minutes), two cards wide. */
    fun pairCard(pair: Int): Rect = card(2 * pair).let {
        Rect(it.topLeft, Size(2 * cardWidth, cardHeight))
    }
}

/**
 * The geometry that fills a box. A rectangular box (wider than [RECTANGULAR_ASPECT] times its
 * height) puts the four cards in a row; any other box puts the minutes below the hours. The cards
 * are as large as the box allows in both directions, so the clock fills the screen; the numerals
 * are then sized to the cards, see [numeralsFor].
 */
private fun flipGeometry(width: Float, height: Float): FlipGeometry {
    val stacked = width <= height * RECTANGULAR_ASPECT
    val cardWidth: Float
    val cardHeight: Float
    if (stacked) {
        cardWidth = width / 2
        cardHeight = (height - cardWidth * PAIR_GAP_RATIO) / (2 * TabDigit.SLOT_HEIGHT_RATIO)
    } else {
        cardWidth = width / (4 + PAIR_GAP_RATIO)
        cardHeight = height / TabDigit.SLOT_HEIGHT_RATIO
    }
    return FlipGeometry(max(1F, cardWidth), max(1F, cardHeight), stacked)
}

/** The numerals: their text size in px and their horizontal scale. */
private class Numerals(val textSize: Float, val scaleX: Float)

/**
 * Numerals as tall as the card allows, condensed (down to [MIN_TEXT_SCALE_X]) when they would
 * otherwise be wider than the card: the tall, narrow numerals of the old flip clocks, which fill
 * the cards of a wide screen. They are never wider than their natural [TabDigit.FLAP_TEXT_SCALE_X],
 * so on a wide card they keep their shape. [sizeFactor] (the size slider) scales them down inside
 * the card. [digitSizeRatio] is the box around the digits per 1 px of text at a scale of 1, and
 * [paddingRatio] the space around a numeral per 1 px of text.
 */
private fun numeralsFor(
    geometry: FlipGeometry,
    digitSizeRatio: PointF,
    paddingRatio: Float,
    sizeFactor: Float
): Numerals {
    val heightFit = geometry.cardHeight / (digitSizeRatio.y + paddingRatio)
    // At the narrowest scale even the widest digit must fit the card
    val widthFit = geometry.cardWidth / (digitSizeRatio.x * MIN_TEXT_SCALE_X + paddingRatio)
    val textSize = (min(heightFit, widthFit) * sizeFactor).coerceIn(1F, MAX_TEXT_PX)
    val widestScale = (geometry.cardWidth / textSize - paddingRatio) / digitSizeRatio.x
    return Numerals(
        textSize = textSize,
        scaleX = widestScale.coerceIn(MIN_TEXT_SCALE_X, TabDigit.FLAP_TEXT_SCALE_X)
    )
}

/**
 * Places the four flap slots of [content] by [FlipGeometry.slot]: the slots overlap, so the cards
 * of a pair touch and the pairs sit [FlipGeometry.gap] apart. The group is the size of the cards.
 */
@Composable
private fun FlapGroup(
    geometry: FlipGeometry,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, _ ->
        val slotConstraints = Constraints.fixed(
            width = geometry.slotWidth.roundToInt(),
            height = geometry.slotHeight.roundToInt()
        )
        val placeables = measurables.map { it.measure(slotConstraints) }
        layout(geometry.groupWidth.roundToInt(), geometry.groupHeight.roundToInt()) {
            placeables.forEachIndexed { index, placeable ->
                val slot = geometry.slot(index)
                placeable.place(slot.left.roundToInt(), slot.top.roundToInt())
            }
        }
    }
}

/**
 * The two cards behind the flaps, one per pair, each from the left flap's left edge to the right
 * flap's right edge. Each has a soft shadow under it and a hairline edge, so it reads as a card on
 * the page in both themes; on top of the flaps come a sheen on the upper flap and a shadow under
 * the split. [label] goes in the bottom-left corner of the hour card.
 */
private fun Modifier.flapCards(
    geometry: FlipGeometry,
    color: Color,
    digitColor: Color,
    splitColor: Color,
    label: String?
): Modifier = this
    .drawBehind {
        for (pair in 0..1) {
            val card = geometry.pairCard(pair)
            // A soft shadow under the card lifts it off the page
            drawRect(
                color = Color.Black.copy(alpha = 0.2F),
                topLeft = card.topLeft + Offset(0F, card.height * 0.02F),
                size = card.size
            )
            // Square corners: the flap tiles are square and cover any rounding of the card
            drawRect(
                color = color,
                topLeft = card.topLeft,
                size = card.size
            )
        }
    }
    .drawWithContent {
        drawContent()
        for (pair in 0..1) {
            drawCardLight(geometry.pairCard(pair), splitColor)
        }
        if (label != null) {
            drawLabel(geometry.pairCard(0), label, digitColor)
        }
    }

/** The light of the old clocks on [card]: a sheen on the upper flap, a shadow under the split, the split line and a hairline edge. */
private fun DrawScope.drawCardLight(card: Rect, splitColor: Color) {
    val half = card.height / 2F
    val split = card.top + half
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.22F), Color.Transparent),
            startY = card.top,
            endY = card.top + half * 0.6F
        ),
        topLeft = card.topLeft,
        size = Size(card.width, half * 0.6F)
    )
    val shadow = card.height * 0.08F
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color.Black.copy(alpha = 0.3F), Color.Transparent),
            startY = split,
            endY = split + shadow
        ),
        topLeft = Offset(card.left, split),
        size = Size(card.width, shadow)
    )
    drawLine(
        color = splitColor,
        start = Offset(card.left, split),
        end = Offset(card.right, split),
        strokeWidth = max(1F, card.height * 0.012F)
    )
    // A hairline edge, so the card keeps its outline on a page of a similar colour
    drawRect(
        color = splitColor.copy(alpha = 0.35F),
        topLeft = card.topLeft,
        size = card.size,
        style = Stroke(width = max(1F, card.height * 0.008F))
    )
}

/** [label] (AM or PM) in the bottom-left corner of [card], as on the old clocks. */
private fun DrawScope.drawLabel(card: Rect, label: String, digitColor: Color) {
    drawIntoCanvas { canvas ->
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = digitColor.copy(alpha = 0.75F).toArgb()
            textSize = card.height * 0.085F
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.nativeCanvas.drawText(
            label,
            card.left + card.height * 0.05F,
            card.top + card.height * 0.95F,
            paint
        )
    }
}

@Composable
private fun FlipDigit(
    chars: CharArray,
    index: Int,
    numerals: Numerals,
    geometry: FlipGeometry,
    reverseRotation: Boolean,
    cardColor: Int,
    textColor: Int,
    dividerColor: Int
) {
    // Its size is the slot, given by FlapGroup
    AndroidView(
        factory = { context ->
            TabDigit(context).apply {
                this.chars = chars
                this.textSize = numerals.textSize
                this.textScaleX = numerals.scaleX
                this.textColor = textColor
                setChar(index)
            }
        },
        update = { view ->
            view.reverseRotation = reverseRotation
            view.cornerSize = DEFAULT_CORNER_SIZE
            view.background = cardColor
            view.cardWidth = geometry.cardWidth
            view.cardHeight = geometry.cardHeight
            view.textSize = numerals.textSize
            view.textScaleX = numerals.scaleX
            view.dividerColor = dividerColor
            view.textColor = textColor
            // A single step flips, any other change jumps, so the digit is always the current time
            view.flipTo(index)
        }
    )
}

private val HIGH_MINUTES = charArrayOf('0', '1', '2', '3', '4', '5')
private val LOW_MINUTES = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')

private val OUTER_PADDING = 4.dp
// Upper end of the font size slider: the numerals fill their cards
private const val MAX_TEXT_SIZE_FC = 450F
// Guard against unbounded constraints
private const val MAX_TEXT_PX = 4000F
// The gap between the hour and the minute card, per card width
private const val PAIR_GAP_RATIO = 0.08F
// A box wider than this many times its height is rectangular: the hours and minutes go side by side
private const val RECTANGULAR_ASPECT = 1.2F
// The numerals are condensed at most to this horizontal scale to fit a narrow card
private const val MIN_TEXT_SCALE_X = 0.45F

private val CARD_COLOR = Color(0xFF2C2C2C)
private val DIGIT_COLOR = Color(0xFFEDEDED)
private val SPLIT_COLOR = Color(0xFF000000)

const val DEFAULT_REVERSE_ROTATION_FC = true
const val DEFAULT_CORNER_SIZE = 0F
// The old dark card color, still TabDigit's own default
val DEFAULT_BACKGROUND = android.graphics.Color.parseColor("#2C2C2C")
const val DEFAULT_TEXT_SIZE_FC = 440F
const val DEFAULT_PADDING_FC = 12F
