package com.D107.runmate.data.remote.response.kakaolocal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddressDocumentsResponse (
    @Json(name = "address")
    val address:AddressResponse,
    @Json(name = "road_address")
    val road_address: RoadAddressResponse?
)