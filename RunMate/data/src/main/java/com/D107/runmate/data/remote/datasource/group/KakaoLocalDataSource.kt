package com.D107.runmate.data.remote.datasource.group

import com.D107.runmate.data.remote.response.kakaolocal.KakaoCoord2AddressResponse
import com.D107.runmate.data.remote.response.kakaolocal.KakaoKeywordSearchResponse
import retrofit2.Response

interface KakaoLocalDataSource {
    suspend fun getSearchKeyword(query:String): Response<KakaoKeywordSearchResponse>
    suspend fun getCoord2Address(x:Double,y:Double): Response<KakaoCoord2AddressResponse>
}