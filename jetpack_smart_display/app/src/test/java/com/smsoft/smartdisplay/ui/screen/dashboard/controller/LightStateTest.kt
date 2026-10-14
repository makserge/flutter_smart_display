package com.smsoft.smartdisplay.ui.screen.dashboard.controller

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The proximity light toggle reads the light's status message. */
class LightStateTest {

    @Test
    fun commandPayloadsAndCommonWords() {
        assertEquals(true, parseLightState("1", "1", "0"))
        assertEquals(false, parseLightState("0", "1", "0"))
        assertEquals(true, parseLightState(" ON ", "1", "0"))
        assertEquals(false, parseLightState("off", "1", "0"))
        assertEquals(true, parseLightState("Licht an", "licht an", "licht aus"))
        assertEquals(false, parseLightState("false", "x", "y"))
    }

    @Test
    fun anythingElseIsIgnored() {
        assertNull(parseLightState("""{"POWER":"ON"}""", "1", "0"))
        assertNull(parseLightState("", "1", "0"))
        assertNull(parseLightState("toggle", "1", "0"))
    }
}
