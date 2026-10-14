package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.MaterialIcons
import com.smsoft.smartdisplay.ui.common.ChoiceScreen
import com.smsoft.smartdisplay.utils.getIcon

/**
 * Picks the icon of a sensor or of one of its values. The field shows the chosen icon alone, with
 * no text: "Icon" did not fit the narrow field in the larger dialog text and broke into one letter
 * per line. A tap opens a full-screen list (it was a drop-down pop-up, which on the 4 inch panel
 * covered the whole screen, so only picking an icon closed it); the list starts with "No icon",
 * and its back arrow leaves it without a choice.
 */
@Composable
fun IconPicker(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit = {},
) {
    var selected by rememberSaveable { mutableStateOf(value) }
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val noIcon = stringResource(R.string.no_icon)

    val choose = { item: String ->
        selected = item
        onValueChange(item)
        isExpanded = false
    }

    Box(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .heightIn(min = TextFieldDefaults.MinHeight)
                .clip(MaterialTheme.shapes.small)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled),
                    shape = MaterialTheme.shapes.small
                )
                .clickable {
                    isExpanded = true
                }
                .padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (selected.isEmpty()) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = stringResource(R.string.icon),
                    tint = LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                )
            } else {
                Icon(
                    // The drawables are 48 dp; the field holds a normal 24 dp icon
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = getIcon(context = context, item = selected)),
                    contentDescription = selected
                )
            }
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null
            )
        }
    }
    if (isExpanded) {
        ChoiceScreen(
            title = stringResource(R.string.icon),
            entries = listOf("" to noIcon) + MaterialIcons.shortList.map { it to it },
            selected = selected,
            onSelect = choose,
            onDismissRequest = {
                isExpanded = false
            },
            icon = { item ->
                Icon(
                    modifier = Modifier.size(32.dp),
                    painter = if (item.isEmpty()) {
                        rememberVectorPainter(Icons.Outlined.Block)
                    } else {
                        painterResource(id = getIcon(context = context, item = item))
                    },
                    contentDescription = null
                )
            }
        )
    }
}

@Preview
@Composable
fun IconPickerPreview() {
    val icon = "co2"

    IconPicker(
        modifier = Modifier
            .padding(
                start = 5.dp
            ),
        value = icon,
        onValueChange = {
        },
    )
}

