package com.smsoft.smartdisplay.service.radio

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.smsoft.smartdisplay.data.PreferenceKey
import com.smsoft.smartdisplay.data.RadioType
import com.smsoft.smartdisplay.utils.mpd.data.MPDCredentials
import com.smsoft.smartdisplay.utils.radioPresetKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The radio settings the radio player is built from. */
class RadioConfigTest {

    private val type = stringPreferencesKey(PreferenceKey.RADIO_TYPE.key)
    private val host = stringPreferencesKey(PreferenceKey.MPD_SERVER_HOST.key)
    private val port = stringPreferencesKey(PreferenceKey.MPD_SERVER_PORT.key)
    private val password = stringPreferencesKey(PreferenceKey.MPD_SERVER_PASSWORD.key)

    // The config read with the MPD radio selected and the given server settings.
    private fun mpdConfig(vararg pairs: Preferences.Pair<*>) =
        readRadioConfig(preferencesOf(type to "mpd", *pairs))

    @Test
    fun noSettings_internalRadio() {
        assertEquals(RadioConfig(RadioType.INTERNAL, mpd = null), readRadioConfig(preferencesOf()))
    }

    @Test
    fun radioType_readById() {
        assertEquals(RadioType.INTERNAL, readRadioConfig(preferencesOf(type to "internal")).type)
        assertEquals(RadioType.MPD, readRadioConfig(preferencesOf(type to "mpd")).type)
    }

    @Test
    fun unknownRadioType_fallsBackToInternal() {
        assertEquals(
            RadioConfig(RadioType.INTERNAL, mpd = null),
            readRadioConfig(preferencesOf(type to "spotify"))
        )
        assertEquals(RadioType.INTERNAL, RadioType.getById(""))
        RadioType.entries.forEach { assertEquals(it, RadioType.getById(it.id)) }
    }

    @Test
    fun internal_hasNoMpdServer() {
        val config = readRadioConfig(
            preferencesOf(
                type to "internal",
                host to "192.168.1.5",
                port to "6601",
                password to "secret"
            )
        )
        assertNull(config.mpd)
    }

    @Test
    fun mpd_defaults() {
        assertEquals(MPDCredentials(host = "", port = 6600, password = ""), mpdConfig().mpd)
    }

    @Test
    fun mpd_readsTheServer() {
        assertEquals(
            MPDCredentials(host = "music.local", port = 6601, password = "secret"),
            mpdConfig(host to "music.local", port to "6601", password to "secret").mpd
        )
    }

    @Test
    fun mpd_hostAndPortAreTrimmed() {
        val config = mpdConfig(host to "  192.168.1.5 \n", port to " 6601 ")
        assertEquals("192.168.1.5", config.mpd?.host)
        assertEquals(6601, config.mpd?.port)
    }

    @Test
    fun mpd_invalidPort_fallsBackToTheDefault() {
        for (invalid in listOf("", " ", "abc", "66o0", "6600.0", "99999999999")) {
            assertEquals("port '$invalid'", 6600, mpdConfig(port to invalid).mpd?.port)
        }
    }

    @Test
    fun mpdEdit_whileInternal_isNoChange() {
        val before = readRadioConfig(
            preferencesOf(type to "internal", host to "a.local", port to "6600")
        )
        val after = readRadioConfig(
            preferencesOf(type to "internal", host to "b.local", port to "7000", password to "x")
        )
        assertEquals(before, after)
    }

    @Test
    fun mpdEdit_whileMpd_isAChange() {
        assertNotEquals(mpdConfig(host to "a.local"), mpdConfig(host to "b.local"))
        assertNotEquals(mpdConfig(port to "6600"), mpdConfig(port to "6601"))
        assertNotEquals(mpdConfig(password to ""), mpdConfig(password to "x"))
        // Only whitespace or an invalid port is no change.
        assertEquals(
            mpdConfig(host to "a.local", port to "6600"),
            mpdConfig(host to " a.local ", port to "oops")
        )
    }

    @Test
    fun presetKey_perRadioType() {
        // The internal list and the MPD queue keep separate station indexes.
        assertEquals(PreferenceKey.RADIO_PRESET.key, radioPresetKey(RadioType.INTERNAL).name)
        assertEquals(PreferenceKey.RADIO_PRESET_MPD.key, radioPresetKey(RadioType.MPD).name)
        assertNotEquals(radioPresetKey(RadioType.INTERNAL), radioPresetKey(RadioType.MPD))
    }
}
