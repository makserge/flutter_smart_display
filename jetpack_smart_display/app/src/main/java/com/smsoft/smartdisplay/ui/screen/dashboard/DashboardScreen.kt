package com.smsoft.smartdisplay.ui.screen.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.DashboardItems

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val list = DashboardItems.values()
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize(),
        columns = GridCells.Fixed(2),

        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 16.dp
        )
    ) {
        items(
            count = list.size,
            key = { it }
        ) { index ->
            val item = list[index]
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .clickable(
                        onClick = {
                            if (item.route.isNotEmpty()) {
                                navController.navigate(item.route)
                            }
                        }
                    ),
                elevation = 8.dp,
                shape = RoundedCornerShape(10.dp),
                backgroundColor = MaterialTheme.colors.surface,
            ) {
                DrawItem(
                    modifier = Modifier,
                    iconId = item.iconId,
                    textId = item.textId
                )
            }
        }
    }
}

@Composable
fun DrawItem(
    modifier: Modifier = Modifier,
    iconId: Int,
    textId: Int
) {
    Column(
        modifier = Modifier
            .padding(15.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .padding(dimensionResource(R.dimen.main_padding)),
            painter = painterResource(iconId),
            contentDescription = null,
        )
        Text(
            modifier = Modifier,
            text = stringResource(textId),
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.body1
        )
    }
}