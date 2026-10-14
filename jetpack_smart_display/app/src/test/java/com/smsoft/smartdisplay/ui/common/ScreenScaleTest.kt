package com.smsoft.smartdisplay.ui.common

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenScaleTest {

    @Test
    fun squareFourInchPanelIsScaledUpToTheDesignWidth() {
        // 480 px at 120 px per inch: 4 inch per side, a diagonal of about 5.66 inch
        assertEquals(480F / 360F, uiScaleFor(diagonalInches = 5.66F, widthDp = 480F, smallerSidePx = 480), 0.001F)
    }

    @Test
    fun largeScreenIsNotScaled() {
        // An 8 inch screen such as the Lenovo ThinkSmart keeps its own controls
        assertEquals(1F, uiScaleFor(diagonalInches = 8F, widthDp = 800F, smallerSidePx = 800), 0F)
    }

    @Test
    fun largePanelReportingItsDensityAsPixelsPerInchIsNotScaled() {
        // 1280 by 800 at a reported 240 px per inch: a 6.3 inch diagonal by the numbers, really 8
        assertEquals(1F, uiScaleFor(diagonalInches = 6.29F, widthDp = 533F, smallerSidePx = 800), 0F)
    }

    @Test
    fun smallScreenIsNeverScaledDown() {
        assertEquals(1F, uiScaleFor(diagonalInches = 4F, widthDp = 282F, smallerSidePx = 282), 0F)
    }
}
