package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.PrefsScreen
import com.jamal.composeprefs.ui.prefs.EditTextPref
import com.smsoft.smartdisplay.R

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
fun Settings(
    modifier: Modifier,
    dataStore: DataStore<Preferences>,
    onCloseDialog: () -> Unit
) {
    AlertDialog(
        modifier = Modifier,
        onDismissRequest = onCloseDialog,
        title = {
            Text(
                modifier = Modifier,
                text = stringResource(R.string.settings)
            )
        },
        text = {
            PrefsScreen(
                modifier = Modifier,
                dataStore = dataStore
            ) {
                prefsGroup({
                    GroupHeader(
                        title = stringResource(R.string.mqtt_broker)
                    )
                }) {
                    prefsItem {
                        EditTextPref(
                            key = "et1",
                            title = "EditText example",
                            summary = "But it has a dialog title and message",
                            dialogTitle = "Dialog Title",
                            dialogMessage = "Dialog Message"
                        )
                    }
                }
            }
            /*
            Column(
                modifier = Modifier,
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .focusRequester(titleFocusRequester),
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.add_sensor_title)
                        )
                    }
                )
                LaunchedEffect(Unit) {
                    coroutineContext.job.invokeOnCompletion {
                        titleFocusRequester.requestFocus()
                    }
                }
                Spacer(
                    modifier = Modifier.height(10.dp)
                )
                OutlinedTextField(
                    modifier = Modifier.focusRequester(topicFocusRequester),
                    value = topic,
                    onValueChange = {
                        topic = it
                    },
                    placeholder = {
                        Text(
                            modifier = Modifier,
                            text = stringResource(R.string.add_sensor_topic)
                        )
                    }
                )
            }

             */
        },
        confirmButton = {
            TextButton(
                modifier = Modifier,
                onClick = {
                    /*
                    if (!checkField(title, titleFocusRequester)) {
                        return@TextButton
                    }
                    if (!checkField(topic, topicFocusRequester)) {
                        return@TextButton
                    }
                    onCloseDialog()
                    val sensor = Sensor(
                        id = item.id,
                        title = title,
                        topic = topic
                    )
                    onUpdateItem(sensor)

                     */
                }
            ) {
                Text(
                    modifier = Modifier,
                    text = stringResource(R.string.sensor_settings_update_button)
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