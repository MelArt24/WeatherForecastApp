package com.am24.weatherforecastapp.domain.repository

import com.am24.weatherforecastapp.domain.model.UserLocation

interface LocationRepository {
    suspend fun getCurrentLocation(): UserLocation
}
