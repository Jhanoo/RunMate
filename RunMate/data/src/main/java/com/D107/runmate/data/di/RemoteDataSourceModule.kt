package com.D107.runmate.data.di

import com.D107.runmate.data.remote.api.GroupService
import com.D107.runmate.data.remote.api.KakaoLocalService
import com.D107.runmate.data.remote.datasource.group.GroupDataSource
import com.D107.runmate.data.remote.datasource.group.GroupDataSourceImpl
import com.D107.runmate.data.remote.datasource.group.KakaoLocalDataSource
import com.D107.runmate.data.remote.datasource.group.KakaoLocalDataSourceImpl
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

    @Provides
    @Singleton
    fun provideGroupDataSource(groupService: GroupService): GroupDataSource {
        return GroupDataSourceImpl(groupService)
    }
}