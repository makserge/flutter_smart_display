package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.database.entity.Sensor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Item(
    modifier: Modifier,
    item: Sensor,
    itemsData: HashMap<String, String>,
    editMode: Boolean,
    onDelete: (item: Sensor) -> Unit,
    onOptionsClick: (item: Sensor) -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .defaultMinSize(
                minHeight = 120.dp
            ),
        elevation = 8.dp,
        shape = RoundedCornerShape(10.dp),
        backgroundColor = MaterialTheme.colors.surface,
    ) {
        Row(
            modifier = Modifier
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1F)
            ) {
                TextWithIcon(
                    modifier = Modifier,
                    context = context,
                    text = item.title,
                    icon = item.titleIcon,
                )
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
                FlowRow(
                    modifier = Modifier
                ) {
                    TextWithUnitIcon(
                        modifier = Modifier,
                        context = context,
                        text = itemsData[item.topic1] ?: "n/a",
                        unit = if (itemsData[item.topic1] != null) item.topic1Unit else "",
                        icon = item.topic1Icon
                    )
                    if (item.topic2.isNotEmpty()) {
                        TextWithUnitIcon(
                            modifier = Modifier,
                            context = context,
                            text = itemsData[item.topic2] ?: "n/a",
                            unit = if (itemsData[item.topic2] != null) item.topic2Unit else "",
                            icon = item.topic2Icon
                        )
                    }
                    if (item.topic3.isNotEmpty() && item.topic3Unit.isNotEmpty()) {
                        TextWithUnitIcon(
                            modifier = Modifier,
                            context = context,
                            text = itemsData[item.topic3] ?: "n/a",
                            unit = if (itemsData[item.topic3] != null) item.topic3Unit else "",
                            icon = item.topic3Icon
                        )
                    }
                    if (item.topic4.isNotEmpty() && item.topic4Unit.isNotEmpty()) {
                        TextWithUnitIcon(
                            modifier = Modifier,
                            context = context,
                            text = itemsData[item.topic4] ?: "n/a",
                            unit = if (itemsData[item.topic4] != null) item.topic4Unit else "",
                            icon = item.topic4Icon
                        )
                    }
                }
            }
            if (editMode) {
                Column(
                    modifier = Modifier
                ) {
                    Icon(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(
                                top = 20.dp,
                                end = 10.dp
                            )
                            .clickable(
                                onClick = {
                                    onDelete(item)
                                }
                            ),
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete)
                    )
                    Icon(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(
                                top = 10.dp,
                                end = 10.dp
                            )
                            .clickable(
                                onClick = {
                                    onOptionsClick(item)
                                }
                            ),
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.options)
                    )
                }
            }
        }
    }
}