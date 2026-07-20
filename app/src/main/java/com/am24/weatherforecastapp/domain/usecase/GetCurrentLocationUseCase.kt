package com.am24.weatherforecastapp.domain.usecase

import com.am24.weatherforecastapp.data.network.NetworkMonitor
import com.am24.weatherforecastapp.domain.OfflineException
import com.am24.weatherforecastapp.domain.model.UserLocation
import com.am24.weatherforecastapp.domain.repository.LocationRepository
import kotlinx.coroutines.CancellationException

class GetCurrentLocationUseCase(
    private val locationRepository: LocationRepository,
    private val networkMonitor: NetworkMonitor = NetworkMonitor { true }
) {
    suspend operator fun invoke(): UserLocation {
        if (!networkMonitor.isOnline()) {
            return loadSavedLocation() ?: throw OfflineException()
        }

        val currentLocation = try {
            locationRepository.getCurrentLocation()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (locationFailure: Exception) {
            val savedLocation = loadSavedLocation()
            return savedLocation ?: throw locationFailure
        }
        try {
            locationRepository.saveLastLocation(currentLocation)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // A current location remains usable when best-effort persistence fails.
        }
        return currentLocation
    }

    private suspend fun loadSavedLocation(): UserLocation? = try {
        locationRepository.getLastSavedLocation()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        null
    }
}
