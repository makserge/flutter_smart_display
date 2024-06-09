package com.smsoft.smartdisplay.ui.composable.timers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.util.UnstableApi
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.AlarmSoundToneType
import com.smsoft.smartdisplay.data.TimerDurationType
import com.smsoft.smartdisplay.data.database.entity.Timer
import com.smsoft.smartdisplay.ui.common.ListChooser
import com.smsoft.smartdisplay.ui.screen.timers.TimersViewModel
import com.smsoft.smartdisplay.utils.playAlarmSound

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@UnstableApi
@Composable
fun UpdateItem(
    modifier: Modifier,
    item: Timer,
    viewModel: TimersViewModel,
    onCloseDialog: () -> Unit,
    onUpdateItem: (item: Timer) -> Unit
) {
    val context = LocalContext.current

    var duration by remember { mutableLongStateOf(item.duration) }
    var soundTone by remember { mutableStateOf(item.soundTone) }
    var title by remember { mutableStateOf(item.title) }

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
                text = stringResource(if (item.id > 0) R.string.edit_timer else R.string.add_timer)
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
                    EditTextChooser(
                        modifier = modifier,
                        title = stringResource(R.string.timer_title),
                        value = title,
                        onChange = {
                            title = it
                        },
                    )
                    ListChooser(
                        modifier = Modifier,
                        title = stringResource(R.string.timer_duration),
                        entries = TimerDurationType.toMap(context),
                        value = duration.toString(),
                        onChange = {
                            duration = it.toLong()
                        }
                    )
                    ListChooser(
                        modifier = Modifier,
                        title = stringResource(R.string.timer_sound_tone),
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
                }
            }
        },
        confirmButton = {
            TextButton(
                modifier = Modifier,
                onClick = {
                    val timer = Timer(
                        id = item.id,
                        title = title,
                        duration = duration,
                        soundTone = soundTone
                    )
                    onUpdateItem(timer)
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