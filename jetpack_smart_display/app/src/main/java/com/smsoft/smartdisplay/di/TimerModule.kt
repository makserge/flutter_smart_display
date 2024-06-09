package com.smsoft.smartdisplay.di

import com.smsoft.smartdisplay.service.timer.TimerHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class TimerModule {

    @Provides
    @Singleton
    fun providesTimerHandler(
        coroutineScope: CoroutineScope
    ): TimerHandler = TimerHandler(coroutineScope)
}