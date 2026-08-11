package com.am24.weatherforecastapp.domain.usecase

import com.am24.weatherforecastapp.domain.error.DomainError
import com.am24.weatherforecastapp.domain.error.DomainFailureException
import com.am24.weatherforecastapp.domain.error.NetworkErrorReason
import com.am24.weatherforecastapp.domain.model.UserLocation
import com.am24.weatherforecastapp.domain.network.NetworkMonitor
import com.am24.weatherforecastapp.domain.network.isOnlineOrDomainFailure
import com.am24.weatherforecastapp.domain.repository.LocationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetCurrentLocationUseCase(
    private val locationRepository: LocationRepository,
    private val networkMonitor: NetworkMonitor =
        object : NetworkMonitor {
            override fun isOnline(): Boolean = true

            override fun observeConnectivity(): Flow<Boolean> = flowOf(true)
        },
) {
    /**
     * Returns a saved location when offline or when acquiring a current location fails.
     *
     * If no saved value is available, an offline probe produces an offline domain failure, while
     * an acquisition failure is rethrown unchanged. Saving a newly acquired location is
     * best-effort; cancellation is always propagated.
     */
    suspend operator fun invoke(): UserLocation {
        if (!networkMonitor.isOnlineOrDomainFailure()) {
            return loadSavedLocation() ?: throw DomainFailureException(
                DomainError.Network(NetworkErrorReason.Offline),
            )
        }

        val currentLocation =
            try {
                locationRepository.getCurrentLocation()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (
                @Suppress("TooGenericExceptionCaught")
                locationFailure: Exception,
            ) {
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

    private suspend fun loadSavedLocation(): UserLocation? =
        try {
            locationRepository.getLastSavedLocation()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            null
        }
}
