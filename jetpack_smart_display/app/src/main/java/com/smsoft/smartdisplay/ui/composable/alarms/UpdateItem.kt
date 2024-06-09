package com.smsoft.smartdisplay.ui.composable.alarms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.AlarmSoundToneType
import com.smsoft.smartdisplay.data.AlarmSoundType
import com.smsoft.smartdisplay.data.database.entity.Alarm
import com.smsoft.smartdisplay.ui.common.ListChooser
import com.smsoft.smartdisplay.ui.screen.alarms.AlarmsViewModel
import com.smsoft.smartdisplay.utils.playAlarmSound

@UnstableApi
@Composable
fun UpdateItem(
    modifier: Modifier,
    item: Alarm,
    viewModel: AlarmsViewModel,
    onCloseDialog: () -> Unit,
    onUpdateItem: (item: Alarm) -> Unit
) {
    val context = LocalContext.current

    var time by remember { mutableIntStateOf(item.time) }
    var days by remember { mutableIntStateOf(item.days) }
    var soundType by remember { mutableStateOf(item.soundType) }
    var soundTone by remember { mutableStateOf(item.soundTone) }
    var radioPreset by remember { mutableIntStateOf(item.radioPreset) }
    val radioPresets = viewModel.radioPresets.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadRadioPresets()
    }

    AlertDialog(
        modifier = Modifier
            .padding(15.dp),
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        onDismissRequest = onCloseDialog,
        title = {
            Text(
                modifier = Modifier,
                text = stringResource(if (item.id > 0) R.string.edit_alarm else R.string.add_alarm)
            )
        },
        text = {
            Column(
                modifier = Modifier,
            ) {
                Text(
                    modifier = Modifier
                        .height(1.dp),
                    text = ""
                )
                Column(
                    modifier = Modifier,
                ) {
                    TimeChooser(
                        modifier = Modifier,
                        time = time,
                        onChange = {
                            time = it
                        },
                    )
                    DayOfWeekChooser(
                        modifier = Modifier,
                        days = days,
                        onChange = {
                            days = it
                        },
                    )
                    ListChooser(
                        modifier = Modifier,
                        title = stringResource(R.string.alarm_sound_type),
                        entries = AlarmSoundType.toMap(context),
                        value = soundType,
                        onChange = {
                            soundType = it
                        },
                    )
                    ListChooser(
                        modifier = Modifier,
                        title = stringResource(R.string.alarm_sound_tone),
                        entries = AlarmSoundToneType.toMap(context),
                        value = soundTone,
                        onChange = {
                            soundTone = it
                            playAlarmSound(
                                player = viewModel.player,
                                soundToneType = AlarmSoundToneType.getById(it),
                                soundVolume = 1F
                            )
                        },
                    )
                    ListChooser(
                        modifier = Modifier,
                        title = stringResource(R.string.alarm_sound_radio_preset),
                        entries = radioPresets.value,
                        value = radioPreset.toString(),
                        onChange = {
                            radioPreset = Integer.parseInt(it)
                            viewModel.playRadio(radioPreset,  false) {
                            }
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                modifier = Modifier,
                onClick = {
                    if (days == 0) {
                        return@TextButton
                    }
                    val alarm = Alarm(
                        id = item.id,
                        time = time,
                        days = days,
                        isEnabled = item.isEnabled,
                        radioPreset = radioPreset,
                        soundTone = soundTone,
                        soundType = soundType
                    )
                    onUpdateItem(alarm)
                    onCloseDialog()
                }
            ) {
                Text(
                    modifier = Modifier,
                    text = stringResource(if (item.id > 0) R.string.add_sensor_update_button else R.string.add_sensor_add_button)
                )
            }
        },
        dismissButton = {
            TextButton(
                modifier = Modifier,
                onClick = onCloseDialog
            ) {
                Text(
                    modifier = Modifier,
                    text = stringResource(R.string.add_sensor_cancel_button)
                )
            }
        }
    )
}