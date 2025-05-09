package com.D107.runmate.data.di

import com.D107.runmate.data.repository.DataStoreRepositoryImpl
import com.D107.runmate.data.repository.KakaoApiRepositoryImpl
import com.D107.runmate.domain.repository.DataStoreRepository
import com.D107.runmate.domain.repository.KakaoApiRepository
import com.D107.runmate.data.repository.RunningTrackingRepositoryImpl
import com.D107.runmate.domain.repository.running.RunningTrackingRepository
import com.D107.runmate.data.repository.DataStoreRepositoryImpl
import com.D107.runmate.data.repository.SmartInsoleRepositoryImpl
import com.D107.runmate.domain.repository.DataStoreRepository
import com.D107.runmate.domain.repository.SmartInsoleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
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
>>>>>>> RunMate/data/src/main/java/com/D107/runmate/data/di/RepositoryModule.kt

}