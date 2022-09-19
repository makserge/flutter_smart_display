package com.smsoft.smartdisplay.ui.screen.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.smsoft.smartdisplay.R

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DashboardItems.values().forEach {
                Item(
                    iconId = it.iconId,
                    textId = it.textId,
                    isSelected = true
                )
            }
        }
    }
}

@Composable
fun Item(
    iconId: Int,
    textId: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(iconId),
            contentDescription = null,
            modifier = Modifier
                .padding(dimensionResource(R.dimen.main_padding)))
        Text(
            text = stringResource(textId),
            color = if (isSelected) MaterialTheme.colors.secondary else MaterialTheme.colors.primary,
            style = MaterialTheme.typography.body1
        )
    }
}
/*
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Greetings
        val greetings by viewModel.greetingsRes.collectAsState()
        Text(
            text = stringResource(id = greetings),
            style = MaterialTheme.typography.h3
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Action : Click Me
        Button(onClick = {
            viewModel.onClickMeClicked()
        }) {
            Text(text = stringResource(id = R.string.action_click_me))
        }
    }

 */