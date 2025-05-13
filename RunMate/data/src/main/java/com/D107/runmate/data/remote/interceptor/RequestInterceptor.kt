package com.D107.runmate.data.remote.interceptor

import com.D107.runmate.data.local.UserDataStoreSource
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class RequestInterceptor constructor(private val dataStore: UserDataStoreSource): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
//        val token = runBlocking {
//            dataStore.accessToken.firstOrNull() ?: ""
//        }
        val token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhNDY5YTRmYy01ZmViLTQ5ZWEtODRiZS03ODcxYTE1NmFhMjgiLCJpYXQiOjE3NDcxMjY1MTIsImV4cCI6MTc0NzIxMjkxMn0.OFGl33X9nezZtXGuRT9vmc2r4JsnZsBR3pRzCQt3Iu4"
        val requestWithToken = chain.request().newBuilder()
            .addHeader("Authorization", token)
            .build()

        return chain.proceed(requestWithToken)
    }
}