package com.smsoft.smartdisplay.ui.screen.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.filter
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.data.DashboardItem
import com.smsoft.smartdisplay.ui.composable.asr.CheckRecordAudioPermission
import com.smsoft.smartdisplay.ui.composable.asr.SpeechRecognitionAlert
import com.smsoft.smartdisplay.ui.composable.dashboard.HorizontalPagerScreen
import com.smsoft.smartdisplay.ui.composable.message.MessageAlert
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
        // The page the ViewModel already asks for, e.g. Alarms for an alarm that was ringing
        // when this dashboard was created.
        initialPage = viewModel.currentPageState.value,
        pageCount = {
            pageCount
        }
    )
    // Report only pages the pager has come to rest on. currentPage also changes while a voice
    // command animates across several pages; feeding those intermediate pages back into the
    // ViewModel restarted the scroll toward them and cut the jump short.
    // The first report is the page the pager starts on. A pager restored after Settings or the
    // doorbell screen starts on its saved page and ignores initialPage. Reporting that page
    // replaced a page the ViewModel had asked for meanwhile (an alarm or a timer that went off),
    // so the alert stayed hidden, and an alarm whose page had not been opened yet did not ring.
    // Such a first report is skipped: the scroll below brings the pager to the requested page,
    // and that page is reported when the pager comes to rest.
    LaunchedEffect(pagerState) {
        var isStartPage = true
        snapshotFlow { pagerState.isScrollInProgress to pagerState.settledPage }
            .filter { (isScrolling, _) -> !isScrolling }
            .collect { (_, page) ->
                val isFirstReport = isStartPage
                isStartPage = false
                if (isFirstReport && (page != viewModel.currentPageState.value)) {
                    return@collect
                }
                viewModel.onPageChanged(page)
            }
    }
    val currentPageState = viewModel.currentPageState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    LaunchedEffect(currentPageState.value) {
        scope.launch {
            pagerState.animateScrollToPage(currentPageState.value)
        }
    }
    val voiceCommandState = viewModel.voiceCommandState.collectAsStateWithLifecycle()

    // A ring opens the doorbell screen from an effect. It used to be handled during composition
    // with an early return that left the pager out for one frame: the radio stopped, started
    // again and stopped once more within a second, and page state was lost.
    val currentOnDoorbell by rememberUpdatedState(onDoorbell)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.doorBellAlarmState.filter { it }.collect {
                viewModel.resetDoorBellAlarmState()
                currentOnDoorbell()
            }
        }
    }

    val asrPermissionsState = viewModel.asrPermissionsState.collectAsStateWithLifecycle()
    val asrRecognitionState = viewModel.asrRecognitionState.collectAsStateWithLifecycle()
    val messageState = viewModel.messageState.collectAsStateWithLifecycle()

    HorizontalPagerScreen(
        pagerState = pagerState,
        pageCount = pageCount,
        command = voiceCommandState.value,
        onResetCommand = {
            viewModel.resetVoiceCommand()
        },
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
    if (messageState.value != null) {
        MessageAlert(
            modifier = Modifier,
            text = messageState.value!!,
            onDismiss = {
                viewModel.cancelMessageAction()
            }
        )
    }
}