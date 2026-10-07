package com.smsoft.smartdisplay.ui.common.prefs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Receiver of the [PrefsScreen] DSL. Same shape as the former ComposePrefs
 * `com.jamal.composeprefs.ui.PrefsScope`, so existing settings builders only change imports.
 */
interface PrefsScope {
    /** One lazy-list row. Several prefs emitted inside it are stacked vertically. */
    fun prefsItem(content: @Composable PrefsScope.() -> Unit)

    /** A group headed by a default [GroupHeader] showing [title]. */
    fun prefsGroup(title: String, items: PrefsScope.() -> Unit)

    /** A group headed by a custom [header]; ends with a 16dp spacer (as in ComposePrefs). */
    fun prefsGroup(header: @Composable PrefsScope.() -> Unit, items: PrefsScope.() -> Unit)
}

internal class PrefsScopeImpl : PrefsScope {
    val prefsItems = mutableListOf<@Composable PrefsScope.() -> Unit>()
    val headerIndexes = mutableSetOf<Int>()
    val footerIndexes = mutableSetOf<Int>()

    override fun prefsItem(content: @Composable PrefsScope.() -> Unit) {
        prefsItems += content
    }

    override fun prefsGroup(title: String, items: PrefsScope.() -> Unit) {
        prefsGroup(header = { GroupHeader(title = title) }, items = items)
    }

    override fun prefsGroup(header: @Composable PrefsScope.() -> Unit, items: PrefsScope.() -> Unit) {
        headerIndexes += prefsItems.size
        prefsItem(header)
        items()
        prefsItem { Spacer(Modifier.height(16.dp)) }
        // No divider after the group's last row nor after its footer spacer.
        footerIndexes += prefsItems.size - 2
        footerIndexes += prefsItems.size - 1
    }
}
