package com.smsoft.smartdisplay.ui.screen.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import com.smsoft.smartdisplay.data.DashboardItem
import com.smsoft.smartdisplay.data.Screen
import com.smsoft.smartdisplay.service.asr.processCommand
import com.smsoft.smartdisplay.ui.composable.asr.CheckRecordAudioPermission
import com.smsoft.smartdisplay.ui.composable.asr.SpeechRecognitionAlert
import com.smsoft.smartdisplay.ui.composable.dashboard.HorizontalPagerScreen
import kotlinx.coroutines.launch

@UnstableApi
@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val page = remember { mutableStateOf(DashboardItem.CLOCK.ordinal) }

    val doorBellAlarmState = viewModel.doorBellAlarmState.collectAsStateWithLifecycle()

    if (doorBellAlarmState.value) {
        viewModel.resetDoorBellAlarmState()
        LaunchedEffect(Unit) {
            navController.navigate(Screen.Doorbell.route)
        }
        return
    }

    val asrCommandState = viewModel.asrCommandState.collectAsStateWithLifecycle()
    if (asrCommandState.value != null && asrCommandState.value!!.isNotBlank()) {
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                processCommand(
                    context = context,
                    command = asrCommandState.value!!,
                    onPageChanged = {
                        page.value = it
                    }
                )
            }
            viewModel.resetAsrCommandState()
        }
        return
    }

    val asrPermissionsState = viewModel.asrPermissionsState.collectAsStateWithLifecycle()
    val asrRecognitionState = viewModel.asrRecognitionState.collectAsStateWithLifecycle()

    HorizontalPagerScreen(
        currentPage = page.value,
        onSettingsClick = {
            navController.navigate(Screen.Settings.route)
        },
        onClick = {
            viewModel.sendPressButtonEvent()
        }
    )
    if (asrPermissionsState.value) {
        CheckRecordAudioPermission(
            modifier = Modifier,
            onGranted = {
                viewModel.startAsrService()
            },
            onCancel = {
                viewModel.disableAsr()
            }
        )
    }
    if (asrRecognitionState.value != null) {
        SpeechRecognitionAlert(
            modifier = Modifier,
            text = asrRecognitionState.value!!,
            onDismiss = {
                viewModel.cancelAsrAction()
            }
        )
    }
}