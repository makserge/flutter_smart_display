package com.smsoft.smartdisplay.ui

import androidx.compose.runtime.Composable
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smsoft.smartdisplay.data.Screen
import com.smsoft.smartdisplay.ui.screen.clock.ClockScreen
import com.smsoft.smartdisplay.ui.screen.dashboard.DashboardScreen
import com.smsoft.smartdisplay.ui.screen.doorbell.DoorbellScreen
import com.smsoft.smartdisplay.ui.screen.radio.RadioScreen
import com.smsoft.smartdisplay.ui.screen.sensors.SensorsScreen
import com.smsoft.smartdisplay.ui.screen.settings.SettingsScreen
import com.smsoft.smartdisplay.ui.screen.weather.WeatherScreen

@UnstableApi
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController
            )
        }

        composable(Screen.Doorbell.route) {
            DoorbellScreen(
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onBack = {
                    navController.navigate(Screen.Dashboard.route)
                }
            )
        }

        composable(Screen.Clock.route) {
            ClockScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen {
                navController.navigateUp()
            }
        }

        composable(Screen.Radio.route) {
            RadioScreen {
                navController.navigate(Screen.Settings.route)
            }
        }

        composable(Screen.Sensors.route) {
            SensorsScreen()
        }

        composable(Screen.Weather.route) {
            WeatherScreen {
                navController.navigate(Screen.Settings.route)
            }
        }
    }
}

