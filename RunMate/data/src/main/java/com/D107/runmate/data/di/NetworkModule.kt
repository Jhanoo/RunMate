package com.D107.runmate.data.di

import com.D107.runmate.data.local.UserDataStoreSource
import com.D107.runmate.data.remote.api.KakaoLocalService
import com.D107.runmate.data.remote.common.ApiResponseAdapterFactory
import com.D107.runmate.data.remote.common.ApiResponseHandler
import com.D107.runmate.data.remote.interceptor.KakaoRequestInterceptor
import com.D107.runmate.data.remote.interceptor.RequestInterceptor
import com.D107.runmate.data.remote.logger.RunMateApiLogger
import com.D107.runmate.domain.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val SERVER_URL = BuildConfig.BASE_URL // 찐서버 url
    private const val KAKAO_API_URL = BuildConfig.KAKAO_API_URL // 카카오 API url

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(ApiResponseAdapterFactory())
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @InterceptorOkHttpClient
    @Singleton
    @Provides
    fun provideInterceptorOkHttp(requestInterceptor: RequestInterceptor): OkHttpClient {
        return OkHttpClient.Builder().apply {
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
            addInterceptor(requestInterceptor)
            addInterceptor(
                HttpLoggingInterceptor(RunMateApiLogger())
                    .apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
            )
        }.build()
    }

    @InterceptorRetrofit
    @Provides
    @Singleton
    fun provideInterceptorRetrofit(
        @InterceptorOkHttpClient okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi)) // Gson → Moshi
            .client(okHttpClient)
            .build()
    }

    @NoInterceptorRetrofit
    @Provides
    @Singleton
    fun provideRetrofit(
        @NoInterceptorOkHttpClient okHttpClient: OkHttpClient,
        moshi: Moshi // Gson → Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
    }

    @KakaoApiOkHttpClient
    @Singleton
    @Provides
    fun provideExternalApiOkHttpClient(
         kakaoRequestInterceptor: KakaoRequestInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
            addInterceptor(kakaoRequestInterceptor)
            addInterceptor(
                HttpLoggingInterceptor(RunMateApiLogger())
                    .apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
            )
        }.build()
    }

    @KakaoApiRetrofit
    @Provides
    @Singleton
    fun provideExternalApiRetrofit(
        @KakaoApiOkHttpClient okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(KAKAO_API_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
    }

    @Provides
    fun provideRequestInterceptor(userDataStoreSource: UserDataStoreSource): RequestInterceptor {
        return RequestInterceptor(userDataStoreSource)
    }

    @Provides
    fun provideKakaoRequestInterceptor(): KakaoRequestInterceptor {
        return KakaoRequestInterceptor()
    }

    @Provides
    @Singleton
    fun provideApiResponseHandler(moshi: Moshi): ApiResponseHandler {
        return ApiResponseHandler(moshi)
    }

}