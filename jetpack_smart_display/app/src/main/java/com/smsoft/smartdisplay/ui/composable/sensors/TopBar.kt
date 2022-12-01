package com.smsoft.smartdisplay.ui.composable.sensors

import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smsoft.smartdisplay.R

@Composable
fun TopBar(
    modifier: Modifier,
    onEditClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var isShowMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                modifier = Modifier,
                text = stringResource(R.string.sensors)
            )
        },
        actions = {
            IconButton(
                modifier = Modifier,
                onClick = {
                    isShowMenu = !isShowMenu
                }
            ) {
                Icon(
                    modifier = Modifier,
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.options)
                )
            }
            DropdownMenu(
                modifier = Modifier,
                expanded = isShowMenu,
                onDismissRequest = {
                    isShowMenu = false
                }
            ) {
                DropdownMenuItem(
                    modifier = Modifier
                        .width(50.dp),
                    onClick = onEditClick
                ) {
                    Icon(
                        modifier = Modifier,
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.edit)
                    )
                }
                DropdownMenuItem(
                    modifier = Modifier
                        .width(50.dp),
                    onClick = onSettingsClick
                ) {
                    Icon(
                        modifier = Modifier,
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.settings)
                    )
                }
            }
        }
    )
}