package com.smsoft.smartdisplay.di

import android.content.Context
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionHandler
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionService.Companion.MODEL
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionService.Companion.SAMPLE_RATE
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionService.Companion.TARGET_PATH
import com.smsoft.smartdisplay.service.asr.SpeechRecognitionServiceHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SpeechRecognitionModule {

    @Provides
    @Singleton
    fun provideModel(
        @ApplicationContext context: Context
    ): Model {
        val outputPath = StorageService.sync(
            context,
            MODEL,
            TARGET_PATH
        )
        return Model(outputPath)
    }

    @Provides
    @Singleton
    fun provideSpeechService(
        model: Model
    ): SpeechService {
        val rec = Recognizer(model, SAMPLE_RATE)
        return SpeechService(rec, SAMPLE_RATE)
    }

    @Provides
    @Singleton
    fun provideSpeechRecognitionHandler(): SpeechRecognitionHandler = SpeechRecognitionServiceHandler()
}