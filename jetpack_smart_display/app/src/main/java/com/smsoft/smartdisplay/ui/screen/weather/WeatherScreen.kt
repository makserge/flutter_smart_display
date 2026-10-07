package com.smsoft.smartdisplay.ui.screen.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.database.entity.WeatherCurrent
import com.smsoft.smartdisplay.data.database.entity.WeatherForecast
import com.smsoft.smartdisplay.network.WeatherFailureKind
import com.smsoft.smartdisplay.network.WeatherResult
import com.smsoft.smartdisplay.ui.composable.weather.CurrentWeather
import com.smsoft.smartdisplay.ui.composable.weather.Forecast

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel(),
    onSettingsClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.onStart()
    }
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val uiState = state) {
            WeatherUiState.Loading -> CircularProgressIndicator()
            // Settings open only on a tap, never automatically
            WeatherUiState.NoLocation -> WeatherMessage(
                message = stringResource(R.string.weather_no_location),
                details = null,
                buttonText = stringResource(R.string.weather_open_settings),
                onButtonClick = onSettingsClick
            )
            is WeatherUiState.Error -> WeatherMessage(
                message = failureText(uiState.failure),
                details = stringResource(R.string.weather_retrying),
                buttonText = stringResource(R.string.weather_retry),
                onButtonClick = {
                    viewModel.retry()
                }
            )
            is WeatherUiState.Content -> {
                WeatherBanner(
                    viewModel = viewModel,
                    state = uiState
                )
                Weather(
                    modifier = Modifier.weight(1F),
                    viewModel = viewModel,
                    currentListState = currentListState,
                    currentForecast = uiState.current,
                    weatherForecast = uiState.forecast
                )
            }
        }
    }
}

@Composable
private fun WeatherMessage(
    message: String,
    details: String?,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier,
            text = message,
            style = MaterialTheme.typography.h5,
            color = MaterialTheme.colors.primary,
            textAlign = TextAlign.Center
        )
        if (details != null) {
            Text(
                modifier = Modifier
                    .padding(top = 8.dp),
                text = details,
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.secondary,
                textAlign = TextAlign.Center
            )
        }
        TextButton(
            modifier = Modifier
                .padding(top = 16.dp),
            onClick = onButtonClick
        ) {
            Text(
                text = buttonText,
                style = MaterialTheme.typography.h6
            )
        }
    }
}

// One line above the data when the last update failed or the data is from an earlier day
@Composable
private fun WeatherBanner(
    viewModel: WeatherViewModel,
    state: WeatherUiState.Content
) {
    if ((state.failure != null) || state.isStale) {
        val failure = state.failure?.let { failureText(it) }
        val dataTime = viewModel.getDataTime(state.lastSuccessAt, state.forecast)?.let {
            stringResource(R.string.weather_stale, it)
        }
        val text = listOfNotNull(failure, dataTime).joinToString(". ")
        if (text.isNotEmpty()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 4.dp
                    ),
                text = text,
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.error,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

// failure == null means no attempt has finished yet
@Composable
private fun failureText(failure: WeatherResult.Failure?): String {
    val code = failure?.httpCode ?: 0
    return when (failure?.kind) {
        null -> stringResource(R.string.weather_waiting)
        WeatherFailureKind.NETWORK -> stringResource(R.string.weather_error_network)
        WeatherFailureKind.AUTH -> stringResource(R.string.weather_error_auth, code)
        WeatherFailureKind.CLIENT,
        WeatherFailureKind.SERVER -> stringResource(R.string.weather_error_server, code)
        WeatherFailureKind.PARSE -> stringResource(R.string.weather_error_parse)
        WeatherFailureKind.UNKNOWN -> stringResource(R.string.weather_error_unknown)
    }
}

@Composable
fun Weather(
    modifier: Modifier,
    viewModel: WeatherViewModel,
    currentListState: LazyListState,
    currentForecast: WeatherCurrent?,
    weatherForecast: List<WeatherForecast>
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        state = currentListState
    ) {
        currentForecast?.run {
            item {
                CurrentWeather(
                    modifier = Modifier,
                    viewModel = viewModel,
                    currentWeather = currentForecast,
                    nightTemperature = weatherForecast.firstOrNull()?.temperatureNight
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
