package com.firebirdberlin.nightdream

import android.content.Context
import android.graphics.Typeface
import java.util.*

object FontCache {
    private val ASSET_PATH = "file:///android_asset/"
    private val fontCache = Hashtable<String, Typeface?>()

    operator fun get(context: Context, nameParam: String): Typeface? {
        var name = nameParam
        var tf = fontCache[nameParam]
        if (tf == null) {
            if (name.contains(ASSET_PATH)) {
                name = name.replace(ASSET_PATH, "")
            }
            tf = try {
                if (name.startsWith("file://")) {
                    Typeface.createFromFile(name.replace("file://", ""))
                } else {
                    Typeface.createFromAsset(context.assets, name)
                }
            } catch (e: RuntimeException) {
                e.printStackTrace()
                return null
            }
            fontCache[nameParam] = tf
        }
        return tf
    }
}