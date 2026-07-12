package com.am24.weatherforecastapp.domain.repository

import com.am24.weatherforecastapp.domain.model.GeocodedLocation

interface GeocodingRepository {
    suspend fun searchLocation(query: String): GeocodedLocation?

    suspend fun resolvePlaceName(
        latitude: Double,
        longitude: Double
    ): String?
}
