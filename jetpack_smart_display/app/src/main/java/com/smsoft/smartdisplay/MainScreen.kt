package com.smsoft.smartdisplay

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController

@Composable
fun MainScreen(navController: NavHostController) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainScreenItems.values().forEach {
                Item(it.iconId, it.textId, true)
            }
        }
    }
}

@Composable
fun Item(iconId: Int, textId: Int, isSelected: Boolean) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val image = vectorResource(iconId)
        val modifier = Modifier
            .padding(dimensionResource(R.dimen.main_padding))
        Image(image, modifier)
        Text(
            text = stringResource(textId),
            color = if (isSelected) MaterialTheme.colors.secondary else MaterialTheme.colors.primary,
            style = MaterialTheme.typography.body1
        )
    }
}
