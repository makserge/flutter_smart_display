package com.smsoft.smartdisplay.ui.screen

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.ui.AppNavigation
import com.smsoft.smartdisplay.ui.theme.SmartDisplayTheme
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference


@AndroidEntryPoint
@UnstableApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        replaceOlderInstance()
        setContent {
            SmartDisplayTheme {
                Surface(modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colors.background)) {
                    AppNavigation()
                }
            }
        }
        hideSystemUI()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    override fun onDestroy() {
        if (currentInstance?.get() === this) {
            currentInstance = null
        }
        super.onDestroy()
    }

    /**
     * Keeps one dashboard. singleTask covers normal launches, but a HOME launch (the home key
     * while the app runs in a normal task, e.g. after a start from the app drawer, or a HOME
     * intent sent on the app's behalf) still creates a second activity in a separate task. Both
     * would ring alarms, show the doorbell and run every voice command. The new one is what the
     * system shows now, so the older one is closed.
     */
    private fun replaceOlderInstance() {
        currentInstance?.get()?.let { older ->
            // A configuration change destroys the old instance before this one is created.
            if ((older !== this) && !older.isFinishing && !older.isDestroyed) {
                Log.i(TAG, "Closing the older dashboard instance")
                if (older.taskId == taskId) {
                    older.finish()
                } else {
                    // Its task holds nothing else and would stay behind in the recents list.
                    older.finishAndRemoveTask()
                }
            }
        }
        currentInstance = WeakReference(this)
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = window.insetsController
            controller?.hide(WindowInsets.Type.ime())
            controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller?.hide(WindowInsets.Type.systemBars())
        } else {
            //noinspection
            @Suppress("DEPRECATION")
            // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        // Main thread only.
        private var currentInstance: WeakReference<MainActivity>? = null
    }
}
