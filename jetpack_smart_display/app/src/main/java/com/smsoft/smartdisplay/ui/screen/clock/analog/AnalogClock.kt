package com.smsoft.smartdisplay.ui.screen.clock.analog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smsoft.smartdisplay.R

@Composable
fun AnalogClock(
    modifier: Modifier = Modifier,
    viewModel: AnalogClockViewModel = hiltViewModel()
) {
    Image(
        modifier = Modifier
            .fillMaxSize(),
        painter = painterResource(R.drawable.clock),
        contentDescription = null
    )
    Column (
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Image(
                modifier = Modifier
                    .width(100.dp)
                    .rotate(90F),
                painter = painterResource(R.drawable.clock_minute),
                contentDescription = null
            )
            Image(
                modifier = Modifier
                    .rotate(0F),
                painter = painterResource(R.drawable.clock_hour),
                contentDescription = null
            )
        }
    }
}