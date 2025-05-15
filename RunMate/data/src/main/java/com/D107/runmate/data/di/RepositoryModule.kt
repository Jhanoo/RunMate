package com.D107.runmate.data.di

import com.D107.runmate.data.repository.AuthRepositoryImpl
import com.D107.runmate.data.repository.DataStoreRepositoryImpl
import com.D107.runmate.data.repository.GroupRepositoryImpl
import com.D107.runmate.data.repository.KakaoApiRepositoryImpl
import com.D107.runmate.domain.repository.DataStoreRepository
import com.D107.runmate.domain.repository.KakaoApiRepository
import com.D107.runmate.data.repository.CourseRepositoryImpl
import com.D107.runmate.data.repository.RunningTrackingRepositoryImpl
import com.D107.runmate.domain.repository.running.RunningTrackingRepository
import com.D107.runmate.data.repository.SmartInsoleRepositoryImpl
import com.D107.runmate.data.repository.SocketRepositoryImpl
import com.D107.runmate.domain.repository.SmartInsoleRepository
import com.D107.runmate.domain.repository.user.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import com.D107.runmate.domain.repository.running.RunningRepository
import com.D107.runmate.data.repository.RunningRepositoryImpl
import com.D107.runmate.domain.repository.course.CourseRepository
import com.D107.runmate.domain.repository.group.GroupRepository
import com.D107.runmate.domain.repository.socket.SocketRepository
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module(includes = [NetworkModule::class, DataStoreModule::class])
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRunningTrackingRepository(
        runningTrackingRepositoryImpl: RunningTrackingRepositoryImpl
    ): RunningTrackingRepository

    @Binds
    @Singleton
    abstract fun bindDataStoreRepository(
        dataStoreRepositoryImpl: DataStoreRepositoryImpl
    ): DataStoreRepository

    @Binds
    @Singleton
    abstract fun bindKakaoApiRepository(
    kakaoApiRepositoryImpl: KakaoApiRepositoryImpl): KakaoApiRepository

    @Binds
    @Singleton
    abstract fun bindSmartInsoleRepository(impl: SmartInsoleRepositoryImpl): SmartInsoleRepository

    @Binds
    @Singleton
    abstract fun bindRunningRepository(
        runningRepositoryImpl: RunningRepositoryImpl
    ): RunningRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(impl: GroupRepositoryImpl): GroupRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSocketRepository(impl: SocketRepositoryImpl): SocketRepository

    @Binds
    @Singleton
    abstract fun bindCourseRepository(
        courseRepositoryImpl: CourseRepositoryImpl
    ): CourseRepository
}