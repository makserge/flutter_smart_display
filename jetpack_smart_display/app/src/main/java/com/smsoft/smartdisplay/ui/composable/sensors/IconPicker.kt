package com.smsoft.smartdisplay.ui.composable.sensors

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.MaterialIcons
import com.smsoft.smartdisplay.utils.getIcon

@Composable
fun IconPicker(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit = {},
) {
    val items = MaterialIcons.shortList

    var selectedOptionText by rememberSaveable { mutableStateOf(value) }
    var searchedOption by rememberSaveable { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    var filteredItems = mutableListOf<String>()

    val context = LocalContext.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            modifier = Modifier
                .clickable {
                    isExpanded = !isExpanded
                },
            colors = TextFieldDefaults.outlinedTextFieldColors(),
            enabled = false,
            value = selectedOptionText,
            onValueChange = {
                selectedOptionText = it
            },
            placeholder = {
                Text(
                    modifier = Modifier,
                    text = stringResource(R.string.icon)
                )
            },
            leadingIcon = {
                Icon(
                    modifier = Modifier,
                    painter = painterResource(
                        id = getIcon(
                            context = context,
                            item = selectedOptionText
                        )
                    ),
                    contentDescription = null
                )
            },
        )
        if (isExpanded) {
            DropdownMenu(
                modifier = Modifier
                    .padding(15.dp),
                expanded = isExpanded,
                onDismissRequest = {
                    isExpanded = false
                }
            ) {
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier,
                        value = searchedOption,
                        onValueChange = { it ->
                            searchedOption = it
                            filteredItems = items.filter {
                                it.contains(searchedOption, ignoreCase = true)
                            }.toMutableList()
                        }
                    )
                    val itemsList = if (filteredItems.isEmpty()) {
                        items.subList(0, MAX_INITIAL_LIST_SIZE)
                    } else {
                        filteredItems
                    }
                    itemsList.forEach { item ->
                        DropdownMenuItem(
                            onClick = {
                                selectedOptionText = item
                                onValueChange(item)
                                searchedOption = ""
                                isExpanded = false
                            }
                        ) {
                            DropdownItem(
                                modifier = Modifier,
                                context = context,
                                item = item
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownItem(
    modifier: Modifier,
    context: Context,
    item: String
) {
    Row(
        modifier = Modifier
            .wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier,
            painter = painterResource(id = getIcon(
                context = context,
                item = item
            )),
            contentDescription = null
        )
        Spacer(
            modifier = Modifier.width(12.dp)
        )
        Text(
            modifier = Modifier,
            text = item
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

private const val MAX_INITIAL_LIST_SIZE = 5