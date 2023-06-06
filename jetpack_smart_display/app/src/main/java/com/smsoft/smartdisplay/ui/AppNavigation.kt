package com.smsoft.smartdisplay.ui

import android.annotation.SuppressLint
import android.net.Uri
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

@SuppressLint("AuthLeak")
@UnstableApi
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController,
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Doorbell.route) {
            //DoorbellScreen(uri = Uri.parse("rtsp://a:a@192.168.8.110:8080/h264_pcm.sdp"))
            //"rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mp4"
            //adb shell am start -n org.videolan.vlc/.StartActivity -a android.intent.action.VIEW -d rtsp://camera:Camera12@192.168.8.130/live/ch00_1
            //adb shell am start -n com.vladpen.cams/com.vladpen.cams.MainActivity
            DoorbellScreen(
                uri = Uri.parse("rtsp://***")
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
            SensorsScreen {
                navController.navigate(Screen.Settings.route)
            }
        }

        composable(Screen.Weather.route) {
            WeatherScreen {
                navController.navigate(Screen.Settings.route)
            }
        }
    }
}