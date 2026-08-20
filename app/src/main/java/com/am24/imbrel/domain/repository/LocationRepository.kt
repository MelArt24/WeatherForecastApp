package com.am24.imbrel.domain.repository

import com.am24.imbrel.domain.model.UserLocation

interface LocationRepository {
    suspend fun getCurrentLocation(): UserLocation

    suspend fun getLastSavedLocation(): UserLocation?

    suspend fun saveLastLocation(location: UserLocation)
}
