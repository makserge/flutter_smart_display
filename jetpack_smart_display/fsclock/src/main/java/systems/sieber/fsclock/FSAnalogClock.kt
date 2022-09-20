package systems.sieber.fsclock

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource

@Composable
fun FSAnalogClock(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    color: ColorFilter = ColorFilter.tint(color = MaterialTheme.colors.primary),
    hour: Int,
    minute: Int,
    second: Int,
    millisecond: Int
) {
    val hourAngle = (hour + minute.toFloat() / 60) * 360 / 12
    val minuteAngle = (minute + second.toFloat() / 60) * 360 / 60
    val secondAngle = (second + millisecond.toFloat() / 1000) * 360 / 60

    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Image(
                modifier = modifier
                    .fillMaxSize(),
                colorFilter = color,
                painter = painterResource(R.drawable.ic_background),
                contentDescription = null
            )
            Image(
                modifier = modifier
                    .fillMaxSize()
                    .rotate(hourAngle),
                colorFilter = color,
                painter = painterResource(R.drawable.ic_hour),
                contentDescription = null,
            )
            Image(
                modifier = modifier
                    .fillMaxSize()
                    .rotate(minuteAngle),
                colorFilter = color,
                painter = painterResource(R.drawable.ic_minute),
                contentDescription = null
            )
            Image(
                modifier = modifier
                    .fillMaxSize()
                    .rotate(secondAngle),
                painter = painterResource(R.drawable.ic_second),
                contentDescription = null
            )
        }
    }
}
