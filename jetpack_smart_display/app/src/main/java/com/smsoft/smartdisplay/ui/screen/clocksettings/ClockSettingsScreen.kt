package com.smsoft.smartdisplay.ui.screen.clocksettings

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import com.firebirdberlin.nightdream.AnalogClockConfig
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.toColorInt
import com.jamal.composeprefs.ui.GroupHeader
import com.jamal.composeprefs.ui.LocalPrefsDataStore
import com.jamal.composeprefs.ui.PrefsScope
import com.jamal.composeprefs.ui.PrefsScreen
import com.jamal.composeprefs.ui.prefs.ListPref
import com.jamal.composeprefs.ui.prefs.SliderPref
import com.jamal.composeprefs.ui.prefs.TextPref
import com.smsoft.smartdisplay.R
import com.smsoft.smartdisplay.data.ClockType
import com.smsoft.smartdisplay.data.Font
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.ui.screen.clock.ClockScreen
import kotlinx.coroutines.launch

@Composable
fun ClockSettingsScreen(
    modifier: Modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background),
    viewModel: ClockSettingsViewModel = hiltViewModel(),
) {
    var clockType by remember { mutableStateOf(ClockType.getDefaultId()) }

    val context = LocalContext.current

    val dataStore = viewModel.dataStore

    val prefs by remember { dataStore.data }.collectAsState(initial = null)
    prefs?.get(stringPreferencesKey(PreferenceKey.CLOCK_TYPE.key))?.also {
        clockType = it
    }

    Scaffold(
        modifier = Modifier,
        topBar = {
            SettingsTopBar(
                modifier = Modifier,
            )
        }
    ) {
        Row (
            modifier = modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                DrawPrefs(
                    modifier = Modifier,
                    context = context,
                    dataStore = dataStore,
                    clockType = ClockType.getById(clockType),
                ) {
                    clockType = it
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1F),
                verticalArrangement = Arrangement.Center
            ) {
                ClockScreen(
                    modifier = Modifier,
                    scale = ClockType.getById(clockType).previewScale,
                    onClick = { }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun DrawPrefs(
    modifier: Modifier,
    context: Context,
    dataStore: DataStore<Preferences>,
    clockType: ClockType,
    onValueChange: (String) -> Unit
) {
    PrefsScreen(
        modifier = Modifier,
        dataStore = dataStore
    ) {
        prefsItem {
            ListPref(
                modifier = modifier,
                key = PreferenceKey.CLOCK_TYPE.key,
                title = stringResource(PreferenceKey.CLOCK_TYPE.title),
                defaultValue = ClockType.getDefaultId(),
                useSelectedAsSummary = true,
                entries = ClockType.toMap(context),
                onValueChange = onValueChange
            )
        }
        drawColorPrefs(
            modifier = modifier,
            scope = this
        )
        drawClockTypePrefs(
            modifier = modifier,
            clockType = clockType,
            scope = this,
            context = context
        )
    }
}

fun drawColorPrefs(
    modifier: Modifier,
    scope: PrefsScope
) {
    scope.prefsGroup({
        GroupHeader(
            title = stringResource(R.string.colors)
        )
    }) {
        prefsItem {
            ColorPickerPref(
                modifier = modifier,
                key = PreferenceKey.PRIMARY_COLOR.key,
                title = stringResource(PreferenceKey.PRIMARY_COLOR.title),
                defaultValue = MaterialTheme.colors.primary
            )
            ColorPickerPref(
                modifier = modifier,
                key = PreferenceKey.SECONDARY_COLOR.key,
                title = stringResource(PreferenceKey.SECONDARY_COLOR.title),
                defaultValue = MaterialTheme.colors.secondary
            )
        }
    }
}

fun drawClockTypePrefs(
    modifier: Modifier,
    clockType: ClockType,
    scope: PrefsScope,
    context: Context
) {
    when (clockType) {
        ClockType.ANALOG_ROUND -> drawAnalogRoundClockPrefs(
            modifier = modifier,
            scope = scope,
            context = context
        )
        ClockType.ANALOG_RECTANGULAR -> drawAnalogRectangularClockPrefs(
            modifier = modifier,
            scope = scope,
            context = context
        )
        ClockType.ANALOG_FSCLOCK -> drawAnalogFSClockPrefs(
            modifier = modifier,
            scope = scope,
            context = context
        )
        ClockType.DIGITAL -> drawDigitalClockPrefs(
            modifier = modifier,
            scope = scope
        )
    }
}

fun drawAnalogFSClockPrefs(
    modifier: Modifier,
    scope: PrefsScope,
    context: Context
) {

}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ColorPickerPref(
    modifier: Modifier,
    key: String,
    title: String,
    defaultValue: Color
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val selectionKey = stringPreferencesKey(key)
    val scope = rememberCoroutineScope()

    val datastore = LocalPrefsDataStore.current
    val prefs by remember { datastore.data }.collectAsState(
        initial = null
    )

    var selected = defaultValue
    prefs?.get(selectionKey)?.also {
        selected = Color(android.graphics.Color.parseColor(it))
    }

    fun edit(current: HsvColor) = run {
        scope.launch {
            try {
                datastore.edit { preferences ->
                    preferences[selectionKey] = java.lang.String.format("#%08X", -0x1 and current.toColorInt())
                }
                showDialog = false
            } catch (e: Exception) {
                Log.e("ColorPickerPref", "Could not write pref $key to database. ${e.printStackTrace()}")
            }
        }
    }

    TextPref(
        modifier = modifier,
        title = title,
        textColor = MaterialTheme.colors.onBackground,
        enabled = true,
        onClick = {
            showDialog = !showDialog
        }
    ) {
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(20.dp)
                .clip(
                    shape = RectangleShape
                )
                .background(selected)
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            text = {
                ClassicColorPicker(
                    modifier = Modifier
                        .padding(10.dp),
                    onColorChanged = {
                        edit(it)
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    },
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.primary
                    )
                }

            },
            backgroundColor = MaterialTheme.colors.surface,
            contentColor = contentColorFor(MaterialTheme.colors.surface),
            properties = DialogProperties(
                usePlatformDefaultWidth = true
            ),
        )
    }
}

@Composable
fun SettingsTopBar(
    modifier: Modifier,
) {
    TopAppBar(
        modifier = Modifier,
        title = {
            Text(
                modifier = Modifier,
                text = stringResource(R.string.settings),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    )
}
