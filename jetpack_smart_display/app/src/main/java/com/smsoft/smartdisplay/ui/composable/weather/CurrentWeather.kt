package com.smsoft.smartdisplay.ui.composable.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import coil3.compose.rememberAsyncImagePainter
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.database.entity.WeatherCurrent
import com.smsoft.smartdisplay.ui.screen.weather.WeatherViewModel

/**
 * The current conditions: the icon, the temperature and three details, in [sizes]. On a small
 * panel the icon is beside the text; otherwise it sits above the text, filling the height of a
 * large screen.
 */
@Composable
fun CurrentWeather(
    modifier: Modifier,
    viewModel: WeatherViewModel,
    currentWeather: WeatherCurrent,
    nightTemperature: Int? = null,
    sizes: WeatherSizes
) {
    val compact = sizes.compact
    val temperatureStyle = sizes.temperature
    val detailStyle = sizes.detail
    val icon = @Composable {
        Image(
            modifier = Modifier.size(sizes.currentIcon),
            painter = rememberAsyncImagePainter(
                stringResource(
                    R.string.weather_icon_url,
                    currentWeather.icon
                )
            ),
            contentDescription = null
        )
    }
    val details = @Composable {
        Column(
            modifier = Modifier,
            horizontalAlignment = if (compact) Alignment.Start else Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier,
                text = stringResource(R.string.weather_degrees_unit, currentWeather.temperature),
                style = temperatureStyle,
                color = MaterialTheme.colors.primary
            )
            if (nightTemperature != null) {
                TextWithValue(
                    modifier = Modifier,
                    title = stringResource(R.string.weather_night_temperature),
                    value = stringResource(R.string.weather_degrees_unit, nightTemperature),
                    style = detailStyle
                )
            }
            TextWithValue(
                modifier = Modifier,
                title = stringResource(R.string.weather_humidity),
                value = stringResource(R.string.weather_humidity_value, currentWeather.humidity),
                style = detailStyle
            )
            TextWithValue(
                modifier = Modifier,
                title = stringResource(R.string.weather_wind),
                value = stringResource(R.string.weather_wind_value, currentWeather.windSpeed,
                    viewModel.windDegreeToDirection(currentWeather.windDirection)
                ),
                style = detailStyle
            )
        }
    }
    if (compact) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            details()
        }
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            details()
        }
    }
}
