package com.smsoft.smartdisplay.ui.composable.sensors

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R

@Composable
internal fun UpdateMQTTTopics(
    modifier: Modifier,
    context: Context,
    topic1: String,
    topic1Unit: String,
    topic1Icon: String,
    topic1FocusRequester: FocusRequester,
    topic2: String,
    topic2Unit: String,
    topic2Icon: String,
    topic3: String,
    topic3Unit: String,
    topic3Icon: String,
    topic4: String,
    topic4Unit: String,
    topic4Icon: String,
    onTopic1Change: (String) -> Unit,
    onUnit1Change: (String) -> Unit,
    onIcon1Change: (String) -> Unit,
    onTopic2Change: (String) -> Unit,
    onUnit2Change: (String) -> Unit,
    onIcon2Change: (String) -> Unit,
    onTopic3Change: (String) -> Unit,
    onUnit3Change: (String) -> Unit,
    onIcon3Change: (String) -> Unit,
    onTopic4Change: (String) -> Unit,
    onUnit4Change: (String) -> Unit,
    onIcon4Change: (String) -> Unit
) {
    val topic2FocusRequester = FocusRequester()
    val topic3FocusRequester = FocusRequester()
    val topic4FocusRequester = FocusRequester()

    TextFieldWithIcon(
        modifier = Modifier,
        context = context,
        title = stringResource(R.string.add_sensor_topic1),
        topic = topic1,
        unitTitle = stringResource(R.string.add_sensor_unit),
        unit = topic1Unit,
        icon = topic1Icon,
        focusRequester = topic1FocusRequester,
        initialFocus = false,
        onTopicChange = onTopic1Change,
        onUnitChange = onUnit1Change,
        onIconChange = onIcon1Change
    )
    Spacer(
        modifier = Modifier.height(5.dp)
    )
    TextFieldWithIcon(
        modifier = Modifier,
        context = context,
        title = stringResource(R.string.add_sensor_topic2),
        topic = topic2,
        unitTitle = stringResource(R.string.add_sensor_unit),
        unit = topic2Unit,
        icon = topic2Icon,
        focusRequester = topic2FocusRequester,
        initialFocus = false,
        onTopicChange = onTopic2Change,
        onUnitChange = onUnit2Change,
        onIconChange = onIcon2Change
    )
    Spacer(
        modifier = Modifier.height(5.dp)
    )
    TextFieldWithIcon(
        modifier = Modifier,
        context = context,
        title = stringResource(R.string.add_sensor_topic3),
        topic = topic3,
        unitTitle = stringResource(R.string.add_sensor_unit),
        unit = topic3Unit,
        icon = topic3Icon,
        focusRequester = topic3FocusRequester,
        initialFocus = false,
        onTopicChange = onTopic3Change,
        onUnitChange = onUnit3Change,
        onIconChange = onIcon3Change
    )
    Spacer(
        modifier = Modifier.height(5.dp)
    )
    TextFieldWithIcon(
        modifier = Modifier,
        context = context,
        title = stringResource(R.string.add_sensor_topic4),
        topic = topic4,
        unitTitle = stringResource(R.string.add_sensor_unit),
        unit = topic4Unit,
        icon = topic4Icon,
        focusRequester = topic4FocusRequester,
        initialFocus = false,
        onTopicChange = onTopic4Change,
        onUnitChange = onUnit4Change,
        onIconChange = onIcon4Change
    )
}