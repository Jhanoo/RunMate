package com.D107.runmate.watch.data.di

import android.content.Context
import androidx.room.Room
import com.D107.runmate.watch.data.local.GpxDatabase
import com.D107.runmate.watch.data.remote.GpxApiService
import com.D107.runmate.watch.data.repository.GpxRepositoryImpl
import com.D107.runmate.watch.domain.repository.GpxRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GpxModule {

    @Provides
    @Singleton
    fun provideGpxDatabase(@ApplicationContext context: Context): GpxDatabase {
        return Room.databaseBuilder(
            context,
            GpxDatabase::class.java,
            "gpx_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideGpxDao(database: GpxDatabase) = database.gpxDao()

    @Provides
    @Singleton
    fun provideGpxApiService(retrofit: Retrofit): GpxApiService {
        return retrofit.create(GpxApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGpxRepository(
        @ApplicationContext context: Context,
        gpxDao: com.D107.runmate.watch.data.local.GpxDao,
        apiService: GpxApiService
    ): GpxRepository {
        return GpxRepositoryImpl(context, gpxDao)
    }
}