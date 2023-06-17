package com.smsoft.smartdisplay.ui.screen.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.smsoft.smartdisplay.data.DashboardItem
import com.smsoft.smartdisplay.ui.composable.asr.CheckRecordAudioPermission
import com.smsoft.smartdisplay.ui.composable.asr.SpeechRecognitionAlert
import com.smsoft.smartdisplay.ui.screen.clock.ClockScreen
import com.smsoft.smartdisplay.ui.screen.doorbell.DoorbellScreen
import com.smsoft.smartdisplay.ui.screen.radio.RadioScreen
import com.smsoft.smartdisplay.ui.screen.sensors.SensorsScreen
import com.smsoft.smartdisplay.ui.screen.weather.WeatherScreen

@OptIn(ExperimentalFoundationApi::class, ExperimentalPagerApi::class)
@UnstableApi
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
    onDoorBellAlarm: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val doorBellAlarmState = viewModel.doorBellAlarmState.collectAsStateWithLifecycle()

    if (doorBellAlarmState.value) {
        viewModel.resetDoorBellAlarmState()
        LaunchedEffect(Unit) {
            onDoorBellAlarm()
        }
        return
    }

    val asrPermissionsState = viewModel.asrPermissionsState.collectAsStateWithLifecycle()
    val asrRecognitionState = viewModel.asrRecognitionState.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState()
    val pages = DashboardItem.values()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {
                    viewModel.sendPressButtonEvent()
                },
                onLongClick = onSettingsClick
            )
    ) {
        HorizontalPager(
            modifier = Modifier
                .weight(1F)
                .fillMaxSize(),
            pageCount = pages.size,
            state = pagerState,
        ) { index ->
            if (index == pagerState.currentPage) {
                RenderScreen(
                    modifier = Modifier,
                    index = index,
                    onSettingsClick = onSettingsClick
                )
            }
        }
        HorizontalPagerIndicator(
            modifier = Modifier
                .align(
                    alignment = Alignment.CenterHorizontally
                )
                .padding(16.dp),
            pagerState = pagerState,
            pageCount = pages.size
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
}

@UnstableApi
@Composable
fun RenderScreen(
    modifier: Modifier,
    index: Int,
    onSettingsClick: () -> Unit
) {
    when (DashboardItem.getItem(index)) {
        DashboardItem.CLOCK -> ClockScreen()
        DashboardItem.WEATHER -> WeatherScreen {
            onSettingsClick()
        }
        DashboardItem.SENSORS -> SensorsScreen()
        DashboardItem.INTERNET_RADIO -> RadioScreen {
            onSettingsClick()
        }
        else -> DoorbellScreen(
            onSettingsClick = onSettingsClick
        )
    }
}