package com.smsoft.smartdisplay

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smsoft.smartdisplay.ui.SmartDisplayTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusBar()
        setContent {
            SmartDisplayApp()
        }
    }

    private fun hideStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }
}

@Composable
private fun SmartDisplayApp() {
    SmartDisplayTheme(true) {
        CreateNavigation()
    }
}

@Composable
private fun CreateNavigation() {
    val navController = rememberNavController()
    val mainScreen = stringResource(R.string.main_screen)
    NavHost(
        navController = navController,
        startDestination = mainScreen
    ) {
        composable(mainScreen) {
            MainScreen(navController = navController)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SmartDisplayApp()
}