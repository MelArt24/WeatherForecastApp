package com.am24.weatherforecastapp.domain.repository

import com.am24.weatherforecastapp.domain.model.LocationCoordinates

interface LocationRepository {
    suspend fun getCurrentLocation(): LocationCoordinates
}
