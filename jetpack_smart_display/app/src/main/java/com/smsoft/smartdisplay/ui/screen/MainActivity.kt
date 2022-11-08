package com.smsoft.smartdisplay.ui.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Surface
import com.smsoft.smartdisplay.ui.AppNavigation
import com.smsoft.smartdisplay.ui.theme.SmartDisplayTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartDisplayTheme {
                Surface {
                    AppNavigation()
                }
            }
        }
    }
}