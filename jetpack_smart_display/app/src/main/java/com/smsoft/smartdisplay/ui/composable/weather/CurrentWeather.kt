package com.smsoft.smartdisplay.ui.composable.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.smsoft.smartdisplay.data.database.entity.WeatherCurrent
import com.smsoft.smartdisplay.ui.screen.weather.WeatherViewModel

@Composable
fun CurrentWeather(
    modifier: Modifier,
    viewModel: WeatherViewModel,
    currentWeather: WeatherCurrent
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.size(165.dp),
            painter = rememberAsyncImagePainter(
                stringResource(
                    R.string.weather_icon_url,
                    currentWeather.icon
                )
            ),
            contentDescription = null
        )
        Column(
            modifier = Modifier
        ) {
            Text(
                modifier = Modifier,
                text = stringResource(R.string.weather_degrees_unit, currentWeather.temperature),
                style = MaterialTheme.typography.h3,
                color = MaterialTheme.colors.primary
            )
            TextWithValue(
                modifier = Modifier,
                title = stringResource(R.string.weather_humidity),
                value = stringResource(R.string.weather_humidity_value, currentWeather.humidity),
                style = MaterialTheme.typography.h6
            )
            TextWithValue(
                modifier = Modifier,
                title = stringResource(R.string.weather_wind),
                value = stringResource(R.string.weather_wind_value, currentWeather.windSpeed,
                    viewModel.windDegreeToDirection(currentWeather.windDirection)
                ),
                style = MaterialTheme.typography.h6
            )
        }
    }
}