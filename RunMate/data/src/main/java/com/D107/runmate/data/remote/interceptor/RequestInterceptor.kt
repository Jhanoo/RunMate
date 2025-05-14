package com.D107.runmate.data.remote.interceptor

import com.D107.runmate.data.local.UserDataStoreSource
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class RequestInterceptor constructor(private val dataStore: UserDataStoreSource): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            dataStore.accessToken.firstOrNull() ?: "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI3MmIxZGE3Ny1kMzE0LTRiZTAtODdmYS0xZWE4N2E5NWM4N2MiLCJpYXQiOjE3NDY5NDg1MjQsImV4cCI6MTc0NzAzNDkyNH0.3f1RH93cVvBapngOTKSzLZviDddG65htvXO-jD9w1lk"
        }
        val authHeaderValue = "Bearer $token"

        val requestWithToken = chain.request().newBuilder()
            .addHeader("Authorization", authHeaderValue)
            .build()

        return chain.proceed(requestWithToken)
    }
}