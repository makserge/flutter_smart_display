package com.smsoft.smartdisplay.ui.composable.clock.nightdream.digit

/**
 * rotates middle tab downwards
 */
class TabAnimationDown(
    topTab: TabDigit.Tab,
    bottomTab: TabDigit.Tab,
    middleTab: TabDigit.Tab) :
    AbstractTabAnimation(
        topTab,
        bottomTab,
        middleTab
    ) {

    override fun initState() {
        state = UPPER_POSITION
    }

    override fun initMiddleTab() {
        middleTab.rotate(180)
    }

    override fun run() {
        if (time == -1L) {
            return
        }
        when (state) {
            LOWER_POSITION -> {
                if (alpha <= 0) {
                    bottomTab.next()
                    state = UPPER_POSITION
                    time = -1 // animation finished
                }
            }
            MIDDLE_POSITION -> {
                if (alpha < 90) {
                    middleTab.next()
                    state = LOWER_POSITION
                }
            }
            UPPER_POSITION -> {
                topTab.next()
                state = MIDDLE_POSITION
            }
        }
        if (time != -1L) {
            val delta = System.currentTimeMillis() - time
            alpha = 180 - (180 * (1 - (1 * elapsedTime - delta) / (1 * elapsedTime))).toInt()
            middleTab.rotate(alpha)
        }
    }

    override fun makeSureCycleIsClosed() {
        if (time == -1L) {
            return
        }
        if (state == LOWER_POSITION) {
            bottomTab.next()
            state = UPPER_POSITION
            time = -1 // animation finished
        }
        middleTab.rotate(180)
    }
}