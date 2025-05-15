package com.D107.runmate.data.di

import com.D107.runmate.data.remote.api.RunningService
import com.D107.runmate.data.remote.api.GroupService
import com.D107.runmate.data.remote.api.KakaoLocalService
import com.D107.runmate.data.remote.api.UserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module(includes = [NetworkModule::class, DataStoreModule::class])
@InstallIn(SingletonComponent::class)
class ApiModule {
    @Provides
    @Singleton
    fun provideRunningService(
        @InterceptorRetrofit retrofit: Retrofit
    ): RunningService {
        return retrofit.create(RunningService::class.java)
    }
    @Provides
    @Singleton
    fun kakaoLocalService(@KakaoApiRetrofit retrofit: Retrofit): KakaoLocalService {
        return retrofit.create(KakaoLocalService::class.java)
    }

    @Provides
    @Singleton
    fun groupService(@InterceptorRetrofit retrofit: Retrofit): GroupService {
        return retrofit.create(GroupService::class.java)
    }

    @Provides
    @Singleton
    fun userService(@InterceptorRetrofit retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }

}