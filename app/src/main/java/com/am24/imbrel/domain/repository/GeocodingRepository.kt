package com.am24.imbrel.domain.repository

import com.am24.imbrel.domain.model.GeocodedLocation

interface GeocodingRepository {
    suspend fun searchLocation(query: String): GeocodedLocation?

    suspend fun resolvePlaceName(
        latitude: Double,
        longitude: Double,
    ): String?
}
