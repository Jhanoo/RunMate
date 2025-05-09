package com.D107.runmate.data.di

import com.D107.runmate.data.repository.DataStoreRepositoryImpl
import com.D107.runmate.data.repository.KakaoApiRepositoryImpl
import com.D107.runmate.domain.repository.DataStoreRepository
import com.D107.runmate.domain.repository.KakaoApiRepository
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
    abstract fun bindDataStoreRepository(
        dataStoreRepositoryImpl: DataStoreRepositoryImpl
    ): DataStoreRepository

//    @Binds
//    @Singleton
//    abstract fun bindSmartInsoleRepository(impl: SmartInsoleRepositoryImpl): SmartInsoleRepository
    @Binds
    @Singleton
    abstract fun bindKakaoApiRepository(
    kakaoApiRepositoryImpl: KakaoApiRepositoryImpl): KakaoApiRepository

}