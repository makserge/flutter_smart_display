package systems.sieber.fsclock

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map

@Composable
fun FSAnalogClock(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.Center),
    dataStore: DataStore<Preferences>,
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int
) {

    val defaultPrimaryColor = getColor(MaterialTheme.colors.primary)
    var primaryColor by remember { mutableStateOf(defaultPrimaryColor) }
    getColorSetings(
        key = PreferenceKey.PRIMARY_COLOR,
        dataStore = dataStore,
        defaultValue = MaterialTheme.colors.primary
    )?.let {
        primaryColor = Color(android.graphics.Color.parseColor(it))
    }
    val primaryColorFilter = ColorFilter.tint(
        color = primaryColor
    )

    val defaultSecondaryColor = getColor(MaterialTheme.colors.secondary)
    var secondaryColor by remember { mutableStateOf(defaultSecondaryColor) }
    getColorSetings(
        key = PreferenceKey.SECONDARY_COLOR,
        dataStore = dataStore,
        defaultValue = MaterialTheme.colors.primary
    )?.let {
        secondaryColor = Color(android.graphics.Color.parseColor(it))
    }
    val secondaryColorFilter = ColorFilter.tint(
        color = secondaryColor
    )

    OnDraw(
        modifier = modifier,
        primaryColor = primaryColorFilter,
        secondaryColor = secondaryColorFilter,
        hour = hour,
        minute = minute,
        second = second,
        milliSecond = milliSecond
    )
}

@Composable
fun OnDraw(
    modifier: Modifier,
    primaryColor: ColorFilter,
    secondaryColor: ColorFilter,
    hour: Int,
    minute: Int,
    second: Int,
    milliSecond: Int
) {
    val hourAngle = (hour + minute.toFloat() / 60) * 360 / 12
    val minuteAngle = (minute + second.toFloat() / 60) * 360 / 60
    val secondAngle = (second + milliSecond.toFloat() / 1000) * 360 / 60

    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Image(
                modifier = modifier
                    .fillMaxSize(),
                colorFilter = primaryColor,
                painter = painterResource(R.drawable.ic_background),
                contentDescription = null
            )
            Image(
                modifier = modifier
                    .fillMaxSize()
                    .rotate(hourAngle),
                colorFilter = primaryColor,
                painter = painterResource(R.drawable.ic_hour),
                contentDescription = null,
            )
            Image(
                modifier = modifier
                    .fillMaxSize()
                    .rotate(minuteAngle),
                colorFilter = primaryColor,
                painter = painterResource(R.drawable.ic_minute),
                contentDescription = null
            )
            Image(
                modifier = modifier
                    .fillMaxSize()
                    .rotate(secondAngle),
                colorFilter = secondaryColor,
                painter = painterResource(R.drawable.ic_second),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun getColorSetings(
    key: PreferenceKey,
    dataStore: DataStore<Preferences>,
    defaultValue: Color
): String? {
    return getParam(
        dataStore = dataStore,
        defaultValue = "#${Integer.toHexString(defaultValue.toArgb())}"
    ) { preferences -> preferences[stringPreferencesKey(key.key)] }
            as String?
}

private fun getColor(
    color: Color
): Color {
    val defaultPrimaryColorStr = "#${Integer.toHexString(color.toArgb())}"
    return Color(android.graphics.Color.parseColor(defaultPrimaryColorStr))
}

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
private fun getParam(
    dataStore: DataStore<Preferences>,
    defaultValue: Any?,
    getter: (preferences: Preferences) -> Any?
): Any? {
    return dataStore.data.map {
        getter(it) ?: defaultValue
    }.collectAsState(initial = defaultValue).value
}