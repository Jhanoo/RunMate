package com.D107.runmate.watch.data.di

import com.D107.runmate.watch.data.repository.CadenceRepositoryImpl
import com.D107.runmate.watch.domain.repository.CadenceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CadenceModule {

    @Binds
    @Singleton
    abstract fun provideCadenceRepository(
        cadenceRepositoryImpl: CadenceRepositoryImpl
    ): CadenceRepository
}