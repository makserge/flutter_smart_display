package com.smsoft.smartdisplay.ui.screen.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.jamal.composeprefs.ui.PrefsScreen
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.ClockType
import com.smsoft.smartdisplay.ui.composable.asr.CheckRecordAudioPermission
import com.smsoft.smartdisplay.ui.composable.settings.asrSettings
import com.smsoft.smartdisplay.ui.composable.settings.clockSettings
import com.smsoft.smartdisplay.ui.composable.settings.doorbellSettings
import com.smsoft.smartdisplay.ui.composable.settings.pushButtonSettings
import com.smsoft.smartdisplay.ui.composable.settings.radioSettings
import com.smsoft.smartdisplay.ui.composable.settings.sensorsSettings
import com.smsoft.smartdisplay.ui.composable.settings.weatherSettings

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

    val pushButtonTopic by viewModel.pushButtonTopic.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val pushButtonPayloadOn by viewModel.pushButtonPayloadOn.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val pushButtonPayloadOff by viewModel.pushButtonPayloadOff.collectAsStateWithLifecycle(
        initialValue = ""
    )

    val asrPermissionsState = viewModel.asrPermissionsState.collectAsStateWithLifecycle()

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
        PrefsScreen(
            modifier = modifier,
            dataStore = dataStore
        ) {
            clockSettings(
                modifier = modifier,
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
            doorbellSettings (
                modifier = Modifier,
                scope = this,
                alarmTopic = doorbellAlarmTopic.toString(),
                streamURL = doorbellStreamURL.toString()
            )
            pushButtonSettings (
                modifier = Modifier,
                scope = this,
                topic = pushButtonTopic.toString(),
                payloadOn = pushButtonPayloadOn.toString(),
                payloadOff = pushButtonPayloadOff.toString(),
            )
            asrSettings (
                modifier = Modifier,
                scope = this
            )
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