package com.D107.runmate.data.mapper

import android.location.Location
import com.D107.runmate.domain.model.running.LocationModel

object LocationMapper {
    fun toDomain(location: android.location.Location): LocationModel =
        LocationModel(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            speed = location.speed
        )

    fun toAndroid(locationModel: LocationModel, provider: String = "gps"): Location {
        return Location(provider).apply {
            latitude = locationModel.latitude
            longitude = locationModel.longitude
            altitude = locationModel.altitude
            speed = locationModel.speed
        }
    }
}