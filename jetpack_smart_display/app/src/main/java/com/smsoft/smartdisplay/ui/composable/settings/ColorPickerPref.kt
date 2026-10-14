package com.smsoft.smartdisplay.ui.composable.settings

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.toColorInt
import com.smsoft.smartdisplay.ui.common.BarScreen
import com.smsoft.smartdisplay.ui.common.BarTitle
import com.smsoft.smartdisplay.ui.common.prefs.LocalPrefsDataStore
import com.smsoft.smartdisplay.ui.common.prefs.TextPref
import com.smsoft.smartdisplay.R
import kotlinx.coroutines.launch

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
    val prefs by remember { datastore.data }.collectAsStateWithLifecycle(
        initialValue = null
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
                Log.e(
                    "ColorPickerPref",
                    "Could not write pref $key to database. ${e.printStackTrace()}"
                )
            }
        }
    }

    // The colour being picked: saved with Save, not on the first move of the picker, which closed
    // the pop-up this screen replaces
    var picked by remember { mutableStateOf(HsvColor.from(selected)) }

    TextPref(
        modifier = modifier,
        title = title,
        textColor = MaterialTheme.colors.onBackground,
        enabled = true,
        onClick = {
            picked = HsvColor.from(selected)
            showDialog = true
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
        BarScreen(
            onDismissRequest = { showDialog = false },
            title = {
                BarTitle(text = title)
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = { edit(picked) }) {
                    Text(text = stringResource(R.string.save_button))
                }
            },
            scrollable = false
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(picked.toColor())
            )
            ClassicColorPicker(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 12.dp),
                color = picked,
                onColorChanged = { color: HsvColor ->
                    picked = color
                }
            )
        }
    }
}