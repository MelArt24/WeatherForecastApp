package com.am24.imbrel.domain.model

data class SavedLocation(
    val latitude: Double,
    val longitude: Double,
    val placeName: String?,
    val savedAtMillis: Long,
) {
    fun toUserLocation() =
        UserLocation(
            latitude = latitude,
            longitude = longitude,
            placeName = placeName,
        )
}
