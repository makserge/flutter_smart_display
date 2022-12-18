package com.smsoft.smartdisplay.ui.screen.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.database.entity.WeatherCurrent
import com.smsoft.smartdisplay.data.database.entity.WeatherForecast
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
                horizontalArrangement = Arrangement.SpaceEvenly,
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

@Composable
fun CurrentWeather(
    modifier: Modifier,
    viewModel: WeatherViewModel,
    currentWeather: WeatherCurrent
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier.size(128.dp),
            painter = rememberAsyncImagePainter(stringResource(R.string.weather_icon_url, currentWeather.icon)),
            contentDescription = null
        )
        Column(
            modifier = Modifier
        ) {
            Text(
                modifier = Modifier,
                text = stringResource(R.string.weather_degrees_unit, currentWeather.temperature),
                style = MaterialTheme.typography.h3
            )
            Text(
                modifier = Modifier,
                text = stringResource(R.string.weather_humidity, currentWeather.humidity)
            )
            Text(
                modifier = Modifier,
                text = stringResource(R.string.weather_wind, currentWeather.windSpeed, viewModel.windDegreeToDirection(currentWeather.windDirection))
            )
        }
    }
}

@Composable
fun Forecast(
    modifier: Modifier,
    viewModel: WeatherViewModel,
    item: WeatherForecast,
) {
    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier
                .padding(
                    horizontal = 16.dp,
                    vertical = 4.dp
                ),
            text = viewModel.getDayOfWeek(item.date, item.timezone),
            fontSize = 12.sp,
            style = MaterialTheme.typography.overline
        )
        Image(
            modifier = Modifier.size(64.dp),
            painter = rememberAsyncImagePainter(stringResource(R.string.weather_icon_url, item.icon)),
            contentDescription = null
        )
        Text(
            modifier = Modifier,
            text = stringResource(R.string.weather_morning_temperature, item.temperatureMorning),
            fontSize = 12.sp
        )
        Text(
            modifier = Modifier,
            text = stringResource(R.string.weather_day_temperature, item.temperatureDay),
            fontSize = 12.sp
        )
        Text(
            modifier = Modifier,
            text = stringResource(R.string.weather_evening_temperature, item.temperatureEvening),
            fontSize = 12.sp
        )
        Text(
            modifier = Modifier,
            text = stringResource(R.string.weather_night_temperature, item.temperatureNight),
            fontSize = 12.sp
        )
        Text(
            modifier = Modifier,
            text = stringResource(R.string.weather_humidity, item.humidity),
            fontSize = 12.sp
        )
        Text(
            modifier = Modifier,
            text = stringResource(R.string.weather_wind, item.windSpeed, viewModel.windDegreeToDirection(item.windDirection)),
            fontSize = 12.sp
        )
    }
}
