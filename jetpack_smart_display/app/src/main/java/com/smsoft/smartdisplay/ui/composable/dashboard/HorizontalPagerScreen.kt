package com.smsoft.smartdisplay.ui.composable.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.smsoft.smartdisplay.data.DashboardItem
import com.smsoft.smartdisplay.data.VoiceCommand
import com.smsoft.smartdisplay.ui.screen.clock.ClockScreen
import com.smsoft.smartdisplay.ui.screen.doorbell.DoorbellScreen
import com.smsoft.smartdisplay.ui.screen.radio.RadioScreen
import com.smsoft.smartdisplay.ui.screen.sensors.SensorsScreen
import com.smsoft.smartdisplay.ui.screen.weather.WeatherScreen

@OptIn(ExperimentalFoundationApi::class)
@UnstableApi
@Composable
fun HorizontalPagerScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    command: VoiceCommand,
    onSettingsClick: () -> Unit,
    onClick: () -> Unit
) {
    val pages = DashboardItem.values()
    val pagerState = rememberPagerState(
        initialPage = currentPage,
        pageCount = {
            pages.size
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {
                    onClick()
                },
                onLongClick = onSettingsClick
            )
    ) {
        HorizontalPager(
            modifier = Modifier
                .weight(1F)
                .fillMaxSize(),
            state = pagerState
        ) { index ->
            if (index == pagerState.currentPage) {
                RenderScreen(
                    modifier = Modifier,
                    index = index,
                    command = command,
                    onSettingsClick = onSettingsClick
                )
            }
        }
        HorizontalPagerIndicator(
            modifier = Modifier
                .align(
                    alignment = Alignment.CenterHorizontally
                )
                .padding(16.dp),
            pagerState = pagerState,
            pageCount = pages.size
        )
    }
}

@UnstableApi
@Composable
fun RenderScreen(
    modifier: Modifier,
    index: Int,
    command: VoiceCommand,
    onSettingsClick: () -> Unit
) {
    when (DashboardItem.getItem(index)) {
        DashboardItem.CLOCK -> ClockScreen()
        DashboardItem.WEATHER -> WeatherScreen {
            onSettingsClick()
        }
        DashboardItem.SENSORS -> SensorsScreen()
        DashboardItem.INTERNET_RADIO -> RadioScreen(
            command = command,
            onSettingsClick = onSettingsClick
        )
        else -> DoorbellScreen(
            onSettingsClick = onSettingsClick
        )
    }
}