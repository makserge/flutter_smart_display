package com.smsoft.smartdisplay.ui.composable.sensors

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.SensorType

@Composable
fun ItemTypeSelector(
    modifier: Modifier,
    context: Context,
    value: SensorType,
    onValueChange: (String) -> Unit = {}
) {
    val items = SensorType.toMap(context)

    val selectedValue = context.getString(SensorType.getById(value.id).titleId)

    var selectedOptionText by rememberSaveable { mutableStateOf(selectedValue) }
    var isExpanded by remember { mutableStateOf(false) }

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
            },
            placeholder = {
                Text(
                    modifier = Modifier,
                    text = stringResource(R.string.type)
                )
            }
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
                    items.forEach { item ->
                        DropdownMenuItem(
                            onClick = {
                                selectedOptionText = item.value
                                onValueChange(item.key)
                                isExpanded = false
                            }
                        ) {
                            Text(
                                modifier = Modifier,
                                text = item.value
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ItemTypeSelectorPreview() {
    val context = LocalContext.current
    val type = SensorType.getDefault()

    ItemTypeSelector(
        modifier = Modifier,
        context = context,
        value = type,
        onValueChange = {
        },
    )
}