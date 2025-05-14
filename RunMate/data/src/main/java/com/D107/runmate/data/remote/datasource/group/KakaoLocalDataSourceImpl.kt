package com.D107.runmate.data.remote.datasource.group

import com.D107.runmate.data.remote.api.KakaoLocalService
import com.D107.runmate.data.remote.response.kakaolocal.KakaoCoord2AddressResponse
import com.D107.runmate.data.remote.response.kakaolocal.KakaoKeywordSearchResponse
import retrofit2.Response
import javax.inject.Inject

class KakaoLocalDataSourceImpl @Inject constructor(
    private val kakaoLocalService: KakaoLocalService
): KakaoLocalDataSource {
    override suspend fun getSearchKeyword(query: String): Response<KakaoKeywordSearchResponse> = kakaoLocalService.getSearchKeyword(query)
    override suspend fun getCoord2Address(
        x: Double,
        y: Double
    ):Response<KakaoCoord2AddressResponse> = kakaoLocalService.getCoord2Address(x, y)
}