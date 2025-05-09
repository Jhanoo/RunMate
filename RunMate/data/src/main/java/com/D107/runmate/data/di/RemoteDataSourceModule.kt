package com.D107.runmate.data.di

import com.D107.runmate.data.remote.api.KakaoLocalService
import com.D107.runmate.data.remote.datasource.KakaoLocalDataSource
import com.D107.runmate.data.remote.datasource.KakaoLocalDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteDataSourceModule {
    @Provides
    @Singleton
    fun provideKakaoLocalDataSource(kakaoLocalService: KakaoLocalService): KakaoLocalDataSource {
        return KakaoLocalDataSourceImpl(kakaoLocalService)
    }
}