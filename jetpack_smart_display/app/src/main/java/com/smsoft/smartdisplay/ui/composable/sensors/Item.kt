package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.database.entity.Sensor
import com.smsoft.smartdisplay.ui.common.COMPACT_SCREEN_DP
import com.smsoft.smartdisplay.ui.common.ITEM_TEXT_END_INSET
import com.smsoft.smartdisplay.ui.common.ItemCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Item(
    modifier: Modifier,
    item: Sensor,
    itemsData: Map<String, String>,
    editMode: Boolean,
    onDelete: (item: Sensor) -> Unit,
    onEnableModification: () -> Unit,
    onOptionsClick: (item: Sensor) -> Unit
) {
    val context = LocalContext.current
    // A narrow screen (the 4 inch panel is 360 dp) gets smaller values, so a card's values share a
    // line and two cards fit on the page
    val compact = LocalConfiguration.current.screenWidthDp < COMPACT_SCREEN_DP
    val valueStyle = if (compact) MaterialTheme.typography.h4 else MaterialTheme.typography.h2
    val valueIconSize = if (compact) COMPACT_VALUE_ICON_SIZE else VALUE_ICON_SIZE

    // The values first and large, as the time of an alarm or a timer, and the name below them as
    // their title, with the edit buttons at its end, so the three lists look the same (ItemCard)
    ItemCard(
        editMode = editMode,
        editDescription = stringResource(R.string.edit_sensor),
        onEnableModification = onEnableModification,
        onEdit = { onOptionsClick(item) },
        onDelete = { onDelete(item) },
        top = {
            FlowRow(
                modifier = Modifier.padding(end = ITEM_TEXT_END_INSET)
            ) {
                TextWithUnitIcon(
                    modifier = Modifier,
                    context = context,
                    text = itemsData[item.topic1] ?: "n/a",
                    unit = if (itemsData[item.topic1] != null) item.topic1Unit else "",
                    icon = item.topic1Icon,
                    style = valueStyle,
                    iconSize = valueIconSize
                )
                if (item.topic2.isNotEmpty()) {
                    TextWithUnitIcon(
                        modifier = Modifier,
                        context = context,
                        text = itemsData[item.topic2] ?: "n/a",
                        unit = if (itemsData[item.topic2] != null) item.topic2Unit else "",
                        icon = item.topic2Icon,
                        style = valueStyle,
                        iconSize = valueIconSize
                    )
                }
                if (item.topic3.isNotEmpty() && item.topic3Unit.isNotEmpty()) {
                    TextWithUnitIcon(
                        modifier = Modifier,
                        context = context,
                        text = itemsData[item.topic3] ?: "n/a",
                        unit = if (itemsData[item.topic3] != null) item.topic3Unit else "",
                        icon = item.topic3Icon,
                        style = valueStyle,
                        iconSize = valueIconSize
                    )
                }
                if (item.topic4.isNotEmpty() && item.topic4Unit.isNotEmpty()) {
                    TextWithUnitIcon(
                        modifier = Modifier,
                        context = context,
                        text = itemsData[item.topic4] ?: "n/a",
                        unit = if (itemsData[item.topic4] != null) item.topic4Unit else "",
                        icon = item.topic4Icon,
                        style = valueStyle,
                        iconSize = valueIconSize
                    )
                }
            }
        },
        bottom = {
            TextWithIcon(
                modifier = Modifier,
                context = context,
                text = item.title,
                icon = item.titleIcon,
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.primary,
                iconSize = TITLE_ICON_SIZE
            )
        }
    )
}

// Icons next to the large values (on a narrow screen and on a wide one) and next to the title
private val COMPACT_VALUE_ICON_SIZE = 32.dp
private val VALUE_ICON_SIZE = 48.dp
private val TITLE_ICON_SIZE = 32.dp