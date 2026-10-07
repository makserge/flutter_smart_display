package com.smsoft.smartdisplay.utils.m3uparser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.Duration

/** The M3U parser, on the bundled station list and on a small playlist. */
class M3uParserTest {

    // Gradle runs unit tests in the module directory.
    private val stations = File("src/main/assets/radio.m3u")

    // The file's lines without blank ones; every line that is not a comment is a station.
    private val lines = stations.readLines().map { it.trim() }.filter { it.isNotEmpty() }

    @Test
    fun bundledStations_allParsed() {
        // Counted from the file, so adding or removing a station needs no change here.
        val stationCount = lines.count { !it.startsWith("#") }
        assertTrue("no stations in $stations", stationCount > 0)
        // Read as the radio player does: through a reader.
        val entries = stations.reader().use { M3uParser.parse(it) }
        assertEquals(stationCount, entries.size)
        entries.forEach { entry ->
            assertTrue("$entry", entry.location is MediaUrl)
            assertTrue("$entry", entry.location.url.protocol in setOf("http", "https"))
            assertTrue("$entry", entry.location.url.host.isNotEmpty())
            assertTrue("$entry", !entry.title.isNullOrBlank())
            // "#EXTINF:0": no duration.
            assertEquals(Duration.ZERO, entry.duration)
        }
    }

    @Test
    fun bundledStations_titlesAndUrlsInFileOrder() {
        val titles = lines.filter { it.startsWith("#EXTINF:") }.map { it.substringAfter(',') }
        val urls = lines.filterNot { it.startsWith("#") }
        val entries = M3uParser.parse(stations.toPath())
        assertEquals(titles, entries.map { it.title })
        assertEquals(urls, entries.map { it.location.toString() })
    }

    @Test
    fun extendedEntries() {
        val entries = M3uParser.parse(
            """
            #EXTM3U

            #EXTINF:123,Artist - Song
            http://example.com/song.mp3
            #EXTINF:-1 tvg-logo="http://example.com/logo.png" group-title="News",News Radio
            https://example.com/news.aac
            """.trimIndent()
        )
        assertEquals(2, entries.size)
        val (song, news) = entries

        assertEquals("Artist - Song", song.title)
        assertEquals(Duration.ofSeconds(123), song.duration)
        assertEquals("http://example.com/song.mp3", song.location.url.toString())
        assertTrue(song.metadata.isEmpty())

        assertEquals("News Radio", news.title)
        // A negative duration means unknown.
        assertNull(news.duration)
        assertEquals("https://example.com/news.aac", news.location.toString())
        assertEquals("http://example.com/logo.png", news.metadata.logo)
        assertEquals("News", news.metadata["group-title"])
    }

    @Test
    fun plainEntries_haveNoTitle() {
        val entries =
            M3uParser.parse("http://example.com/a.mp3\r\n\r\nhttp://example.com/b.mp3\r\n")
        assertEquals(
            listOf("http://example.com/a.mp3", "http://example.com/b.mp3"),
            entries.map { it.location.toString() }
        )
        entries.forEach {
            assertNull(it.title)
            assertNull(it.duration)
            assertTrue(it.metadata.isEmpty())
        }
    }

    @Test
    fun infoLineWithoutLocation_isDropped() {
        val entries = M3uParser.parse("#EXTINF:0,One\nhttp://example.com/1\n#EXTINF:0,Dangling")
        assertEquals(listOf("One"), entries.map { it.title })
    }

    @Test
    fun emptyPlaylists() {
        assertTrue(M3uParser.parse("").isEmpty())
        assertTrue(M3uParser.parse("\n  \n").isEmpty())
        assertTrue(M3uParser.parse("#EXTM3U\n").isEmpty())
    }
}
