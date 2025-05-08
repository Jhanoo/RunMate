package com.D107.runmate.data.di

import com.D107.runmate.data.repository.RunningTrackingRepositoryImpl
import com.D107.runmate.domain.repository.running.RunningTrackingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module(includes = [NetworkModule::class, DataStoreModule::class])
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDataStoreRepository(
        runningTrackingRepositoryImpl: RunningTrackingRepositoryImpl
    ): RunningTrackingRepository
}