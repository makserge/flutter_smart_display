package com.smsoft.smartdisplay.ui.screen.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.pager.*
import com.smsoft.smartdisplay.data.DashboardItem
import com.smsoft.smartdisplay.data.Screen
import com.smsoft.smartdisplay.ui.screen.clock.ClockScreen
import com.smsoft.smartdisplay.ui.screen.radio.RadioScreen
import com.smsoft.smartdisplay.ui.screen.sensors.SensorsScreen
import com.smsoft.smartdisplay.ui.screen.weather.WeatherScreen

@OptIn(ExperimentalPagerApi::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val pagerState = rememberPagerState()
    val pages = DashboardItem.values()
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        HorizontalPager(
            modifier = Modifier
                .weight(1F)
                .fillMaxSize(),
            count = pages.size,
            state = pagerState,
        ) { index ->
            if (index == pagerState.currentPage) {
                RenderScreen(
                    modifier = Modifier,
                    index = index,
                    navController = navController
                )
            }
        }
        HorizontalPagerIndicator(
            modifier = Modifier
                .align(
                    alignment = Alignment.CenterHorizontally
                )
                .padding(16.dp),
            pagerState = pagerState
        )
    }
}

@Composable
fun RenderScreen(
    modifier: Modifier,
    index: Int,
    navController: NavHostController
) {
    when (DashboardItem.getItem(index)) {
        DashboardItem.CLOCK -> ClockScreen {
            navController.navigate(Screen.ClockSettings.route)
        }
        DashboardItem.WEATHER -> WeatherScreen {
            navController.navigate(Screen.WeatherSettings.route)
        }
        DashboardItem.SENSORS -> SensorsScreen {
            navController.navigate(Screen.SensorsSettings.route)
        }
        else -> RadioScreen {
            navController.navigate(Screen.RadioSettings.route)
        }
    }
}