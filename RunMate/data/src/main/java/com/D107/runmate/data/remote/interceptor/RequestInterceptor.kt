package com.D107.runmate.data.remote.interceptor

import com.D107.runmate.data.local.UserDataStoreSource
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class RequestInterceptor constructor(private val dataStore: UserDataStoreSource): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            dataStore.accessToken.firstOrNull() ?: ""
        }
        val requestWithToken = chain.request().newBuilder()
            .addHeader("Authorization", token)
            .build()

        return chain.proceed(requestWithToken)
    }
}