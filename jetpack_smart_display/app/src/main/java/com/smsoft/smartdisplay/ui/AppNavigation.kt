package com.smsoft.smartdisplay.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smsoft.smartdisplay.data.Screen
import com.smsoft.smartdisplay.ui.screen.clock.ClockScreen
import com.smsoft.smartdisplay.ui.screen.clocksettings.ClockSettingsScreen
import com.smsoft.smartdisplay.ui.screen.dashboard.DashboardScreen
import com.smsoft.smartdisplay.ui.screen.doorbell.DoorbellScreen
import com.smsoft.smartdisplay.ui.screen.radio.RadioScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Radio.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }

        composable(Screen.Doorbell.route) {
            //DoorbellScreen(uri = Uri.parse("rtsp://a:a@192.168.8.110:8080/h264_pcm.sdp"))
            DoorbellScreen(
                uri = Uri.parse("rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mp4")
            )
        }

        composable(Screen.Clock.route) {
            ClockScreen(
                onClick = {
                    navController.navigate(Screen.ClockSettings.route)
                }
            )
        }

        composable(Screen.ClockSettings.route) {
            ClockSettingsScreen()
        }

        composable(Screen.Radio.route) {
            RadioScreen()
        }
    }
}