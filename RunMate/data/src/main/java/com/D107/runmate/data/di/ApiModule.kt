package com.D107.runmate.data.di

import com.D107.runmate.data.remote.api.CourseService
import com.D107.runmate.data.remote.api.CurriculumService
import com.D107.runmate.data.remote.api.RunningService
import com.D107.runmate.data.remote.api.GroupService
import com.D107.runmate.data.remote.api.HistoryService
import com.D107.runmate.data.remote.api.KakaoLocalService
import com.D107.runmate.data.remote.api.MarathonService
import com.D107.runmate.data.remote.api.TodoService
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

    @Provides
    @Singleton
    fun provideCourseService(@InterceptorRetrofit retrofit: Retrofit): CourseService {
        return retrofit.create(CourseService::class.java)
    }

    @Provides
    @Singleton
    fun provideHistoryService(@InterceptorRetrofit retrofit: Retrofit): HistoryService {
        return retrofit.create(HistoryService::class.java)
    }

    @Provides
    @Singleton
    fun marathonService(@InterceptorRetrofit retrofit: Retrofit): MarathonService {
        return retrofit.create(MarathonService::class.java)
    }

    @Provides
    @Singleton
    fun curriculumService(@InterceptorRetrofit retrofit: Retrofit): CurriculumService {
        return retrofit.create(CurriculumService::class.java)
    }

    @Provides
    @Singleton
    fun todoService(@InterceptorRetrofit retrofit: Retrofit): TodoService {
        return retrofit.create(TodoService::class.java)
    }
}