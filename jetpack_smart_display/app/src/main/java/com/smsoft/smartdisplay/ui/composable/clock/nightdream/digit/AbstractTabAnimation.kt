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

    init {
        initState()
    }

    fun start() {
        makeSureCycleIsClosed()
        time = System.currentTimeMillis()
    }

    abstract fun initState()
    abstract fun initMiddleTab()
    abstract fun run()
    protected abstract fun makeSureCycleIsClosed()
}