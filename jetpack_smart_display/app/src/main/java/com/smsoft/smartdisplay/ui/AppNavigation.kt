package com.smsoft.smartdisplay.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smsoft.smartdisplay.data.Screen
import com.smsoft.smartdisplay.ui.screen.clock.ClockScreen
import com.smsoft.smartdisplay.ui.screen.clocksettings.ClockSettingsScreen
import com.smsoft.smartdisplay.ui.screen.dashboard.DashboardScreen
import com.smsoft.smartdisplay.ui.screen.doorbell.DoorbellScreen
import com.smsoft.smartdisplay.ui.screen.radio.RadioScreen
import com.smsoft.smartdisplay.ui.screen.radiosettings.RadioSettingsScreen
import com.smsoft.smartdisplay.ui.screen.sensors.SensorsScreen
import com.smsoft.smartdisplay.ui.screen.sensorssettings.SensorsSettingsScreen
import com.smsoft.smartdisplay.ui.screen.weather.WeatherScreen
import com.smsoft.smartdisplay.ui.screen.weathersettings.WeatherSettingsScreen

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
            //DoorbellScreen(uri = Uri.parse("rtsp://a:a@192.168.8.110:8080/h264_pcm.sdp"))
            DoorbellScreen(
                uri = Uri.parse("rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mp4")
            )
        }

        composable(Screen.Clock.route) {
            ClockScreen {
                navController.navigate(Screen.ClockSettings.route)
            }
        }

        composable(Screen.ClockSettings.route) {
            ClockSettingsScreen {
                navController.navigateUp()
            }
        }

        composable(Screen.Radio.route) {
            RadioScreen {
                navController.navigate(Screen.RadioSettings.route)
            }
        }

        composable(Screen.RadioSettings.route) {
            RadioSettingsScreen {
                navController.navigateUp()
            }
        }

        composable(Screen.Sensors.route) {
            SensorsScreen {
                navController.navigate(Screen.SensorsSettings.route)
            }
        }

        composable(Screen.SensorsSettings.route) {
            SensorsSettingsScreen {
                navController.navigateUp()
            }
        }

        composable(Screen.Weather.route) {
            WeatherScreen {
                navController.navigate(Screen.WeatherSettings.route)
            }
        }

        composable(Screen.WeatherSettings.route) {
            WeatherSettingsScreen {
                navController.navigateUp()
            }
        }
    }
}