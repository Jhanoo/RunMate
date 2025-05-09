package com.D107.runmate.data.remote.interceptor


import com.D107.runmate.data.local.UserDataStoreSource
import com.D107.runmate.domain.BuildConfig
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class KakaoRequestInterceptor constructor(): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        val requestWithToken = chain.request().newBuilder()
            .addHeader("Authorization","KakaoAK ${BuildConfig.REST_API_KEY}")
            .build()

        return chain.proceed(requestWithToken)
    }
}