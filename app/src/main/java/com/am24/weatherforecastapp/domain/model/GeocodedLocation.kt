package com.am24.weatherforecastapp.domain.model

data class GeocodedLocation(
    val latitude: Double,
    val longitude: Double,
    val localizedName: String?
)
