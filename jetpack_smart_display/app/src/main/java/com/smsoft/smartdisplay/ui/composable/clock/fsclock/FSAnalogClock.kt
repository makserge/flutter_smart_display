package com.smsoft.smartdisplay.ui.composable.clock.fsclock

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import com.smsoft.smartdisplay.R

@Composable
fun FSAnalogClock(
    modifier: Modifier = Modifier,
    primaryColor: Color,
    secondaryColor: Color,
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int
) {
    // This function runs on every 100 ms tick, so it only stores the time. The face below
    // reads it while placing the hand layers, so it is not recomposed and the vectors are
    // not redrawn: a tick only changes the rotation of the hand layers.
    val millisOfDay = rememberUpdatedState(
        ((hour * 60 + minute) * 60 + second) * 1000 + milliSecond
    )

    OnDraw(
        modifier = modifier,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        millisOfDay = { millisOfDay.value }
    )
}

@Composable
private fun OnDraw(
    modifier: Modifier,
    primaryColor: Color,
    secondaryColor: Color,
    millisOfDay: () -> Int
) {
    val primaryColorFilter = remember(primaryColor) {
        ColorFilter.tint(
            color = primaryColor
        )
    }
    val secondaryColorFilter = remember(secondaryColor) {
        ColorFilter.tint(
            color = secondaryColor
        )
    }

    // The largest square that fits the page, centred, whatever modifier the caller passes.
    Box(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
            .aspectRatio(1F)
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // The dial artwork is not centred in its 140x140 viewport (its centre is at
                    // 69.45/69.66), while the hands pivot exactly at the centre of the box.
                    translationX = size.width * DIAL_OFFSET_X
                    translationY = size.height * DIAL_OFFSET_Y
                },
            colorFilter = primaryColorFilter,
            painter = painterResource(R.drawable.ic_background_fs),
            contentDescription = null
        )
        Hand(
            drawable = R.drawable.ic_hour_fs,
            colorFilter = primaryColorFilter,
            angle = { hourAngle(millisOfDay()) }
        )
        Hand(
            drawable = R.drawable.ic_minute_fs,
            colorFilter = primaryColorFilter,
            angle = { minuteAngle(millisOfDay()) },
            // Its tip ran onto the bright outline of the hour marks
            lengthScale = MINUTE_HAND_SCALE
        )
        Hand(
            drawable = R.drawable.ic_second_fs,
            colorFilter = secondaryColorFilter,
            angle = { secondAngle(millisOfDay()) }
        )
    }
}

@Composable
private fun Hand(
    @DrawableRes drawable: Int,
    colorFilter: ColorFilter,
    angle: () -> Float,
    lengthScale: Float = 1F
) {
    Image(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // Read in the layer block: a new time only updates the layer's rotation.
                rotationZ = angle()
                // The hands are drawn in a 130 viewport and the dial in a 140 one (both declare
                // that size in dp). Filling the same box made the hands 140/130 too long, so the
                // minute hand ran into the hour marks.
                scaleX = HAND_SCALE
                // Only the length: the hands point up before the rotation, and the layer scales
                // before it rotates, so this keeps the width
                scaleY = HAND_SCALE * lengthScale
            },
        colorFilter = colorFilter,
        painter = painterResource(drawable),
        contentDescription = null
    )
}

// Degrees clockwise from 12 o'clock. Hour and minute hands step once a second,
// the second hand sweeps with every tick.
private fun hourAngle(millisOfDay: Int) = (millisOfDay / 1000 % 43_200) / 120F

private fun minuteAngle(millisOfDay: Int) = (millisOfDay / 1000 % 3_600) / 10F

private fun secondAngle(millisOfDay: Int) = (millisOfDay % 60_000) * 0.006F

private const val DIAL_OFFSET_X = 0.548F / 140F
private const val DIAL_OFFSET_Y = 0.336F / 140F
private const val HAND_SCALE = 130F / 140F
// The minute hand ends at 0.835 of the dial radius, where the outline of the marks starts at
// about 0.80; this brings its tip just inside it.
private const val MINUTE_HAND_SCALE = 0.95F
