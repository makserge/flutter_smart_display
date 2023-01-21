package com.smsoft.smartdisplay.ui.screen.weather

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsoft.smartdisplay.data.database.entity.WeatherCurrent
import com.smsoft.smartdisplay.data.database.entity.WeatherForecast
import com.smsoft.smartdisplay.ui.composable.weather.CurrentWeather
import com.smsoft.smartdisplay.ui.composable.weather.Forecast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    val currentForecast = viewModel.currentForecast.collectAsStateWithLifecycle(
        initialValue = WeatherCurrent(
            id = viewModel.weatherRepository.CURRENT_ID,
            temperature = 0,
            humidity = 0,
            icon = "",
            windSpeed = 0,
            windDirection = 0
        )
    )
    val weatherForecast = viewModel.weatherForecast.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    val currentListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            if (viewModel.onStart()) {
                withContext(Dispatchers.Main) {
                    onSettingsClick()
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (state.value) {
            UIState.Initial -> CircularProgressIndicator()
            UIState.Ready -> Weather(
                modifier = Modifier,
                viewModel = viewModel,
                currentListState = currentListState,
                currentForecast = currentForecast.value,
                weatherForecast = weatherForecast.value,
                onSettingsClick = onSettingsClick
            )
        }
    }
}

@Composable
fun Weather(
    modifier: Modifier,
    viewModel: WeatherViewModel,
    currentListState: LazyListState,
    currentForecast: WeatherCurrent?,
    weatherForecast: List<WeatherForecast>,
    onSettingsClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = onSettingsClick
            ),
        state = currentListState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        currentForecast?.run {
            item {
                CurrentWeather(
                    modifier = Modifier,
                    viewModel = viewModel,
                    currentWeather = currentForecast
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weatherForecast.forEach {
                    Forecast(
                        modifier = Modifier,
                        viewModel = viewModel,
                        item = it
                    )
                }
            }
        }
    }
}