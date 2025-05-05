package com.D107.runmate.watch.data.di

import com.D107.runmate.watch.domain.usecase.timer.FormatTimeUseCase
import com.D107.runmate.watch.domain.usecase.timer.StartTimerUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TimerModule {

    @Provides
    @Singleton
    fun providerStartTimerUseCase(): StartTimerUseCase {
        return StartTimerUseCase()
    }

    @Provides
    @Singleton
    fun providerFormatTimeUseCase(): FormatTimeUseCase {
        return FormatTimeUseCase()
    }
}