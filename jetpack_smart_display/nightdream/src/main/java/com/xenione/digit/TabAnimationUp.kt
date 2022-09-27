package com.xenione.digit

/**
 * rotates middle tab upwards
 */
class TabAnimationUp(topTab: TabDigit.Tab, bottomTab: TabDigit.Tab, middleTab: TabDigit.Tab) :
    AbstractTabAnimation(topTab, bottomTab, middleTab) {

    override fun initState() {
        state = LOWER_POSITION
    }

    override fun initMiddleTab() {
    }

    override fun run() {
        if (time == -1L) {
            return
        }
        when (state) {
            LOWER_POSITION -> {
                bottomTab.next()
                state = MIDDLE_POSITION
            }
            MIDDLE_POSITION -> {
                if (alpha > 90) {
                    middleTab.next()
                    state = UPPER_POSITION
                }
            }
            UPPER_POSITION -> {
                if (alpha >= 180) {
                    topTab.next()
                    state = LOWER_POSITION
                    time = -1 // animation finished
                }
            }
        }
        if (time != -1L) {
            val delta = System.currentTimeMillis() - time
            alpha = (180 * (1 - (1 * elapsedTime - delta) / (1 * elapsedTime))).toInt()
            middleTab.rotate(alpha)
        }
    }

    override fun makeSureCycleIsClosed() {
        if (time == -1L) {
            return
        }
        when (state) {
            LOWER_POSITION -> {
                run {
                    middleTab.next()
                    state = UPPER_POSITION
                }
                run {
                    topTab.next()
                    state = LOWER_POSITION
                    time = -1 // animation finished
                }
            }
            UPPER_POSITION -> {
                topTab.next()
                state = LOWER_POSITION
                time = -1
            }
        }
        middleTab.rotate(180)
    }
}