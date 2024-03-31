package com.smsoft.smartdisplay.ui.screen.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.data.DashboardItem
import com.smsoft.smartdisplay.ui.composable.asr.CheckRecordAudioPermission
import com.smsoft.smartdisplay.ui.composable.asr.SpeechRecognitionAlert
import com.smsoft.smartdisplay.ui.composable.dashboard.HorizontalPagerScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@UnstableApi
@Composable
fun DashboardScreen(
    onSettingsClick: () -> Unit,
    onDoorbell:() -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val pageCount = DashboardItem.entries.toTypedArray().size
    val pagerState = rememberPagerState(
        initialPage = DashboardItem.CLOCK.ordinal,
        pageCount = {
            pageCount
        }
    )
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageChanged(pagerState.currentPage)
    }
    val currentPageState = viewModel.currentPageState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    LaunchedEffect(currentPageState.value) {
        if (pagerState.currentPage != currentPageState.value) {
            scope.launch {
                pagerState.animateScrollToPage(currentPageState.value)
            }
        }
    }
    val voiceCommandState = viewModel.voiceCommandState.collectAsStateWithLifecycle()

    val doorBellAlarmState = viewModel.doorBellAlarmState.collectAsStateWithLifecycle()

    if (doorBellAlarmState.value) {
        viewModel.resetDoorBellAlarmState()
        LaunchedEffect(Unit) {
            onDoorbell()
        }
        return
    }

    val asrPermissionsState = viewModel.asrPermissionsState.collectAsStateWithLifecycle()
    val asrRecognitionState = viewModel.asrRecognitionState.collectAsStateWithLifecycle()

    HorizontalPagerScreen(
        pagerState = pagerState,
        pageCount = pageCount,
        command = voiceCommandState.value,
        onSettingsClick = onSettingsClick,
        onClick = {
            viewModel.togglePressButton()
        },
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