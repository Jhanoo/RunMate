package com.D107.runmate.data.remote.request.group

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.OffsetDateTime

@JsonClass(generateAdapter = true)
data class CreateGroupRequest(
    @Json(name = "groupName")
    val groupName: String,

    @Json(name = "courseId")
    val courseId: String,

    @Json(name = "startTime")
    val startTime: OffsetDateTime,

    @Json(name = "startLocation")
    val startLocation: String,

    @Json(name = "latitude")
    val latitude: Double,

    @Json(name = "longitude")
    val longitude: Double
)
