package com.am24.weatherforecastapp.domain.usecase

import com.am24.weatherforecastapp.domain.model.LocationCoordinates
import com.am24.weatherforecastapp.domain.repository.LocationRepository

class GetCurrentLocationUseCase(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(): LocationCoordinates =
        locationRepository.getCurrentLocation()
}
