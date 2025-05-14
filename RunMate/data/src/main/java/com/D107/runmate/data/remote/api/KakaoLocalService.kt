package com.D107.runmate.data.remote.api

import com.D107.runmate.data.remote.response.kakaolocal.KakaoKeywordSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface KakaoLocalService {
    @GET("local/search/keyword.json")
    suspend fun getSearchKeyword(
        @Query("query") query: String,
    ):Response<KakaoKeywordSearchResponse>

}