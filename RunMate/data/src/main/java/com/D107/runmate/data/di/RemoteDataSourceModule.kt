package com.D107.runmate.data.di

import android.content.Context
import com.D107.runmate.data.remote.api.CourseService
import com.D107.runmate.data.remote.api.GroupService
import com.D107.runmate.data.remote.api.HistoryService
import com.D107.runmate.data.remote.api.KakaoLocalService
import com.D107.runmate.data.remote.api.RunningService
import com.D107.runmate.data.remote.api.UserService
import com.D107.runmate.data.remote.datasource.course.CourseDataSource
import com.D107.runmate.data.remote.datasource.course.CourseDataSourceImpl
import com.D107.runmate.data.remote.datasource.group.GroupDataSource
import com.D107.runmate.data.remote.datasource.group.GroupDataSourceImpl
import com.D107.runmate.data.remote.datasource.group.KakaoLocalDataSource
import com.D107.runmate.data.remote.datasource.group.KakaoLocalDataSourceImpl
import com.D107.runmate.data.remote.datasource.history.HistoryDataSource
import com.D107.runmate.data.remote.datasource.history.HistoryDataSourceImpl
import com.D107.runmate.data.remote.datasource.running.RunningDataSource
import com.D107.runmate.data.remote.datasource.running.RunningDataSourceImpl
import com.D107.runmate.data.remote.datasource.socket.SocketService
import com.D107.runmate.data.remote.datasource.user.AuthDataSource
import com.D107.runmate.data.remote.datasource.user.AuthDataSourceImpl
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
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

    @Provides
    @Singleton
    fun provideAuthDataSource(
        userService: UserService,
        moshi: Moshi,
        @ApplicationContext context: Context
    ): AuthDataSource {
        return AuthDataSourceImpl(userService, moshi, context)
    }

    @Provides
    @Singleton
    fun provideRunningDataSource(
        @ApplicationContext context: Context,
        runningService: RunningService
    ): RunningDataSource {
        return RunningDataSourceImpl(context, runningService)
    }

    @Provides
    @Singleton
    fun provideSocketService(): SocketService {
        return SocketService()
    }

    @Provides
    @Singleton
    fun provideCourseDataSource(
        courseService: CourseService
    ): CourseDataSource {
        return CourseDataSourceImpl(courseService)
    }

    @Provides
    @Singleton
    fun provideHistoryDataSource(
        historyService: HistoryService
    ): HistoryDataSource {
        return HistoryDataSourceImpl(historyService)
    }

}