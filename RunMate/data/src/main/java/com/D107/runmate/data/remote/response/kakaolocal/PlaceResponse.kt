package com.D107.runmate.data.remote.response.kakaolocal

import com.D107.runmate.data.mapper.DataMapper
import com.D107.runmate.data.remote.common.BaseResponse
import com.D107.runmate.domain.model.group.Place
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlaceResponse(
    @Json(name="id")
    val id:String,

    @Json(name = "place_name")
    val placeName: String,

    @Json(name = "address_name")
    val addressName: String,

    @Json(name = "x")
    val x: String,

    @Json(name = "y")
    val y: String

):BaseResponse{
    companion object : DataMapper<PlaceResponse, Place> {
        override fun PlaceResponse.toDomainModel(): Place {
            return Place(
                id = id,
                name = placeName,
                address = addressName,
                x = x.toDouble(),
                y = y.toDouble()
            )
        }

    }
}