package com.D107.runmate.data.remote.response.kakaolocal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class KakaoCoord2AddressResponse(
    @Json(name = "documents")
    val documents: List<AddressDocumentsResponse>
)