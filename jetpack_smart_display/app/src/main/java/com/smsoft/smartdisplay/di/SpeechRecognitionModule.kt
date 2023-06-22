package com.smsoft.smartdisplay.di

import com.smsoft.smartdisplay.service.asr.SpeechRecognitionHandler
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionServiceHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SpeechRecognitionModule {

    @Provides
    @Singleton
    fun provideSpeechRecognitionHandler(): SpeechRecognitionHandler = SpeechRecognitionServiceHandler()
}