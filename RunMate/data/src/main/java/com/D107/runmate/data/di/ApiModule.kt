package com.D107.runmate.data.di

import com.D107.runmate.data.remote.api.KakaoLocalService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [NetworkModule::class, DataStoreModule::class])
@InstallIn(SingletonComponent::class)
class ApiModule {
    @Provides
    @Singleton
    fun kakaoLocalService(@KakaoApiRetrofit retrofit: Retrofit): KakaoLocalService {
        return retrofit.create(KakaoLocalService::class.java)
    }

}