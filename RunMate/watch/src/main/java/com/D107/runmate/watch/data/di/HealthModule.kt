package com.D107.runmate.watch.data.di

import com.D107.runmate.watch.data.repository.HeartRateRepositoryImpl
import com.D107.runmate.watch.domain.repository.HeartRateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HealthModule {

    @Binds
    @Singleton
    abstract fun bindHeartRateRepository(
        heartRateRepositoryImpl: HeartRateRepositoryImpl
    ): HeartRateRepository
}