package com.smsoft.smartdisplay.ui.screen.settings

import android.annotation.SuppressLint
import android.content.ComponentName
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.ui.common.ChoiceScreen
import com.smsoft.smartdisplay.ui.common.prefs.PrefsScreen
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.ClockType
import com.smsoft.smartdisplay.data.SettingsTab
import com.smsoft.smartdisplay.ui.composable.asr.CheckRecordAudioPermission
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_COMMAND_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_COMMAND_OFF_DEFAULT_PAYLOAD
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_COMMAND_ON_DEFAULT_PAYLOAD
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_DIMMER_COMMAND_ON_OFF_DEFAULT_TOPIC
import com.smsoft.smartdisplay.ui.composable.settings.ALARM_LIGHT_SENSOR_THRESHOLD_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.LIGHT_SENSOR_INTERVAL_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.LIGHT_SENSOR_TOPIC_DEFAULT
import com.smsoft.smartdisplay.ui.composable.settings.alarmSettings
import com.smsoft.smartdisplay.ui.composable.settings.asrSettings
import com.smsoft.smartdisplay.ui.composable.settings.clockSettings
import com.smsoft.smartdisplay.ui.composable.settings.doorbellSettings
import com.smsoft.smartdisplay.ui.composable.settings.lightSensorSettings
import com.smsoft.smartdisplay.ui.composable.settings.messageSettings
import com.smsoft.smartdisplay.ui.composable.settings.cinemaModeSettings
import com.smsoft.smartdisplay.ui.composable.settings.proximitySensorSettings
import com.smsoft.smartdisplay.ui.composable.settings.pushButtonSettings
import com.smsoft.smartdisplay.ui.composable.settings.radioSettings
import com.smsoft.smartdisplay.ui.composable.settings.sensorsSettings
import com.smsoft.smartdisplay.ui.composable.settings.systemSettings
import com.smsoft.smartdisplay.utils.openAndroidSettings
import com.smsoft.smartdisplay.utils.openHomeApp
import com.smsoft.smartdisplay.utils.otherHomeApps
import com.smsoft.smartdisplay.ui.composable.settings.timerSettings
import com.smsoft.smartdisplay.ui.composable.settings.weatherSettings
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@UnstableApi
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background),
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val dataStore = viewModel.dataStore

    val clockType by viewModel.clockType.collectAsStateWithLifecycle(
        initialValue = ClockType.getDefaultId()
    )

    val cityLat by viewModel.cityLat.collectAsStateWithLifecycle(
        initialValue = ""
    )
    val cityLon by viewModel.cityLon.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val mqttHost by viewModel.mqttHost.collectAsStateWithLifecycle(
        initialValue = MQTT_SERVER_DEFAULT_HOST
    )
    val mqttPort by viewModel.mqttPort.collectAsStateWithLifecycle(
        initialValue = MQTT_SERVER_DEFAULT_PORT
    )
    val mqttUserName by viewModel.mqttUserName.collectAsStateWithLifecycle(
        initialValue = ""
    )
    val mqttPassword by viewModel.mqttPassword.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val mpdHost by viewModel.mpdHost.collectAsStateWithLifecycle(
        initialValue = MPD_SERVER_DEFAULT_HOST
    )
    val mpdPort by viewModel.mpdPort.collectAsStateWithLifecycle(
        initialValue = MPD_SERVER_DEFAULT_PORT
    )
    val mpdPassword by viewModel.mpdPassword.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val doorbellAlarmTopic by viewModel.doorbellAlarmTopic.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val doorbellStreamURL by viewModel.doorbellStreamURL.collectAsStateWithLifecycle(
        initialValue = DOORBELL_STREAM_DEFAULT_URL
    )

    val pushButtonStatusTopic by viewModel.pushButtonStatusTopic.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val pushButtonCommandTopic by viewModel.pushButtonCommandTopic.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val pushButtonPayloadOn by viewModel.pushButtonPayloadOn.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val pushButtonPayloadOff by viewModel.pushButtonPayloadOff.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val proximitySensorTopic by viewModel.proximitySensorTopic.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val proximitySensorPayloadOn by viewModel.proximitySensorPayloadOn.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val proximitySensorPayloadOff by viewModel.proximitySensorPayloadOff.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val cinemaModeCommandTopic by viewModel.cinemaModeCommandTopic.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val cinemaModePayloadOn by viewModel.cinemaModePayloadOn.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val cinemaModePayloadOff by viewModel.cinemaModePayloadOff.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val lightSensorTopic by viewModel.lightSensorTopic.collectAsStateWithLifecycle(
        initialValue = LIGHT_SENSOR_TOPIC_DEFAULT
    )

    val lightSensorInterval by viewModel.lightSensorInterval.collectAsStateWithLifecycle(
        initialValue = LIGHT_SENSOR_INTERVAL_DEFAULT
    )

    val lightSensorThreshold by viewModel.lightSensorThreshold.collectAsStateWithLifecycle(
        initialValue = ALARM_LIGHT_SENSOR_THRESHOLD_DEFAULT
    )

    val dimmerCommandOnOffTopic by viewModel.dimmerCommandOnOffTopic.collectAsStateWithLifecycle(
        initialValue = ALARM_LIGHT_DIMMER_COMMAND_ON_OFF_DEFAULT_TOPIC
    )

    val dimmerCommandOnPayload by viewModel.dimmerCommandOnPayload.collectAsStateWithLifecycle(
        initialValue = ALARM_LIGHT_DIMMER_COMMAND_ON_DEFAULT_PAYLOAD
    )

    val dimmerCommandOffPayload by viewModel.dimmerCommandOffPayload.collectAsStateWithLifecycle(
        initialValue = ALARM_LIGHT_DIMMER_COMMAND_OFF_DEFAULT_PAYLOAD
    )

    val dimmerCommandTopic by viewModel.dimmerCommandTopic.collectAsStateWithLifecycle(
        initialValue = ALARM_LIGHT_DIMMER_COMMAND_DEFAULT_TOPIC
    )

    val messageTopic by viewModel.messageTopic.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val asrPermissionsState = viewModel.asrPermissionsState.collectAsStateWithLifecycle()

    // The way out, always picked on a list: Android's settings first, then the other home apps. A
    // single other home app used to open at once; on the 4 inch panel that was Sonoff's app, not
    // Android.
    val androidSettingsLabel = stringResource(R.string.open_home_android_settings)
    var homeChoices by remember { mutableStateOf<List<Pair<String, String>>?>(null) }
    homeChoices?.let { choices ->
        ChoiceScreen(
            title = stringResource(R.string.open_home),
            entries = choices,
            selected = null,
            onSelect = { key ->
                homeChoices = null
                if (key == ANDROID_SETTINGS_KEY) {
                    openAndroidSettings(context)
                } else {
                    ComponentName.unflattenFromString(key)?.let { openHomeApp(context, it) }
                }
            },
            onDismissRequest = { homeChoices = null }
        )
    }

    Scaffold(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount.y > 0) {
                        onBack()
                    }
                }
            },
        topBar = {
            SettingsTopBar(
                modifier = Modifier,
            )
        }
    ) {
        Column(
            modifier = modifier
        ) {
            val pagerState = rememberPagerState(
                pageCount = {
                    SettingsTab.entries.size
                }
            )
            val coroutineScope = rememberCoroutineScope()

            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage
            ) {
                SettingsTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(text = stringResource(tab.titleId))
                        }
                    )
                }
            }

            HorizontalPager(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxWidth(),
                state = pagerState
            ) { page ->
                when (SettingsTab.getItem(page)) {
                    SettingsTab.GENERAL -> PrefsScreen(
                        modifier = Modifier,
                        dataStore = dataStore
                    ) {
                        systemSettings(
                            modifier = Modifier,
                            scope = this,
                            onOpenHome = {
                                homeChoices = listOf(ANDROID_SETTINGS_KEY to androidSettingsLabel) +
                                    otherHomeApps(context).map { (component, label) ->
                                        component.flattenToString() to label
                                    }
                            }
                        )
                        clockSettings(
                            modifier = Modifier,
                            scope = this,
                            context = context,
                            clockType = ClockType.getById(clockType.toString())
                        )
                        weatherSettings(
                            modifier = Modifier,
                            scope = this,
                            cityLat = cityLat.toString(),
                            cityLon = cityLon.toString()
                        )
                    }
                    SettingsTab.CONNECTIVITY -> PrefsScreen(
                        modifier = Modifier,
                        dataStore = dataStore
                    ) {
                        sensorsSettings(
                            modifier = Modifier,
                            scope = this,
                            host = mqttHost.toString(),
                            port = mqttPort.toString(),
                            userName = mqttUserName.toString(),
                            password = mqttPassword.toString()
                        )
                        radioSettings(
                            modifier = Modifier,
                            context = context,
                            scope = this,
                            host = mpdHost.toString(),
                            port = mpdPort.toString(),
                            password = mpdPassword.toString()
                        )
                    }
                    SettingsTab.SENSORS -> PrefsScreen(
                        modifier = Modifier,
                        dataStore = dataStore
                    ) {
                        doorbellSettings(
                            modifier = Modifier,
                            scope = this,
                            alarmTopic = doorbellAlarmTopic.toString(),
                            streamURL = doorbellStreamURL.toString()
                        )
                        pushButtonSettings(
                            modifier = Modifier,
                            scope = this,
                            statusTopic = pushButtonStatusTopic.toString(),
                            commandTopic = pushButtonCommandTopic.toString(),
                            payloadOn = pushButtonPayloadOn.toString(),
                            payloadOff = pushButtonPayloadOff.toString(),
                        )
                        proximitySensorSettings(
                            modifier = Modifier,
                            scope = this,
                            topic = proximitySensorTopic.toString(),
                            payloadOn = proximitySensorPayloadOn.toString(),
                            payloadOff = proximitySensorPayloadOff.toString(),
                        )
                        cinemaModeSettings(
                            modifier = Modifier,
                            scope = this,
                            commandTopic = cinemaModeCommandTopic.toString(),
                            payloadOn = cinemaModePayloadOn.toString(),
                            payloadOff = cinemaModePayloadOff.toString(),
                        )
                        lightSensorSettings(
                            modifier = Modifier,
                            scope = this,
                            topic = lightSensorTopic.toString(),
                            interval = lightSensorInterval.toString()
                        )
                    }
                    SettingsTab.ALARM -> PrefsScreen(
                        modifier = Modifier,
                        dataStore = dataStore
                    ) {
                        alarmSettings(
                            modifier = Modifier,
                            scope = this,
                            lightSensorThreshold = lightSensorThreshold.toString(),
                            dimmerCommandOnOffTopic = dimmerCommandOnOffTopic.toString(),
                            dimmerCommandOnPayload = dimmerCommandOnPayload.toString(),
                            dimmerCommandOffPayload = dimmerCommandOffPayload.toString(),
                            dimmerCommandTopic = dimmerCommandTopic.toString(),
                        )
                    }
                    SettingsTab.VOICE_ALERTS -> PrefsScreen(
                        modifier = Modifier,
                        dataStore = dataStore
                    ) {
                        asrSettings(
                            modifier = Modifier,
                            scope = this,
                            context = context
                        )
                        timerSettings(
                            modifier = Modifier,
                            scope = this
                        )
                        messageSettings(
                            modifier = Modifier,
                            messageTopic = messageTopic.toString(),
                            scope = this
                        )
                    }
                }
            }
        }
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
    }
}

@Composable
fun SettingsTopBar(
    modifier: Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                modifier = Modifier,
                text = stringResource(R.string.settings)
            )
        }
    )
}

// The "Android settings" entry of the "Open home screen" list; the home apps are keyed by their
// component names, which always have a "/"
private const val ANDROID_SETTINGS_KEY = "android-settings"
