package de.dixieflatline.mpcw.client.mpd

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Small pieces of the MPD protocol: the greeting and quoting. (ResponseParser returns
 * android.util.Pair, which is only a stub on the JVM.)
 */
class MpdProtocolTest {

    @Test
    fun version_fromGreeting() {
        assertEquals("0.23.5", Version.parse("OK MPD 0.23.5").toString())
    }

    @Test(expected = InvalidFormatException::class)
    fun version_notMpd_throws() {
        Version.parse("HTTP/1.1 400 Bad Request")
    }

    @Test
    fun quote_escapesQuotes() {
        assertEquals("\"Radio 1\"", EscapeUtil.quote("Radio 1"))
        assertEquals("\"say \\\"hi\\\"\"", EscapeUtil.quote("say \"hi\""))
    }
}
