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
import coil3.compose.rememberAsyncImagePainter
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.database.entity.WeatherForecast
import com.smsoft.smartdisplay.ui.screen.weather.WeatherViewModel

/** One day of the forecast: its name, icon and four lines, in [sizes]. */
@Composable
fun Forecast(
    modifier: Modifier,
    viewModel: WeatherViewModel,
    item: WeatherForecast,
    sizes: WeatherSizes
) {
    val iconSize = sizes.forecastIcon
    val dayStyle = sizes.day
    val lineStyle = sizes.line
    Column(
        modifier = modifier,
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
            style = dayStyle,
            color = MaterialTheme.colors.secondary
        )
        Image(
            modifier = Modifier.size(iconSize),
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
                value = stringResource(
                    R.string.weather_morning_temperature_value,
                    item.temperatureMorning,
                    item.humidity
                ),
                style = lineStyle
            )
            TextWithValue(
                modifier = Modifier,
                title = stringResource(R.string.weather_day_temperature),
                value = stringResource(
                    R.string.weather_day_temperature_value,
                    item.temperatureDay,
                    item.humidity
                ),
                style = lineStyle
            )
            TextWithValue(
                modifier = Modifier,
                title = stringResource(R.string.weather_night_temperature),
                value = stringResource(
                    R.string.weather_night_temperature_value,
                    item.temperatureNight,
                    item.humidity
                ),
                style = lineStyle
            )
            TextWithValue(
                modifier = Modifier,
                title = stringResource(R.string.weather_wind),
                value = stringResource(R.string.weather_wind_value, item.windSpeed,
                    viewModel.windDegreeToDirection(item.windDirection)
                ),
                style = lineStyle
            )
        }
    }
}
