package com.smsoft.smartdisplay.ui.composable.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.database.entity.WeatherForecast
import com.smsoft.smartdisplay.ui.screen.weather.WeatherViewModel

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
                    vertical = 0.dp
                ),
            text = viewModel.getDayOfWeek(item.date, item.timezone),
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.secondary
        )
        Image(
            modifier = Modifier.size(64.dp),
            painter = rememberAsyncImagePainter(
                stringResource(
                    R.string.weather_icon_url,
                    item.icon
                )
            ),
            contentDescription = null
        )
        Column(
            modifier = Modifier
        ) {
            TextWithValue(
                modifier = Modifier,
                title = stringResource(R.string.weather_morning_temperature),
                value = stringResource(R.string.weather_morning_temperature_value, item.temperatureMorning),
                style = MaterialTheme.typography.subtitle2
            )
            TextWithValue(
                modifier = Modifier,
                title = stringResource(R.string.weather_day_temperature),
                value = stringResource(R.string.weather_day_temperature_value, item.temperatureDay),
                style = MaterialTheme.typography.subtitle2
            )
            TextWithValue(
                modifier = Modifier,
                title = stringResource(R.string.weather_evening_temperature),
                value = stringResource(R.string.weather_evening_temperature_value, item.temperatureEvening),
                style = MaterialTheme.typography.subtitle2
            )
            TextWithValue(
                modifier = Modifier,
                title = stringResource(R.string.weather_night_temperature),
                value = stringResource(R.string.weather_night_temperature_value, item.temperatureNight),
                style = MaterialTheme.typography.subtitle2
            )
            TextWithValue(
                modifier = Modifier,
                title = stringResource(R.string.weather_humidity),
                value = stringResource(R.string.weather_humidity_value, item.humidity),
                style = MaterialTheme.typography.subtitle2
            )
            TextWithValue(
                modifier = Modifier,
                title = stringResource(R.string.weather_wind),
                value = stringResource(R.string.weather_wind_value, item.windSpeed,
                    viewModel.windDegreeToDirection(item.windDirection)
                ),
                style = MaterialTheme.typography.subtitle2
            )
        }
    }
}