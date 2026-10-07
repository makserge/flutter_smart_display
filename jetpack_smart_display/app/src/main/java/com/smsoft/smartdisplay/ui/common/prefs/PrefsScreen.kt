package com.smsoft.smartdisplay.ui.common.prefs

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** DataStore the prefs below a [PrefsScreen] read from and write to. */
val LocalPrefsDataStore = staticCompositionLocalOf<DataStore<Preferences>> {
    error("LocalPrefsDataStore is only provided inside PrefsScreen")
}

/**
 * Lazy list of preference rows built with the [PrefsScope] DSL (drop-in for ComposePrefs'
 * PrefsScreen: 12dp top spacer, LazyColumn filling [modifier], dividers between rows).
 */
@Composable
fun PrefsScreen(
    dataStore: DataStore<Preferences>,
    modifier: Modifier = Modifier,
    dividerThickness: Dp = 1.dp,
    dividerIndent: Dp = 0.dp,
    content: PrefsScope.() -> Unit,
) {
    // Deliberately NOT remembered: `content` reads state (e.g. the selected clock type via a
    // delegated State) and the read is tracked by this scope, so the rows must be rebuilt on
    // every recomposition - exactly what ComposePrefs did.
    val prefsScope = PrefsScopeImpl().apply(content)

    CompositionLocalProvider(LocalPrefsDataStore provides dataStore) {
        Column {
            Spacer(Modifier.height(12.dp))
            LazyColumn(modifier = modifier.fillMaxSize()) {
                items(prefsScope.prefsItems.size) { index ->
                    prefsScope.prefsItems[index].invoke(prefsScope)
                    val showDivider = dividerThickness != 0.dp &&
                        index != prefsScope.prefsItems.lastIndex &&
                        index !in prefsScope.headerIndexes &&
                        (index + 1) !in prefsScope.headerIndexes &&
                        index !in prefsScope.footerIndexes
                    if (showDivider) {
                        PrefsDivider(thickness = dividerThickness, indent = dividerIndent)
                    }
                }
            }
        }
    }
}

@Composable
private fun PrefsDivider(thickness: Dp, indent: Dp) {
    Box(
        modifier = Modifier
            .padding(start = indent, end = indent)
            .fillMaxWidth()
            .height(thickness)
            .background(MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
    )
}

/** Latest [Preferences] of [LocalPrefsDataStore]; null until the first emission. */
@Composable
internal fun rememberPreferences(): State<Preferences?> {
    val dataStore = LocalPrefsDataStore.current
    return remember(dataStore) { dataStore.data }.collectAsStateWithLifecycle(initialValue = null)
}

/** Runs a DataStore write; failures are logged (cancellation is not swallowed). */
internal fun CoroutineScope.launchPrefWrite(key: String, block: suspend () -> Unit): Job = launch {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e("Prefs", "Could not write pref $key", e)
    }
}
