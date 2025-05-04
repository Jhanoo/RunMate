package com.D107.runmate.data.di

import com.D107.runmate.data.repository.SmartInsoleRepositoryImpl
import com.D107.runmate.domain.repository.SmartInsoleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSmartInsoleRepository(impl: SmartInsoleRepositoryImpl): SmartInsoleRepository
}