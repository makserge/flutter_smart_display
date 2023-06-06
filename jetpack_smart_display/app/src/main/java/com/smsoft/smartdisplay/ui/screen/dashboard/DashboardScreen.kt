package com.smsoft.smartdisplay.ui.screen.dashboard

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
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.smsoft.smartdisplay.data.DashboardItem
import com.smsoft.smartdisplay.data.Screen
import com.smsoft.smartdisplay.ui.screen.clock.ClockScreen
import com.smsoft.smartdisplay.ui.screen.radio.RadioScreen
import com.smsoft.smartdisplay.ui.screen.sensors.SensorsScreen
import com.smsoft.smartdisplay.ui.screen.weather.WeatherScreen

@OptIn(ExperimentalFoundationApi::class, ExperimentalPagerApi::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onSettingsClick: () -> Unit
) {
    val pagerState = rememberPagerState()
    val pages = DashboardItem.values()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                onLongClick = {
                    onSettingsClick()
                },
                onClick = {  }
            )
    ) {
        HorizontalPager(
            modifier = Modifier
                .weight(1F)
                .fillMaxSize(),
            pageCount = pages.size,
            state = pagerState,
        ) { index ->
            if (index == pagerState.currentPage) {
                RenderScreen(
                    modifier = Modifier,
                    index = index,
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    }
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

@Composable
fun RenderScreen(
    modifier: Modifier,
    index: Int,
    onSettingsClick: () -> Unit
) {
    when (DashboardItem.getItem(index)) {
        DashboardItem.CLOCK -> ClockScreen()
        DashboardItem.WEATHER -> WeatherScreen {
            onSettingsClick()
        }
        DashboardItem.SENSORS -> SensorsScreen {
            onSettingsClick()
        }
        else -> RadioScreen {
            onSettingsClick()
        }
    }
}