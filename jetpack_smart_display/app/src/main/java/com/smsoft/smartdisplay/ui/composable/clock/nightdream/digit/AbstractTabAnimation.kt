package com.smsoft.smartdisplay.ui.composable.clock.nightdream.digit

abstract class AbstractTabAnimation(
    protected val topTab: TabDigit.Tab,
    protected val bottomTab: TabDigit.Tab,
    protected val middleTab: TabDigit.Tab
) {
    protected val LOWER_POSITION = 0
    protected val MIDDLE_POSITION = 1
    protected val UPPER_POSITION = 2

    protected var state = 0
    protected var alpha = 0
    protected var time = -1L
    protected var elapsedTime = 1000F

    /**
     * True from [start] until the flip has finished. The view only redraws itself while it is set.
     */
    val isRunning: Boolean
        get() = time != -1L

    init {
        initState()
    }

    fun start() {
        makeSureCycleIsClosed()
        time = System.currentTimeMillis()
    }

    /**
     * Drops a running flip and puts the middle tab back to its resting position, so all tabs can be
     * set to any character without a half-finished cycle advancing some of them later.
     */
    fun reset() {
        time = -1L
        initState()
        initMiddleTab()
    }

    abstract fun initState()
    abstract fun initMiddleTab()
    abstract fun run()
    protected abstract fun makeSureCycleIsClosed()
}